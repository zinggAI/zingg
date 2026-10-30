package io.zingg.duckdb.legacy;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.zingg.duckdb.api.DuckException;
import io.zingg.duckdb.model.ImportLimits;
import io.zingg.duckdb.model.ImportProvenance;
import io.zingg.duckdb.model.ModelArtifactWriter;
import io.zingg.duckdb.model.ModelFormat;
import io.zingg.duckdb.model.ModelManifest;
import io.zingg.duckdb.model.ModelType;
import io.zingg.duckdb.model.ModelArtifactJson;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.List;
import java.util.HexFormat;
import java.util.Set;
import java.util.TreeSet;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class LegacyBlockingTreeImporterTest {
  @Test
  void isolatedImporterRejectsOutputNestedUnderInputBeforeStartingChild(@TempDir Path temp) throws Exception {
    Path input = temp.resolve("input");
    Files.createDirectories(input);
    Path output = input.resolve("nested/output");

    DuckException failure = assertThrows(DuckException.class, () ->
        new IsolatedImporterProcess(ImportLimits.defaults()).run(
            List.of("missing-importer-executable"), input, output));

    assertTrue(failure.getMessage().contains("outside input directory"));
    assertFalse(Files.exists(output), "validation must happen before output creation or process launch");
  }

  @Test
  void isolatedImporterRejectsSymlinkedOutputAndPreservesExternalSentinel(@TempDir Path temp) throws Exception {
    Path input = temp.resolve("input");
    Path outside = temp.resolve("outside");
    Files.createDirectories(input);
    Files.createDirectories(outside);
    Path sentinel = outside.resolve("sentinel");
    byte[] original = "preserve-me".getBytes(StandardCharsets.UTF_8);
    Files.write(sentinel, original);
    Path output = temp.resolve("output-link");
    try {
      Files.createSymbolicLink(output, outside);
    } catch (UnsupportedOperationException | java.io.IOException | SecurityException unavailable) {
      Assumptions.abort("directory symlink creation is unavailable: " + unavailable.getClass().getSimpleName());
    }

    DuckException failure = assertThrows(DuckException.class, () ->
        new IsolatedImporterProcess(ImportLimits.defaults()).run(
            List.of("missing-importer-executable"), input, output));

    assertTrue(failure.getMessage().contains("symbolic link"));
    assertArrayEquals(original, Files.readAllBytes(sentinel));
  }

  @Test
  void neutralPayloadJsonEscapesAllControlsAndPreservesUnicode() {
    StringBuilder adversarial = new StringBuilder("quote=\\\" slash=\\\\ snowman=☃ duck=🦆 controls=");
    for (int value = 0; value < 0x20; value++) adversarial.append((char) value);
    String json = LegacyBlockingTreeImporterMain.toJson(java.util.Map.of("value", adversarial.toString()));
    var decoded = ModelArtifactJson.parseObject(json);
    assertEquals(adversarial.toString(), decoded.get("value"));
    assertFalse(json.chars().anyMatch(character -> character < 0x20), "raw JSON controls must never be emitted");
  }

  @Test
  void recordsPinnedCandidateTreeSerializationDescriptor() throws Exception {
    Path fixture = fixtureRoot();
    Path serializedTree = fixture.resolve("block/zingg.block");
    assertTrue(Files.isRegularFile(serializedTree), "pinned blocking candidate must be present");
    verifyFixtureChecksum(fixture, serializedTree);
    try (var input = new DescriptorInputStream(Files.newInputStream(serializedTree))) {
      assertThrows(ClassNotFoundException.class, input::readObject);
      ObjectStreamClass descriptor = input.descriptors().get(0);
      assertTrue(descriptor != null, "the serialized root descriptor must be captured");
      assertEquals("zingg.block.Tree", descriptor.getName());
      assertEquals(-7879348438713840442L, descriptor.getSerialVersionUID());
      Set<String> names = new TreeSet<>();
      for (ObjectStreamClass observed : input.descriptors()) names.add(observed.getName());
      assertTrue(names.containsAll(Set.of("zingg.block.Tree", "zingg.block.Canopy",
          "zingg.client.FieldDefinition", "zingg.client.MatchType")),
          "all historical root, canopy, field and match-type descriptors must remain visible");
    }
  }

  @Test
  void pinnedCandidateWithPreRestructureClassNamesFailsClosedWithoutPublishing(@TempDir Path temp)
      throws Exception {
    Path fixture = fixtureRoot();
    Path serializedTree = fixture.resolve("block/zingg.block");
    assertTrue(Files.isRegularFile(serializedTree), "pinned blocking candidate must be present");
    verifyFixtureChecksum(fixture, serializedTree);
    Path output = temp.resolve("blocking.model");

    DuckException failure = assertThrows(DuckException.class, () ->
        LegacyBlockingTreeImporterMain.importSerialized(serializedTree, output, ImportLimits.defaults()));

    assertTrue(failure.getMessage().contains("class is not allowed or unavailable"));
    assertFalse(Files.exists(output), "rejected legacy class descriptors must not publish a neutral model");
  }

  @Test
  void truncatedStreamFailsWithStableDiagnosticAndDoesNotPublish(@TempDir Path temp) throws Exception {
    Path binary = temp.resolve("truncated.ser");
    Files.write(binary, new byte[] {(byte) 0xac, (byte) 0xed, 0, 5, 0x73});
    Path output = temp.resolve("model");

    DuckException failure = assertThrows(DuckException.class, () ->
        LegacyBlockingTreeImporterMain.importSerialized(binary, output, ImportLimits.defaults()));

    assertTrue(failure.getMessage().contains("serialized input is truncated or corrupt"));
    assertFalse(Files.exists(output));
  }

  @Test
  void serializedInputByteLimitIsEnforcedBeforePublication(@TempDir Path temp) throws Exception {
    Path binary = serialized(new SafeTree("root", List.of()), temp);
    Path output = temp.resolve("model");

    var failure = assertThrows(IllegalArgumentException.class, () ->
        LegacyBlockingTreeImporterMain.importSerialized(binary, output,
            new ImportLimits(0, 64, 100, 1_000)));

    assertTrue(failure.getMessage().contains("import limits outside allowed range"));
    assertFalse(Files.exists(output));

    var byteFailure = assertThrows(DuckException.class, () ->
        LegacyBlockingTreeImporterMain.importSerialized(binary, output,
            new ImportLimits(1, 64, 100, 1_000)));
    assertTrue(byteFailure.getMessage().contains("byte limit exceeded"));
    assertFalse(Files.exists(output));
  }

  @Test
  void objectFilterDepthAndReferenceLimitsRejectBeforePublication(@TempDir Path temp) throws Exception {
    Path deep = serialized(new SafeTree("root", List.of(new SafeTree("child", List.of()))), temp);
    Path depthOutput = temp.resolve("depth-model");
    assertThrows(Exception.class, () -> LegacyBlockingTreeImporterMain.importSerialized(deep, depthOutput,
        new ImportLimits(1_000_000, 1, 100, 1_000)));
    assertFalse(Files.exists(depthOutput));

    Path many = serialized(new SafeTree("root", List.of(new SafeTree("a", List.of()),
        new SafeTree("b", List.of()), new SafeTree("c", List.of()))), temp);
    Path referencesOutput = temp.resolve("references-model");
    assertThrows(Exception.class, () -> LegacyBlockingTreeImporterMain.importSerialized(many, referencesOutput,
        new ImportLimits(1_000_000, 64, 2, 1_000)));
    assertFalse(Files.exists(referencesOutput));
  }

  @Test
  void failedReplacementPreservesPreviouslyPublishedModel(@TempDir Path temp) throws Exception {
    Path original = serialized(new SafeTree("original", List.of()), temp);
    Path output = temp.resolve("model");
    new ModelArtifactWriter(output).write(new ModelManifest("test", "test", ModelFormat.CURRENT,
        ModelType.BLOCKING_TREE.name(), List.of(), ""), new byte[] {1, 2, 3},
        new ImportProvenance(original.toString(), "test", "test", Instant.EPOCH, ""));
    byte[] manifestBefore = Files.readAllBytes(output.resolve("manifest.json"));
    byte[] payloadBefore = Files.readAllBytes(output.resolve("model.bin"));

    Path invalid = temp.resolve("invalid.ser");
    Files.writeString(invalid, "not a serialization stream");
    DuckException rejected = assertThrows(DuckException.class, () -> LegacyBlockingTreeImporterMain.importSerialized(
        invalid, output, ImportLimits.defaults()));
    assertTrue(rejected.getMessage().contains("truncated or corrupt"));

    assertTrue(Files.isDirectory(output));
    assertTrue(java.util.Arrays.equals(manifestBefore, Files.readAllBytes(output.resolve("manifest.json"))));
    assertTrue(java.util.Arrays.equals(payloadBefore, Files.readAllBytes(output.resolve("model.bin"))));
  }

  private static Path serialized(SafeTree tree, Path temp) throws Exception {
    Path file = temp.resolve("tree-" + System.nanoTime() + ".ser");
    try (var output = new java.io.ObjectOutputStream(Files.newOutputStream(file))) { output.writeObject(tree); }
    return file;
  }

  private static Path fixtureRoot() {
    return Path.of("..", "compat-zingg07-runtime", "src", "test", "resources", "external",
        "zingg-v07-model-48cb157").toAbsolutePath().normalize();
  }

  private static void verifyFixtureChecksum(Path fixtureRoot, Path serializedTree) throws Exception {
    String expected = null;
    for (String line : Files.readAllLines(fixtureRoot.resolve("SHA256SUMS"), StandardCharsets.UTF_8)) {
      String trimmed = line.trim();
      if (trimmed.endsWith("block/zingg.block")) {
        assertTrue(expected == null, "fixture checksum manifest must not duplicate the blocking artifact");
        expected = trimmed.substring(0, 64).toLowerCase(java.util.Locale.ROOT);
      }
    }
    assertTrue(expected != null, "fixture checksum manifest must list block/zingg.block");
    byte[] digest = MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(serializedTree));
    assertEquals(expected, HexFormat.of().formatHex(digest), "blocking fixture bytes must match pinned manifest");
  }

  private record SafeTree(String function, List<SafeTree> leafs) implements Serializable {
    private static final long serialVersionUID = 1L;
  }

  private static final class DescriptorInputStream extends ObjectInputStream {
    DescriptorInputStream(java.io.InputStream input) throws java.io.IOException { super(input); }
    private final java.util.ArrayList<ObjectStreamClass> observed = new java.util.ArrayList<>();
    @Override protected ObjectStreamClass readClassDescriptor() throws java.io.IOException, ClassNotFoundException {
      ObjectStreamClass descriptor = super.readClassDescriptor();
      observed.add(descriptor);
      return descriptor;
    }
    List<ObjectStreamClass> descriptors() { return List.copyOf(observed); }
  }
}
