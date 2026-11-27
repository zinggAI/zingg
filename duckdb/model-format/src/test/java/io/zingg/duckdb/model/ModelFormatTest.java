package io.zingg.duckdb.model;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.zingg.duckdb.api.DuckException;
import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.util.List;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.io.TempDir;

class ModelFormatTest {
  @Test
  void currentFormatIsAccepted() {
    assertDoesNotThrow(() -> ModelFormat.requireSupported(ModelFormat.CURRENT));
  }

  @Test
  void unknownFormatIsRejected() {
    assertThrows(DuckException.class, () -> ModelFormat.requireSupported("zingg-99.0-native"));
  }

  @Test
  void manifestCopiesFeaturesAndRejectsUnknownTypes() {
    var features = new java.util.ArrayList<>(List.of("name", "email"));
    var manifest = new ModelManifest("zingg-0.7.0", "0.7.0", ModelFormat.CURRENT,
        "CLASSIFIER", features, "abc");
    features.add("mutated");
    org.junit.jupiter.api.Assertions.assertEquals(List.of("name", "email"), manifest.features());
    assertThrows(IllegalArgumentException.class, () -> new ModelManifest(
        "zingg-0.7.0", "0.7.0", ModelFormat.CURRENT, "UNKNOWN", List.of(), "abc"));
  }

  @Test
  void writerAndReaderRoundTripACompleteClassifierArtifact(@TempDir Path temp) throws Exception {
    byte[] payload = "{\"kind\":\"linear-classifier\",\"weights\":[1.0]}".getBytes(StandardCharsets.UTF_8);
    var manifest = new ModelManifest("zingg-0.7.0", "0.7.0", ModelFormat.CURRENT,
        "CLASSIFIER", List.of("name"), "ignored-before-stamping");
    var provenance = new ImportProvenance("fixture/model", "0.7.0", "test-importer",
        Instant.parse("2026-01-01T00:00:00Z"), "");
    Path artifact = new ModelArtifactWriter(temp.resolve("model")).write(manifest, payload, provenance);
    var loaded = ModelReader.load(artifact, 1024);
    assertEquals("CLASSIFIER", loaded.manifest().modelType());
    org.junit.jupiter.api.Assertions.assertArrayEquals(payload, loaded.payload());
    assertEquals(NativeModelStore.sha256(payload), loaded.manifest().sha256());
  }

  @Test
  void readersCannotObserveTheGapBetweenModelReplacementRenames(@TempDir Path temp)
      throws Exception {
    Path output = temp.resolve("model");
    byte[] oldPayload = "{\"kind\":\"linear-classifier\",\"version\":\"old\"}".getBytes(StandardCharsets.UTF_8);
    byte[] newPayload = "{\"kind\":\"linear-classifier\",\"version\":\"new\"}".getBytes(StandardCharsets.UTF_8);
    new ModelArtifactWriter(output).write(classifierManifest(), oldPayload, provenance());

    var afterBackup = new CountDownLatch(1);
    var releaseBackup = new CountDownLatch(1);
    var failed = new AtomicBoolean();
    var executor = Executors.newFixedThreadPool(2);
    try {
      var mover = (ModelArtifactWriter.AtomicMover) (source, target) -> {
        Files.move(source, target, StandardCopyOption.ATOMIC_MOVE);
        if (source.equals(output)) {
          afterBackup.countDown();
          try {
            if (!releaseBackup.await(30, TimeUnit.SECONDS))
              throw new IOException("timed out waiting to release model publication");
          } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            throw new IOException("interrupted waiting to release model publication", interrupted);
          }
        }
      };
      var publisher = executor.submit(() -> new ModelArtifactWriter(output, mover, ModelFormatTest::deleteTree)
          .write(classifierManifest(), newPayload,
              new ImportProvenance("fixture/new-model", "new", "test-writer", Instant.EPOCH, "")));
      assertTrue(afterBackup.await(30, TimeUnit.SECONDS), "publisher did not reach the rename gap");

      var readers = executor.submit(() -> {
        for (int i = 0; i < 50; i++) {
          try {
            byte[] payload = ModelReader.load(output, 1024).payload();
            if (!java.util.Arrays.equals(payload, oldPayload)
                && !java.util.Arrays.equals(payload, newPayload)) failed.set(true);
          } catch (Exception failure) {
            failed.set(true);
          }
        }
      });
      Thread.sleep(150L);
      assertFalse(failed.get(), "a reader must wait rather than observe a missing model");
      releaseBackup.countDown();
      publisher.get(30, TimeUnit.SECONDS);
      readers.get(30, TimeUnit.SECONDS);
      assertFalse(failed.get(), "readers must observe one complete model generation");
      org.junit.jupiter.api.Assertions.assertArrayEquals(newPayload, ModelReader.load(output, 1024).payload());
    } finally {
      releaseBackup.countDown();
      executor.shutdownNow();
      assertTrue(executor.awaitTermination(30, TimeUnit.SECONDS));
    }
  }

  @Test
  void modelArtifactByteBudgetRejectsBeforeCreatingStageOrDestination(@TempDir Path temp)
      throws Exception {
    Path output = temp.resolve("oversized-model");
    byte[] payload = "{\"kind\":\"linear-classifier\",\"weights\":[1.0]}"
        .getBytes(StandardCharsets.UTF_8);

    IOException failure = assertThrows(IOException.class, () ->
        new ModelArtifactWriter(output).write(classifierManifest(), payload, provenance(), 1));

    assertTrue(failure.getMessage().startsWith("model artifact byte budget exceeded:"),
        failure.toString());
    assertFalse(Files.exists(output));
    try (var siblings = Files.list(temp)) {
      assertFalse(siblings.anyMatch(path -> path.getFileName().toString().startsWith(".model-artifact-")),
          "budget rejection must happen before allocating a staging directory");
    }
  }

  @Test
  void atomicMoveUnsupportedFailsClosedAndPreservesExistingArtifact(@TempDir Path temp)
      throws Exception {
    Path output = temp.resolve("model");
    Files.createDirectory(output);
    Files.writeString(output.resolve("sentinel"), "old-model");
    byte[] payload = "{\"kind\":\"linear-classifier\"}".getBytes(StandardCharsets.UTF_8);
    var mover = (ModelArtifactWriter.AtomicMover) (source, target) -> {
      throw new AtomicMoveNotSupportedException(source.toString(), target.toString(), "test provider");
    };
    var writer = new ModelArtifactWriter(output, mover, ModelFormatTest::deleteTree);

    assertThrows(IOException.class, () -> writer.write(classifierManifest(), payload, provenance()));
    assertEquals("old-model", Files.readString(output.resolve("sentinel")));
    try (var siblings = Files.list(temp)) {
      assertFalse(siblings.anyMatch(path -> path.getFileName().toString().startsWith(".model-artifact-")));
    }
  }

  @Test
  void writerRejectsSymbolicLinkAtArtifactDestination(@TempDir Path temp) throws Exception {
    Path actual = temp.resolve("actual-model");
    Files.createDirectory(actual);
    Files.writeString(actual.resolve("sentinel"), "must-not-be-followed");
    Path link = temp.resolve("model-link");
    try {
      Files.createSymbolicLink(link, actual.getFileName());
    } catch (IOException | UnsupportedOperationException unavailable) {
      Assumptions.assumeTrue(false, "symbolic links unavailable for this test runner: " + unavailable);
    }
    assertThrows(IOException.class, () -> new ModelArtifactWriter(link).write(classifierManifest(),
        "{\"kind\":\"linear-classifier\"}".getBytes(StandardCharsets.UTF_8), provenance()));
    assertTrue(Files.isSymbolicLink(link));
    assertEquals("must-not-be-followed", Files.readString(actual.resolve("sentinel")));
  }

  @Test
  void readerRejectsLinkedPayloadWithoutFollowingExternalSentinel(@TempDir Path temp) throws Exception {
    byte[] payload = "{\"kind\":\"linear-classifier\",\"weights\":[1.0]}".getBytes(StandardCharsets.UTF_8);
    Path artifact = new ModelArtifactWriter(temp.resolve("model")).write(classifierManifest(), payload, provenance());
    Path sentinel = temp.resolve("external-payload");
    byte[] outside = "must-not-be-read-or-modified".getBytes(StandardCharsets.UTF_8);
    Files.write(sentinel, outside);
    Files.delete(artifact.resolve("model.bin"));
    try {
      Files.createSymbolicLink(artifact.resolve("model.bin"), sentinel);
    } catch (IOException | UnsupportedOperationException unavailable) {
      Assumptions.assumeTrue(false, "symbolic links unavailable for this test runner: " + unavailable);
    }

    assertThrows(DuckException.class, () -> ModelReader.load(artifact, 1024));
    assertArrayEquals(outside, Files.readAllBytes(sentinel));
  }

  @Test
  void failedNewDirectoryMoveRestoresPreviousArtifactAtomically(@TempDir Path temp)
      throws Exception {
    Path output = temp.resolve("model");
    Files.createDirectory(output);
    Files.writeString(output.resolve("sentinel"), "old-model");
    int[] moves = {0};
    var mover = (ModelArtifactWriter.AtomicMover) (source, target) -> {
      if (++moves[0] == 2)
        throw new AtomicMoveNotSupportedException(source.toString(), target.toString(), "injected");
      Files.move(source, target, StandardCopyOption.ATOMIC_MOVE);
    };
    var writer = new ModelArtifactWriter(output, mover, ModelFormatTest::deleteTree);

    assertThrows(IOException.class, () -> writer.write(classifierManifest(),
        "{\"kind\":\"linear-classifier\"}".getBytes(StandardCharsets.UTF_8), provenance()));
    assertEquals(3, moves[0], "expected backup, failed publish, and atomic restore moves");
    assertEquals("old-model", Files.readString(output.resolve("sentinel")));
    try (var siblings = Files.list(temp)) {
      assertFalse(siblings.anyMatch(path -> path.getFileName().toString().contains(".previous-")));
    }
  }

  @Test
  void backupMoveThatCompletesThenThrowsRestoresPreviousArtifact(@TempDir Path temp)
      throws Exception {
    Path output = temp.resolve("model");
    Files.createDirectory(output);
    Files.writeString(output.resolve("sentinel"), "old-model");
    boolean[] failAfterBackupMove = {true};
    var mover = (ModelArtifactWriter.AtomicMover) (source, target) -> {
      Files.move(source, target, StandardCopyOption.ATOMIC_MOVE);
      if (failAfterBackupMove[0] && source.equals(output)) {
        failAfterBackupMove[0] = false;
        throw new IOException("injected error after completed backup move");
      }
    };
    var writer = new ModelArtifactWriter(output, mover, ModelFormatTest::deleteTree);

    IOException failure = assertThrows(IOException.class, () -> writer.write(classifierManifest(),
        "{\"kind\":\"linear-classifier\"}".getBytes(StandardCharsets.UTF_8), provenance()));
    assertEquals("injected error after completed backup move", failure.getMessage());
    assertEquals("old-model", Files.readString(output.resolve("sentinel")),
        "the previous artifact must be restored even when its completed move reports failure");
    try (var siblings = Files.list(temp)) {
      var paths = siblings.toList();
      assertFalse(paths.stream().anyMatch(path -> path.getFileName().toString().startsWith(".model-artifact-")),
          "the staged artifact must be removed");
      assertFalse(paths.stream().anyMatch(path -> path.getFileName().toString().contains(".previous-")),
          "the restored backup must not remain orphaned");
    }
  }

  @Test
  void cancellationAtBothPublicationMovesRestoresPreviousArtifactAndCleansStage(@TempDir Path temp)
      throws Exception {
    byte[] payload = "{\"kind\":\"linear-classifier\"}".getBytes(StandardCharsets.UTF_8);
    for (boolean cancelAfterBackupMove : List.of(true, false)) {
      Path output = temp.resolve(cancelAfterBackupMove ? "cancel-after-backup" : "cancel-after-publish");
      Files.createDirectory(output);
      Files.writeString(output.resolve("sentinel"), "old-model");
      var cancelled = new AtomicBoolean();
      var mover = (ModelArtifactWriter.AtomicMover) (source, target) -> {
        Files.move(source, target, StandardCopyOption.ATOMIC_MOVE);
        if (cancelAfterBackupMove && source.equals(output)) cancelled.set(true);
        if (!cancelAfterBackupMove && target.equals(output)
            && source.getFileName().toString().startsWith(".model-artifact-")) cancelled.set(true);
      };
      var writer = new ModelArtifactWriter(output, mover, ModelFormatTest::deleteTree,
          cancelled::get);

      IOException failure = assertThrows(IOException.class,
          () -> writer.write(classifierManifest(), payload, provenance()));
      assertTrue(failure.getMessage().contains("cancelled"), failure.toString());
      assertEquals("old-model", Files.readString(output.resolve("sentinel")),
          "cancellation before publication commit must restore the previous artifact");
      try (var siblings = Files.list(temp)) {
        assertFalse(siblings.anyMatch(path -> path.getFileName().toString().startsWith(".model-artifact-")),
            "cancellation must clean staged model directories");
      }
      try (var siblings = Files.list(temp)) {
        assertFalse(siblings.anyMatch(path -> path.getFileName().toString().contains(".previous-")),
            "successful rollback must not leave a backup orphan");
      }
    }
  }

  @Test
  void publishedChecksumFailureRollsBackToPreviousArtifact(@TempDir Path temp) throws Exception {
    Path output = temp.resolve("model");
    Files.createDirectory(output);
    Files.writeString(output.resolve("sentinel"), "old-model");
    int[] moves = {0};
    var mover = (ModelArtifactWriter.AtomicMover) (source, target) -> {
      Files.move(source, target, StandardCopyOption.ATOMIC_MOVE);
      if (++moves[0] == 2) Files.writeString(target.resolve("model.bin"), "tampered");
    };
    var writer = new ModelArtifactWriter(output, mover, ModelFormatTest::deleteTree);

    IOException error = assertThrows(IOException.class, () -> writer.write(classifierManifest(),
        "{\"kind\":\"linear-classifier\"}".getBytes(StandardCharsets.UTF_8), provenance()));
    assertTrue(error.getMessage().contains("checksum"));
    assertEquals("old-model", Files.readString(output.resolve("sentinel")));
    try (var siblings = Files.list(temp)) {
      assertFalse(siblings.anyMatch(path -> path.getFileName().toString().contains(".previous-")));
    }
  }

  @Test
  void backupCleanupFailureKeepsVerifiedPublishedArtifactAndRecoveryCopy(@TempDir Path temp)
      throws Exception {
    Path output = temp.resolve("model");
    Files.createDirectory(output);
    Files.writeString(output.resolve("sentinel"), "old-model");
    var deleter = (ModelArtifactWriter.TreeDeleter) path -> {
      if (path.getFileName().toString().startsWith(".model.previous-"))
        throw new IOException("injected backup cleanup failure");
      deleteTree(path);
    };
    var writer = new ModelArtifactWriter(output,
        (source, target) -> Files.move(source, target, StandardCopyOption.ATOMIC_MOVE), deleter);

    Path published = writer.write(classifierManifest(),
        "{\"kind\":\"linear-classifier\"}".getBytes(StandardCharsets.UTF_8), provenance());
    assertEquals(output, published);
    assertEquals("CLASSIFIER", ModelReader.load(output, 1024).manifest().modelType());
    try (var siblings = Files.list(temp)) {
      Path backup = siblings.filter(path -> path.getFileName().toString().contains(".previous-") )
          .findFirst().orElseThrow();
      assertEquals("old-model", Files.readString(backup.resolve("sentinel")));
    }
  }

  @Test
  void writerEscapesEveryJsonControlCharacterInProvenance(@TempDir Path temp) throws Exception {
    byte[] payload = "{\"kind\":\"linear-classifier\",\"weights\":[1.0]}".getBytes(StandardCharsets.UTF_8);
    var manifest = new ModelManifest("zingg-0.7.0", "0.7.0", ModelFormat.CURRENT,
        "CLASSIFIER", List.of(), "");
    StringBuilder controls = new StringBuilder();
    for (int value = 0; value < 0x20; value++) controls.append((char) value);
    var provenance = new ImportProvenance("quote\" slash\\ controls" + controls + " snowman \u2603",
        "version\nnext", "importer\tname", Instant.parse("2026-01-01T00:00:00Z"), "");

    Path artifact = new ModelArtifactWriter(temp.resolve("escaped-model")).write(manifest, payload, provenance);
    String json = Files.readString(artifact.resolve("provenance.json"), StandardCharsets.UTF_8);

    assertTrue(json.contains("quote\\\" slash\\\\ controls"), json);
    for (int value = 0; value < 0x20; value++) {
      String encoded = switch (value) {
        case '\b' -> "\\b";
        case '\f' -> "\\f";
        case '\n' -> "\\n";
        case '\r' -> "\\r";
        case '\t' -> "\\t";
        default -> String.format(java.util.Locale.ROOT, "\\u%04x", value);
      };
      assertTrue(json.contains(encoded), "missing JSON escape for U+" + String.format("%04X", value));
    }
    assertTrue(json.contains("\"sourceVersion\":\"version\\nnext\""), json);
    assertTrue(json.contains("\"importerVersion\":\"importer\\tname\""), json);
    assertTrue(json.chars().noneMatch(character -> character < 0x20), "JSON output must not contain raw control characters");
    assertEquals(provenance.sourcePath(), ModelArtifactJson.parseObject(json).get("sourcePath"));
    assertEquals(provenance.sourceVersion(), ModelArtifactJson.parseObject(json).get("sourceVersion"));
    assertEquals(NativeModelStore.sha256(payload), ModelReader.load(artifact, 1024).manifest().sha256());
  }

  @Test
  void readerRejectsMalformedDuplicateAndInvalidUnicodeProvenance(@TempDir Path temp) throws Exception {
    byte[] payload = "{\"kind\":\"linear-classifier\",\"weights\":[1.0]}".getBytes(StandardCharsets.UTF_8);
    var manifest = new ModelManifest("zingg-0.7.0", "0.7.0", ModelFormat.CURRENT,
        "CLASSIFIER", List.of(), "");
    Path artifact = new ModelArtifactWriter(temp.resolve("malformed-provenance-model")).write(manifest, payload,
        new ImportProvenance("fixture/model", "0.7.0", "test-importer", Instant.EPOCH, ""));
    String valid = Files.readString(artifact.resolve("provenance.json"), StandardCharsets.UTF_8);
    String[] malformed = {
        valid.substring(0, valid.length() - 1) + ",",
        valid.replace("\"sourceVersion\":", "\"sourcePath\":\"duplicate\",\"sourceVersion\":"),
        valid.replace("\"sourcePath\":\"fixture/model\"", "\"sourcePath\":\"fixture\\qmodel\""),
        valid.replace("\"sourcePath\":\"fixture/model\"", "\"sourcePath\":\"fixture\nmodel\""),
        valid.replace("\"sourcePath\":\"fixture/model\"", "\"sourcePath\":\"fixture\\uD800\"")
    };
    for (String invalid : malformed) {
      Files.writeString(artifact.resolve("provenance.json"), invalid, StandardCharsets.UTF_8);
      assertThrows(DuckException.class, () -> ModelReader.load(artifact, 1024));
    }
    Files.write(artifact.resolve("provenance.json"), new byte[] {'{', (byte) 0xc3, '(', '}'});
    assertThrows(java.io.IOException.class, () -> ModelReader.load(artifact, 1024));
  }

  @Test
  void writerRejectsUnpairedUtf16WithoutPublishingPartialModel(@TempDir Path temp) {
    byte[] payload = "{\"kind\":\"linear-classifier\",\"weights\":[1.0]}".getBytes(StandardCharsets.UTF_8);
    var manifest = new ModelManifest("zingg-0.7.0", "0.7.0", ModelFormat.CURRENT,
        "CLASSIFIER", List.of(), "");
    Path output = temp.resolve("invalid-unicode-model");
    var provenance = new ImportProvenance("bad-" + (char) 0xD800 + "-path", "0.7.0", "test-importer", Instant.EPOCH, "");

    assertThrows(IllegalArgumentException.class,
        () -> new ModelArtifactWriter(output).write(manifest, payload, provenance));
    assertFalse(Files.exists(output), "invalid provenance must not publish a partial artifact");
  }

  @Test
  void manifestFeatureNamesRoundTripJsonControlsAndUnicode(@TempDir Path temp) throws Exception {
    byte[] payload = "{\"kind\":\"linear-classifier\",\"weights\":[1.0]}".getBytes(StandardCharsets.UTF_8);
    StringBuilder feature = new StringBuilder("quoted\" slash\\ ");
    for (int value = 0; value < 0x20; value++) feature.append((char) value);
    feature.append(" snowman \u2603 supplementary 🦆");
    var manifest = new ModelManifest("zingg-0.7.0", "0.7.0", ModelFormat.CURRENT,
        "CLASSIFIER", List.of(feature.toString()), "");
    Path artifact = new ModelArtifactWriter(temp.resolve("escaped-feature-model")).write(manifest, payload,
        new ImportProvenance("fixture/model", "0.7.0", "test-importer", Instant.EPOCH, ""));
    String json = Files.readString(artifact.resolve("manifest.json"), StandardCharsets.UTF_8);
    assertTrue(json.chars().noneMatch(character -> character < 0x20), "manifest must not contain raw JSON controls");
    var loaded = ModelReader.load(artifact, 1024);
    assertEquals(List.of(feature.toString()), loaded.manifest().features());
  }

  @Test
  void readerRejectsMalformedDuplicateAndUnknownManifestJson(@TempDir Path temp) throws Exception {
    byte[] payload = "{\"kind\":\"linear-classifier\",\"weights\":[1.0]}".getBytes(StandardCharsets.UTF_8);
    var manifest = new ModelManifest("zingg-0.7.0", "0.7.0", ModelFormat.CURRENT,
        "CLASSIFIER", List.of("name"), "");
    Path artifact = new ModelArtifactWriter(temp.resolve("malformed-manifest-model")).write(manifest, payload,
        new ImportProvenance("fixture/model", "0.7.0", "test-importer", Instant.EPOCH, ""));
    String valid = Files.readString(artifact.resolve("manifest.json"), StandardCharsets.UTF_8);
    String[] malformed = {
        valid.substring(0, valid.length() - 1) + ",",
        valid.replace("\"zinggVersion\":", "\"profile\":\"duplicate\",\"zinggVersion\":"),
        valid.replace("\"profile\":\"zingg-0.7.0\"", "\"profile\":\"bad\\q\""),
        valid.replace("\"features\":[\"name\"]", "\"features\":\"name\""),
        valid.replace("\"sha256\":", "\"unexpected\":\"field\",\"sha256\":")
    };
    for (String invalid : malformed) {
      Files.writeString(artifact.resolve("manifest.json"), invalid, StandardCharsets.UTF_8);
      assertThrows(DuckException.class, () -> ModelReader.load(artifact, 1024));
    }
  }

  @Test
  void readerRejectsCorruptPayloadAndOversizedArtifact(@TempDir Path temp) throws Exception {
    byte[] payload = "{\"kind\":\"linear-classifier\"}".getBytes(StandardCharsets.UTF_8);
    var manifest = new ModelManifest("zingg-0.7.0", "0.7.0", ModelFormat.CURRENT,
        "CLASSIFIER", List.of(), "");
    Path artifact = new ModelArtifactWriter(temp.resolve("model")).write(manifest, payload,
        new ImportProvenance("fixture/model", "0.7.0", "test-importer", Instant.now(), ""));
    Files.write(artifact.resolve("model.bin"), "corrupt".getBytes(StandardCharsets.UTF_8));
    assertThrows(DuckException.class, () -> ModelReader.load(artifact, 1024));
    assertThrows(DuckException.class, () -> ModelReader.load(artifact, 1));
  }

  @Test
  void backendBlockingHistogramIsDistinctFromStrictZinggTree(@TempDir Path temp) throws Exception {
    byte[] payload = "{\"kind\":\"duckdb-blocking-histogram\",\"blockingFrequencies\":[]}".getBytes(StandardCharsets.UTF_8);
    var manifest = new ModelManifest("duckdb-native-0.1", "0.1.0", ModelFormat.CURRENT,
        "BACKEND_BLOCKING_HISTOGRAM", List.of("block"), "");
    var artifact = new ModelArtifactWriter(temp.resolve("histogram")).write(manifest, payload,
        new ImportProvenance("fixture/histogram", "0.1.0", "native-trainer", Instant.now(), ""));
    assertEquals("BACKEND_BLOCKING_HISTOGRAM", ModelReader.load(artifact, 1024).manifest().modelType());
  }

  @Test
  void readerRejectsMixedNativeAndStrictIdentity(@TempDir Path temp) throws Exception {
    byte[] payload = "{\"kind\":\"duckdb-blocking-histogram\",\"blockingFrequencies\":[]}".getBytes(StandardCharsets.UTF_8);
    var manifest = new ModelManifest("zingg-0.7.0", "0.7.0", ModelFormat.CURRENT,
        "BACKEND_BLOCKING_HISTOGRAM", List.of(), "");
    var artifact = new ModelArtifactWriter(temp.resolve("mixed")).write(manifest, payload,
        new ImportProvenance("fixture/mixed", "0.7.0", "test-importer", Instant.now(), ""));
    assertThrows(DuckException.class, () -> ModelReader.load(artifact, 1024));
  }

  private static ModelManifest classifierManifest() {
    return new ModelManifest("zingg-0.7.0", "0.7.0", ModelFormat.CURRENT,
        "CLASSIFIER", List.of("feature"), "");
  }

  private static ImportProvenance provenance() {
    return new ImportProvenance("fixture/model", "0.7.0", "test-writer", Instant.EPOCH, "");
  }

  private static void deleteTree(Path path) throws IOException {
    if (!Files.exists(path, java.nio.file.LinkOption.NOFOLLOW_LINKS)) return;
    try (var paths = Files.walk(path)) {
      for (Path item : paths.sorted(java.util.Comparator.reverseOrder()).toList())
        Files.deleteIfExists(item);
    }
  }
}
