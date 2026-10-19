package io.zingg.duckdb.compat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.zingg.duckdb.api.DuckException;
import io.zingg.duckdb.api.RuntimeConfig;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.security.MessageDigest;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.io.TempDir;

class PersistentLabelDecisionProviderTest {
  @Test
  void persistentLabelFilesArePrivateOnPosixFilesystems(@TempDir Path temp) throws IOException {
    var provider = new PersistentLabelDecisionProvider(temp);
    provider.enqueue(new LabelDecisionProvider.PendingLabel("left", "right", "sensitive-left", "sensitive-right"));

    for (Path path : List.of(temp.resolve("labels.lock"), temp.resolve("labels.bin"))) {
      Set<PosixFilePermission> permissions;
      try { permissions = Files.getPosixFilePermissions(path); }
      catch (UnsupportedOperationException unsupported) { Assumptions.abort("POSIX permissions are unavailable"); return; }
      assertEquals(Set.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE), permissions,
          "persistent label data must not be readable or writable by group/other users: " + path.getFileName());
    }
  }

  @Test
  void providerFailsClosedWhenExistingPersistentFilesAreGroupAccessible(@TempDir Path temp) throws IOException {
    var provider = new PersistentLabelDecisionProvider(temp);
    provider.enqueue(new LabelDecisionProvider.PendingLabel("left", "right", "sensitive-left", "sensitive-right"));
    Set<PosixFilePermission> permissive = Set.of(PosixFilePermission.OWNER_READ,
        PosixFilePermission.OWNER_WRITE, PosixFilePermission.GROUP_READ);
    try {
      Files.setPosixFilePermissions(temp.resolve("labels.lock"), permissive);
      assertThrows(DuckException.class, provider::acceptedDecisions,
          "an existing group-readable lock must fail closed rather than be silently trusted");
      Files.setPosixFilePermissions(temp.resolve("labels.lock"), Set.of(
          PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE));
      assertTrue(provider.acceptedDecisions().isEmpty(), "restoring 0600 must restore provider access");

      Files.setPosixFilePermissions(temp.resolve("labels.bin"), permissive);
      assertThrows(DuckException.class, provider::acceptedDecisions,
          "an existing group-readable snapshot must fail closed rather than be silently trusted");
      Files.setPosixFilePermissions(temp.resolve("labels.bin"), Set.of(
          PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE));
      assertTrue(provider.acceptedDecisions().isEmpty(), "restoring 0600 must restore snapshot access");
    } catch (UnsupportedOperationException unsupported) {
      Assumptions.abort("POSIX permissions are unavailable");
    } finally {
      try {
        var privateMode = Set.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE);
        Files.setPosixFilePermissions(temp.resolve("labels.lock"), privateMode);
        Files.setPosixFilePermissions(temp.resolve("labels.bin"), privateMode);
      } catch (UnsupportedOperationException unsupported) {
        // Windows test files do not expose POSIX modes.
      }
    }
  }

  @Test
  void acceptedLabelsAndReplaySurviveNewRuntime(@TempDir Path temp) {
    Path labels = temp.resolve("labels");
    var first = new LabelDecisionProvider.LabelDecision("a", "b", LabelDecisionProvider.Decision.MATCH, "human");
    var request = new LabelDecisionProvider.ApplyLabelsRequest("initial", "v1", "stable-key", List.of(first));
    var config = new RuntimeConfig("jdbc:duckdb:" + temp.resolve("runtime.duckdb"), 1, 0, null, 1);
    try (var runtime = runtime(config, labels)) {
      assertEquals(1, runtime.labels().applyLabels(request).applied());
    }
    try (var runtime = runtime(config, labels)) {
      var replay = runtime.labels().applyLabels(new LabelDecisionProvider.ApplyLabelsRequest(
          "retry", "v1", "stable-key", List.of(first)));
      assertTrue(replay.replay());
      assertEquals("retry", replay.requestId());
      assertEquals(List.of(first), runtime.labels().acceptedDecisions());
      assertThrows(DuckException.class, () -> runtime.labels().applyLabels(
          new LabelDecisionProvider.ApplyLabelsRequest("changed", "v1", "stable-key",
              List.of(new LabelDecisionProvider.LabelDecision("a", "b",
                  LabelDecisionProvider.Decision.NON_MATCH, "human")))));
      var conflict = runtime.labels().applyLabels(new LabelDecisionProvider.ApplyLabelsRequest(
          "conflict", "v1", "new-key", List.of(new LabelDecisionProvider.LabelDecision(
              "a", "b", LabelDecisionProvider.Decision.NON_MATCH, "human"))));
      assertFalse(conflict.replay());
      assertEquals(0, conflict.applied());
      assertEquals("CONFLICT", conflict.rejections().get(0).code());
    }
  }

  @Test
  void corruptSnapshotIsRejectedInsteadOfSilentlyStartingEmpty(@TempDir Path temp) throws Exception {
    Path labels = temp.resolve("labels");
    var provider = new PersistentLabelDecisionProvider(labels);
    provider.applyLabels(new LabelDecisionProvider.ApplyLabelsRequest("first", "v1", "first",
        List.of(new LabelDecisionProvider.LabelDecision("a", "b", LabelDecisionProvider.Decision.MATCH, "human"))));
    Path snapshot = labels.resolve("labels.bin");
    byte[] bytes = Files.readAllBytes(snapshot);
    bytes[bytes.length - 1] ^= 1;
    writeSnapshotBytes(snapshot, bytes);
    DuckException failure = assertThrows(DuckException.class, () -> new PersistentLabelDecisionProvider(labels));
    assertTrue(failure.getMessage().contains("checksum mismatch"));
  }

  @Test
  void everyTruncatedSnapshotPrefixAndTrailingByteAreRejected(@TempDir Path temp) throws Exception {
    Path source = temp.resolve("source");
    var provider = new PersistentLabelDecisionProvider(source);
    provider.applyLabels(new LabelDecisionProvider.ApplyLabelsRequest("request", "v1", "key",
        List.of(new LabelDecisionProvider.LabelDecision(
            "left", "right", LabelDecisionProvider.Decision.MATCH, "human"))));
    byte[] valid = Files.readAllBytes(source.resolve("labels.bin"));

    for (int length = 0; length < valid.length; length++) {
      Path labels = temp.resolve("truncated-" + length);
      Files.createDirectories(labels);
      writeSnapshotBytes(labels.resolve("labels.bin"), java.util.Arrays.copyOf(valid, length));
      assertThrows(DuckException.class, () -> new PersistentLabelDecisionProvider(labels),
          "accepted truncated snapshot prefix of " + length + " bytes");
    }

    Path trailing = temp.resolve("trailing");
    Files.createDirectories(trailing);
    byte[] extended = java.util.Arrays.copyOf(valid, valid.length + 1);
    extended[extended.length - 1] = 0x5a;
    writeSnapshotBytes(trailing.resolve("labels.bin"), extended);
    assertThrows(DuckException.class, () -> new PersistentLabelDecisionProvider(trailing),
        "accepted a snapshot with an unframed trailing byte");
  }

  @Test
  void checksummedSnapshotRejectsNonCanonicalBooleanFlags(@TempDir Path temp) throws Exception {
    Path labels = temp.resolve("labels");
    Files.createDirectories(labels);
    var payload = new ByteArrayOutputStream();
    try (var data = new DataOutputStream(payload)) {
      data.writeInt(0); // applied requests
      data.writeInt(1); // pending labels
      data.writeUTF("left");
      data.writeUTF("right");
      data.writeByte(2); // invalid has-left-payload flag
      data.writeBoolean(false);
      data.writeInt(0); // deliveries
      data.writeInt(0); // enqueue receipts
    }
    byte[] content = payload.toByteArray();
    var snapshot = new ByteArrayOutputStream();
    try (var data = new DataOutputStream(snapshot)) {
      data.writeInt(0x5a4c4433);
      data.writeInt(content.length);
      data.write(content);
      data.write(MessageDigest.getInstance("SHA-256").digest(content));
    }
    writeSnapshotBytes(labels.resolve("labels.bin"), snapshot.toByteArray());

    DuckException failure = assertThrows(DuckException.class,
        () -> new PersistentLabelDecisionProvider(labels));
    assertTrue(failure.getMessage().contains("boolean is invalid"));
  }

  @Test
  void checksummedSnapshotRejectsDuplicatePendingLabelReceipts(@TempDir Path temp) throws Exception {
    for (String kind : List.of("delivery", "enqueue")) {
      Path labels = temp.resolve(kind);
      Files.createDirectories(labels);
      var payload = new ByteArrayOutputStream();
      try (var data = new DataOutputStream(payload)) {
        data.writeInt(0); // applied requests
        data.writeInt(0); // pending labels
        data.writeInt(kind.equals("delivery") ? 2 : 0);
        if (kind.equals("delivery")) {
          writeDeliveryReceipt(data, "v1", "same-key");
          writeDeliveryReceipt(data, "v1", "same-key");
        }
        data.writeInt(kind.equals("enqueue") ? 2 : 0);
        if (kind.equals("enqueue")) {
          writeEnqueueReceipt(data, "v1", "same-key", "left-1", "right-1");
          writeEnqueueReceipt(data, "v1", "same-key", "left-2", "right-2");
        }
      }
      writeChecksummedV3Snapshot(labels.resolve("labels.bin"), payload.toByteArray());

      DuckException failure = assertThrows(DuckException.class,
          () -> new PersistentLabelDecisionProvider(labels));
      assertTrue(failure.getMessage().contains("receipt is invalid or duplicated"), kind);
    }
  }

  @Test
  void checksummedSnapshotsRejectBlankPendingReceiptFields(@TempDir Path temp) throws Exception {
    for (String kind : List.of("delivery-schema", "delivery-key", "enqueue-schema", "enqueue-key")) {
      Path labels = temp.resolve(kind);
      Files.createDirectories(labels);
      var payload = new ByteArrayOutputStream();
      try (var data = new DataOutputStream(payload)) {
        data.writeInt(0); // applied requests
        data.writeInt(0); // pending labels
        data.writeInt(kind.startsWith("delivery") ? 1 : 0);
        if (kind.startsWith("delivery")) {
          writeDeliveryReceipt(data, kind.endsWith("schema") ? "" : "v1",
              kind.endsWith("key") ? "" : "delivery-key");
        }
        data.writeInt(kind.startsWith("enqueue") ? 1 : 0);
        if (kind.startsWith("enqueue")) {
          writeEnqueueReceipt(data, kind.endsWith("schema") ? "" : "v1",
              kind.endsWith("key") ? "" : "enqueue-key", "left", "right");
        }
      }
      writeChecksummedV3Snapshot(labels.resolve("labels.bin"), payload.toByteArray());

      DuckException failure = assertThrows(DuckException.class,
          () -> new PersistentLabelDecisionProvider(labels));
      assertTrue(failure.getMessage().contains("receipt is invalid or duplicated"), kind);
    }
  }

  @Test
  void checksummedSnapshotsRejectDuplicateAppliedRequestKeysInEveryFormat(@TempDir Path temp)
      throws Exception {
    for (int magic : List.of(0x5a4c4431, 0x5a4c4432, 0x5a4c4433)) {
      Path labels = temp.resolve(Integer.toHexString(magic));
      Files.createDirectories(labels);
      var payload = new ByteArrayOutputStream();
      try (var data = new DataOutputStream(payload)) {
        data.writeInt(2);
        writeAppliedRequest(data, "first-request", "reused-key");
        writeAppliedRequest(data, "second-request", "reused-key");
        if (magic != 0x5a4c4431) {
          data.writeInt(0); // pending labels
          data.writeInt(0); // deliveries
          if (magic == 0x5a4c4433) data.writeInt(0); // enqueue receipts
        }
      }
      writeChecksummedSnapshot(labels.resolve("labels.bin"), magic, payload.toByteArray());

      DuckException failure = assertThrows(DuckException.class,
          () -> new PersistentLabelDecisionProvider(labels));
      assertTrue(failure.getMessage().contains("applied request key is duplicated"),
          Integer.toHexString(magic));
    }
  }

  private static void writeAppliedRequest(DataOutputStream data, String requestId, String key)
      throws IOException {
    data.writeUTF(requestId);
    data.writeUTF("v1");
    data.writeUTF(key);
    data.writeInt(0);
  }

  private static void writeDeliveryReceipt(DataOutputStream data, String schema, String key) throws IOException {
    data.writeUTF(schema);
    data.writeUTF(key);
    data.writeInt(1);
    data.writeInt(0);
    data.writeBoolean(false);
  }

  private static void writeEnqueueReceipt(DataOutputStream data, String schema, String key,
      String left, String right) throws IOException {
    data.writeUTF(schema);
    data.writeUTF(key);
    data.writeUTF(left);
    data.writeUTF(right);
    data.writeBoolean(false);
    data.writeBoolean(false);
  }

  private static void writeChecksummedV3Snapshot(Path path, byte[] payload) throws Exception {
    writeChecksummedSnapshot(path, 0x5a4c4433, payload);
  }

  private static void writeChecksummedSnapshot(Path path, int magic, byte[] payload) throws Exception {
    var snapshot = new ByteArrayOutputStream();
    try (var data = new DataOutputStream(snapshot)) {
      data.writeInt(magic);
      data.writeInt(payload.length);
      data.write(payload);
      data.write(MessageDigest.getInstance("SHA-256").digest(payload));
    }
    writeSnapshotBytes(path, snapshot.toByteArray());
  }

  private static void writeSnapshotBytes(Path path, byte[] bytes) throws IOException {
    Files.write(path, bytes);
    try {
      Files.setPosixFilePermissions(path,
          java.nio.file.attribute.PosixFilePermissions.fromString("rw-------"));
    } catch (UnsupportedOperationException unsupported) {
      // The parser fixtures retain the host provider's normal ACL behavior on Windows.
    }
  }

  @Test
  void twoInstancesDoNotOverwriteEachOthersAppliedRequests(@TempDir Path temp) {
    Path labels = temp.resolve("labels");
    var one = new PersistentLabelDecisionProvider(labels);
    var two = new PersistentLabelDecisionProvider(labels);
    var first = new LabelDecisionProvider.ApplyLabelsRequest("one", "v1", "key-one",
        List.of(new LabelDecisionProvider.LabelDecision("a", "b", LabelDecisionProvider.Decision.MATCH, "human")));
    var second = new LabelDecisionProvider.ApplyLabelsRequest("two", "v1", "key-two",
        List.of(new LabelDecisionProvider.LabelDecision("c", "d", LabelDecisionProvider.Decision.NON_MATCH, "human")));
    assertEquals(1, one.applyLabels(first).applied());
    assertEquals(1, two.applyLabels(second).applied());
    var reopened = new PersistentLabelDecisionProvider(labels);
    assertTrue(reopened.applyLabels(first).replay());
    assertTrue(reopened.applyLabels(second).replay());
  }

  @Test
  void pendingQueueAndIdempotentDeliveriesSurviveRestart(@TempDir Path temp) {
    Path labels = temp.resolve("labels");
    var first = new LabelDecisionProvider.PendingLabel("a", "b", "left", null);
    var second = new LabelDecisionProvider.PendingLabel("c", "d", null, "right");
    var provider = new PersistentLabelDecisionProvider(labels);
    provider.enqueue(first);
    provider.enqueue(second);
    var reopened = new PersistentLabelDecisionProvider(labels);
    var batch = reopened.getPendingLabels(new LabelDecisionProvider.LabelRequest("first", "v1", "delivery-a", 1));
    assertEquals(List.of(first), batch.labels());
    assertTrue(batch.hasMore());
    var afterDelivery = new PersistentLabelDecisionProvider(labels);
    var replay = afterDelivery.getPendingLabels(new LabelDecisionProvider.LabelRequest("retry", "v1", "delivery-a", 1));
    assertEquals("retry", replay.requestId());
    assertEquals(batch.labels(), replay.labels());
    assertTrue(replay.hasMore());
    assertThrows(DuckException.class, () -> afterDelivery.getPendingLabels(
        new LabelDecisionProvider.LabelRequest("wrong", "v1", "delivery-a", 2)));
    var last = afterDelivery.getPendingLabels(new LabelDecisionProvider.LabelRequest("last", "v1", "delivery-b", 1));
    assertEquals(List.of(second), last.labels());
    assertFalse(last.hasMore());
    var finalProvider = new PersistentLabelDecisionProvider(labels);
    assertEquals(last.labels(), finalProvider.getPendingLabels(
        new LabelDecisionProvider.LabelRequest("retry-last", "v1", "delivery-b", 1)).labels());
    assertTrue(finalProvider.getPendingLabels(
        new LabelDecisionProvider.LabelRequest("empty", "v1", "delivery-c", 1)).labels().isEmpty());
  }

  @Test
  void appliedDecisionsAndPendingQueueArePreservedTogether(@TempDir Path temp) {
    Path labels = temp.resolve("labels");
    var pending = new LabelDecisionProvider.PendingLabel("c", "d", "{}", "{}");
    var decision = new LabelDecisionProvider.LabelDecision("a", "b", LabelDecisionProvider.Decision.MATCH, "human");
    var provider = new PersistentLabelDecisionProvider(labels);
    provider.enqueue(pending);
    provider.applyLabels(new LabelDecisionProvider.ApplyLabelsRequest("apply", "v1", "apply-key", List.of(decision)));
    var reopened = new PersistentLabelDecisionProvider(labels);
    assertEquals(List.of(decision), reopened.acceptedDecisions());
    assertEquals(List.of(pending), reopened.getPendingLabels(
        new LabelDecisionProvider.LabelRequest("get", "v1", "get-key", 10)).labels());
    assertEquals(List.of(decision), new PersistentLabelDecisionProvider(labels).acceptedDecisions());
  }

  @Test
  void appliedLabelMutationsUseAppendOnlyJournalUntilQueueSnapshotCompaction(@TempDir Path temp)
      throws Exception {
    Path labels = temp.resolve("journal-labels");
    var provider = new PersistentLabelDecisionProvider(labels);
    provider.applyLabels(new LabelDecisionProvider.ApplyLabelsRequest("one", "v1", "one",
        List.of(new LabelDecisionProvider.LabelDecision("a", "b",
            LabelDecisionProvider.Decision.MATCH, "human"))));
    byte[] bootstrap = Files.readAllBytes(labels.resolve("labels.bin"));
    provider.applyLabels(new LabelDecisionProvider.ApplyLabelsRequest("two", "v1", "two",
        List.of(new LabelDecisionProvider.LabelDecision("c", "d",
            LabelDecisionProvider.Decision.NON_MATCH, "human"))));

    assertArrayEquals(bootstrap, Files.readAllBytes(labels.resolve("labels.bin")),
        "applied-label append must not rewrite the cumulative snapshot");
    assertTrue(Files.size(labels.resolve("labels.applied.log")) > 0,
        "applied-label delta must be durable in the append-only journal");
    assertEquals(2, new PersistentLabelDecisionProvider(labels).acceptedDecisions().size());

    provider.enqueue(new LabelDecisionProvider.PendingLabel("pending-left", "pending-right", null, null));
    assertEquals(0, Files.size(labels.resolve("labels.applied.log")),
        "a queue snapshot is a safe compaction point for applied deltas");
    assertEquals(2, new PersistentLabelDecisionProvider(labels).acceptedDecisions().size());
  }

  @Test
  void corruptJournalHeaderFailsClosedWithoutTruncatingTheValidPrefix(@TempDir Path temp)
      throws Exception {
    Path labels = temp.resolve("corrupt-journal");
    var provider = new PersistentLabelDecisionProvider(labels);
    provider.applyLabels(new LabelDecisionProvider.ApplyLabelsRequest("one", "v1", "one",
        List.of(new LabelDecisionProvider.LabelDecision("a", "b",
            LabelDecisionProvider.Decision.MATCH, "human"))));
    provider.applyLabels(new LabelDecisionProvider.ApplyLabelsRequest("two", "v1", "two",
        List.of(new LabelDecisionProvider.LabelDecision("c", "d",
            LabelDecisionProvider.Decision.NON_MATCH, "human"))));
    Path journal = labels.resolve("labels.applied.log");
    byte[] valid = Files.readAllBytes(journal);
    byte[] corruptHeader = java.nio.ByteBuffer.allocate(8)
        .putInt(0x12345678).putInt(0).array();
    Files.write(journal, corruptHeader, java.nio.file.StandardOpenOption.APPEND);
    byte[] corrupt = Files.readAllBytes(journal);

    DuckException failure = assertThrows(DuckException.class,
        () -> new PersistentLabelDecisionProvider(labels));
    assertTrue(failure.getMessage().contains("journal header is corrupt"), failure.toString());
    assertArrayEquals(corrupt, Files.readAllBytes(journal),
        "a corrupt header must not be silently truncated during recovery");
    assertEquals(valid.length + corruptHeader.length, corrupt.length);
  }

  @Test
  void distinctNulBearingPairKeysSurviveSnapshotAndRecovery(@TempDir Path temp) {
    Path labels = temp.resolve("nul-pair-labels");
    var first = new LabelDecisionProvider.LabelDecision("a\u0000b", "c",
        LabelDecisionProvider.Decision.MATCH, "human");
    var second = new LabelDecisionProvider.LabelDecision("a", "b\u0000c",
        LabelDecisionProvider.Decision.NON_MATCH, "human");
    var provider = new PersistentLabelDecisionProvider(labels);

    var result = provider.applyLabels(new LabelDecisionProvider.ApplyLabelsRequest(
        "nul-pairs", "v1", "nul-pairs", List.of(first, second)));

    assertEquals(2, result.applied());
    assertEquals(List.of(first, second), new PersistentLabelDecisionProvider(labels).acceptedDecisions());
  }

  @Test
  void versionOneSnapshotIsReadAndMigratedOnNextWrite(@TempDir Path temp) throws Exception {
    Path labels = temp.resolve("labels");
    Files.createDirectories(labels);
    var payload = new ByteArrayOutputStream();
    try (var data = new DataOutputStream(payload)) {
      data.writeInt(1);
      data.writeUTF("original"); data.writeUTF("v1"); data.writeUTF("old-key");
      data.writeInt(1);
      data.writeUTF("a"); data.writeUTF("b"); data.writeUTF("MATCH"); data.writeUTF("human");
    }
    var snapshot = new ByteArrayOutputStream();
    try (var data = new DataOutputStream(snapshot)) {
      data.writeInt(0x5a4c4431);
      data.writeInt(payload.size());
      data.write(payload.toByteArray());
      data.write(MessageDigest.getInstance("SHA-256").digest(payload.toByteArray()));
    }
    writeSnapshotBytes(labels.resolve("labels.bin"), snapshot.toByteArray());
    var provider = new PersistentLabelDecisionProvider(labels);
    assertEquals(1, provider.acceptedDecisions().size());
    provider.enqueue(new LabelDecisionProvider.PendingLabel("c", "d", null, null));
    var reopened = new PersistentLabelDecisionProvider(labels);
    assertEquals(1, reopened.acceptedDecisions().size());
    assertEquals(1, reopened.getPendingLabels(
        new LabelDecisionProvider.LabelRequest("get", "v1", "new-key", 10)).labels().size());
  }

  @RepeatedTest(10)
  void concurrentInstancesDeliverEachPendingPairExactlyOnce(@TempDir Path temp) throws Exception {
    Path labels = temp.resolve("labels");
    var first = new PersistentLabelDecisionProvider(labels);
    var second = new PersistentLabelDecisionProvider(labels);
    for (int i = 0; i < 20; i++)
      first.enqueue(new LabelDecisionProvider.PendingLabel("left-" + i, "right-" + i, null, null));
    var start = new CountDownLatch(1);
    try (var executor = Executors.newFixedThreadPool(2)) {
      var one = executor.submit(() -> dequeueTen(first, "one", start));
      var two = executor.submit(() -> dequeueTen(second, "two", start));
      start.countDown();
      var delivered = new java.util.ArrayList<String>();
      delivered.addAll(one.get(30, TimeUnit.SECONDS));
      delivered.addAll(two.get(30, TimeUnit.SECONDS));
      assertEquals(20, delivered.size());
      assertEquals(IntStream.range(0, 20).mapToObj(i -> "left-" + i).collect(java.util.stream.Collectors.toSet()),
          Set.copyOf(delivered));
      assertTrue(new PersistentLabelDecisionProvider(labels).getPendingLabels(
          new LabelDecisionProvider.LabelRequest("empty", "v1", "empty-key", 1)).labels().isEmpty());
    }
  }

  @Test
  void idempotentProducerSurvivesDeliveryAndRestart(@TempDir Path temp) {
    Path labels = temp.resolve("labels");
    var label = new LabelDecisionProvider.PendingLabel("a", "b", "", null);
    var provider = new PersistentLabelDecisionProvider(labels);
    assertFalse(provider.enqueueIdempotent("v1", "producer-key", label));
    assertTrue(new PersistentLabelDecisionProvider(labels).enqueueIdempotent("v1", "producer-key", label));
    assertThrows(DuckException.class, () -> provider.enqueueIdempotent("v1", "producer-key",
        new LabelDecisionProvider.PendingLabel("a", "c", "", null)));
    var batch = provider.getPendingLabels(new LabelDecisionProvider.LabelRequest("get", "v1", "consumer-key", 1));
    assertEquals(List.of(label), batch.labels());
    var reopened = new PersistentLabelDecisionProvider(labels);
    assertTrue(reopened.enqueueIdempotent("v1", "producer-key", label));
    assertTrue(reopened.getPendingLabels(
        new LabelDecisionProvider.LabelRequest("empty", "v1", "consumer-key-2", 1)).labels().isEmpty());
  }

  @Test
  void versionTwoPendingSnapshotIsReadAndMigrated(@TempDir Path temp) throws Exception {
    Path labels = temp.resolve("labels");
    var label = new LabelDecisionProvider.PendingLabel("a", "b", null, "right");
    new PersistentLabelDecisionProvider(labels).enqueue(label);
    Path file = labels.resolve("labels.bin");
    byte[] versionThree = Files.readAllBytes(file);
    int length = java.nio.ByteBuffer.wrap(versionThree, 4, 4).getInt();
    byte[] versionTwoPayload = java.util.Arrays.copyOfRange(versionThree, 8, 8 + length - 4);
    var versionTwo = new ByteArrayOutputStream();
    try (var data = new DataOutputStream(versionTwo)) {
      data.writeInt(0x5a4c4432);
      data.writeInt(versionTwoPayload.length);
      data.write(versionTwoPayload);
      data.write(MessageDigest.getInstance("SHA-256").digest(versionTwoPayload));
    }
    writeSnapshotBytes(file, versionTwo.toByteArray());
    var provider = new PersistentLabelDecisionProvider(labels);
    assertTrue(provider.getPendingLabels(new LabelDecisionProvider.LabelRequest(
        "get", "v1", "consumer", 1)).labels().contains(label));
    assertFalse(provider.enqueueIdempotent("v1", "producer", label));
    assertTrue(new PersistentLabelDecisionProvider(labels).enqueueIdempotent("v1", "producer", label));
  }

  @Test
  void failuresBeforeAtomicMoveLeaveNoPublishedOrPartialSnapshot(@TempDir Path temp) throws Exception {
    for (var stage : List.of(PersistentLabelDecisionProvider.SnapshotWriteStage.AFTER_TEMP_CREATE,
        PersistentLabelDecisionProvider.SnapshotWriteStage.AFTER_TEMP_FORCE)) {
      Path labels = temp.resolve(stage.name());
      var label = new LabelDecisionProvider.PendingLabel("a", "b", null, null);
      var provider = new PersistentLabelDecisionProvider(labels, point -> {
        if (point == stage) throw new IllegalStateException("injected snapshot failure");
      });
      assertThrows(IllegalStateException.class, () -> provider.enqueueIdempotent("v1", "producer", label));
      assertFalse(Files.exists(labels.resolve("labels.bin")));
      try (var files = Files.list(labels)) {
        assertEquals(0, files.filter(path -> path.getFileName().toString().endsWith(".partial")).count());
      }
      var recovered = new PersistentLabelDecisionProvider(labels);
      assertFalse(recovered.enqueueIdempotent("v1", "producer", label));
      assertEquals(List.of(label), recovered.getPendingLabels(
          new LabelDecisionProvider.LabelRequest("get", "v1", "delivery", 1)).labels());
    }
  }

  @Test
  void startupRemovesOnlyOrphanRegularPartialFilesAndPreservesCommittedState(@TempDir Path temp)
      throws Exception {
    Path labels = temp.resolve("labels");
    var expected = new LabelDecisionProvider.PendingLabel("committed-left", "committed-right", null, null);
    var initial = new PersistentLabelDecisionProvider(labels);
    initial.enqueueIdempotent("v1", "committed-key", expected);
    byte[] committedSnapshot = Files.readAllBytes(labels.resolve("labels.bin"));

    Path orphanOne = labels.resolve(".labels-one.partial");
    Path orphanTwo = labels.resolve(".labels-two.partial");
    Files.writeString(orphanOne, "incomplete staging data");
    Files.writeString(orphanTwo, "another incomplete staging file");
    Path unrelated = labels.resolve("notes.partial");
    Files.writeString(unrelated, "not owned by the snapshot writer");
    Path nested = labels.resolve("nested");
    Files.createDirectory(nested);
    Path nestedPartial = nested.resolve(".labels-nested.partial");
    Files.writeString(nestedPartial, "outside the direct snapshot staging namespace");

    var recovered = new PersistentLabelDecisionProvider(labels);

    assertFalse(Files.exists(orphanOne));
    assertFalse(Files.exists(orphanTwo));
    assertEquals("not owned by the snapshot writer", Files.readString(unrelated));
    assertEquals("outside the direct snapshot staging namespace", Files.readString(nestedPartial));
    assertArrayEquals(committedSnapshot, Files.readAllBytes(labels.resolve("labels.bin")));
    assertEquals(List.of(expected), recovered.getPendingLabels(new LabelDecisionProvider.LabelRequest(
        "read", "v1", "recovery-read", 1)).labels());
  }

  @Test
  void startupDoesNotFollowOrDeleteSymlinkOrphanPartial(@TempDir Path temp) throws Exception {
    Path labels = temp.resolve("labels");
    var expected = new LabelDecisionProvider.PendingLabel("committed-left", "committed-right", null, null);
    new PersistentLabelDecisionProvider(labels).enqueueIdempotent("v1", "committed-key", expected);
    Path external = temp.resolve("external.txt");
    Files.writeString(external, "external target must remain intact");
    Path link = labels.resolve(".labels-linked.partial");
    try {
      Files.createSymbolicLink(link, external);
    } catch (IOException | UnsupportedOperationException | SecurityException unavailable) {
      Assumptions.assumeTrue(false, "symbolic links unavailable on this runner: " + unavailable.getMessage());
    }

    var recovered = new PersistentLabelDecisionProvider(labels);

    assertTrue(Files.isSymbolicLink(link), "cleanup must ignore, not follow or remove, non-regular partials");
    assertEquals("external target must remain intact", Files.readString(external));
    assertEquals(List.of(expected), recovered.getPendingLabels(new LabelDecisionProvider.LabelRequest(
        "read", "v1", "recovery-read", 1)).labels());
  }

  @Test
  void failureAfterAtomicMoveReplaysCommittedProducerAndDelivery(@TempDir Path temp) {
    Path labels = temp.resolve("labels");
    var label = new LabelDecisionProvider.PendingLabel("a", "b", "left", "right");
    var failing = new PersistentLabelDecisionProvider(labels, stage -> {
      if (stage == PersistentLabelDecisionProvider.SnapshotWriteStage.AFTER_ATOMIC_MOVE)
        throw new IllegalStateException("reply lost after atomic move");
    });
    assertThrows(IllegalStateException.class, () -> failing.enqueueIdempotent("v1", "producer", label));
    var recovered = new PersistentLabelDecisionProvider(labels);
    assertTrue(recovered.enqueueIdempotent("v1", "producer", label));
    var deliveryFailing = new PersistentLabelDecisionProvider(labels, stage -> {
      if (stage == PersistentLabelDecisionProvider.SnapshotWriteStage.AFTER_ATOMIC_MOVE)
        throw new IllegalStateException("delivery reply lost after atomic move");
    });
    assertThrows(IllegalStateException.class, () -> deliveryFailing.getPendingLabels(
        new LabelDecisionProvider.LabelRequest("first", "v1", "delivery", 1)));
    var finalProvider = new PersistentLabelDecisionProvider(labels);
    assertEquals(List.of(label), finalProvider.getPendingLabels(
        new LabelDecisionProvider.LabelRequest("retry", "v1", "delivery", 1)).labels());
    assertTrue(finalProvider.getPendingLabels(
        new LabelDecisionProvider.LabelRequest("empty", "v1", "new-delivery", 1)).labels().isEmpty());
  }

  @Test
  void childJvmHardStopPreservesProducerAtomicityAtEveryWriteStage(@TempDir Path temp) throws Exception {
    var label = new LabelDecisionProvider.PendingLabel("crash-left", "crash-right", null, null);
    for (var stage : PersistentLabelDecisionProvider.SnapshotWriteStage.values()) {
      Path labels = temp.resolve(stage.name());
      runCrashProbe(labels, stage, "enqueue");
      boolean committed = stage == PersistentLabelDecisionProvider.SnapshotWriteStage.AFTER_ATOMIC_MOVE;
      assertEquals(committed, Files.exists(labels.resolve("labels.bin")));
      if (!committed) assertEquals(1, partialCount(labels));
      var recovered = new PersistentLabelDecisionProvider(labels);
      assertEquals(0, partialCount(labels));
      assertEquals(committed, recovered.enqueueIdempotent("v1", "crash-producer", label));
      assertEquals(List.of(label), recovered.getPendingLabels(
          new LabelDecisionProvider.LabelRequest("get", "v1", "after-crash", 1)).labels());
    }
  }

  @Test
  void childJvmHardStopPreservesDeliveryAtomicityAtEveryWriteStage(@TempDir Path temp) throws Exception {
    var label = new LabelDecisionProvider.PendingLabel("crash-left", "crash-right", null, null);
    for (var stage : PersistentLabelDecisionProvider.SnapshotWriteStage.values()) {
      Path labels = temp.resolve(stage.name());
      new PersistentLabelDecisionProvider(labels).enqueueIdempotent("v1", "initial-producer", label);
      byte[] before = Files.readAllBytes(labels.resolve("labels.bin"));
      runCrashProbe(labels, stage, "delivery");
      byte[] after = Files.readAllBytes(labels.resolve("labels.bin"));
      boolean committed = stage == PersistentLabelDecisionProvider.SnapshotWriteStage.AFTER_ATOMIC_MOVE;
      if (!committed) assertArrayEquals(before, after);
      else assertFalse(java.util.Arrays.equals(before, after));
      if (!committed) assertEquals(1, partialCount(labels));
      var recovered = new PersistentLabelDecisionProvider(labels);
      assertEquals(0, partialCount(labels));
      assertEquals(List.of(label), recovered.getPendingLabels(
          new LabelDecisionProvider.LabelRequest("retry", "v1", "crash-delivery", 1)).labels());
      assertTrue(recovered.getPendingLabels(
          new LabelDecisionProvider.LabelRequest("empty", "v1", "after-crash", 1)).labels().isEmpty());
    }
  }

  @Test
  void snapshotSerializationStopsAtByteBudgetWithoutOvershooting() {
    var output = new PersistentLabelDecisionProvider.BoundedOutputStream(4);
    output.write(new byte[] {1, 2, 3}, 0, 3);
    assertThrows(DuckException.class, () -> output.write(new byte[] {4, 5}, 0, 2));
    assertEquals(3, output.bytes().length);
    output.write(4);
    assertThrows(DuckException.class, () -> output.write(5));
    assertArrayEquals(new byte[] {1, 2, 3, 4}, output.bytes());
  }

  @Test
  void oversizedPayloadDoesNotPublishSnapshot(@TempDir Path temp) {
    Path labels = temp.resolve("labels");
    var provider = new PersistentLabelDecisionProvider(labels);
    var tooLarge = new LabelDecisionProvider.PendingLabel("a", "b", "x".repeat(70_000), null);
    assertThrows(DuckException.class, () -> provider.enqueueIdempotent("v1", "large", tooLarge));
    assertFalse(Files.exists(labels.resolve("labels.bin")));
    assertFalse(provider.enqueueIdempotent("v1", "small",
        new LabelDecisionProvider.PendingLabel("a", "b", "ok", null)));
  }

  @Test
  void symbolicSnapshotAndLockTargetsAreRejected(@TempDir Path temp) throws Exception {
    Path target = temp.resolve("outside.txt");
    Files.writeString(target, "untouched");
    for (String name : List.of("labels.bin", "labels.lock")) {
      Path labels = temp.resolve(name + "-root");
      Files.createDirectories(labels);
      try { Files.createSymbolicLink(labels.resolve(name), target); }
      catch (IOException | UnsupportedOperationException | SecurityException e) {
        Assumptions.assumeTrue(false, "symbolic links unavailable on this runner: " + e.getMessage());
      }
      assertThrows(DuckException.class, () -> new PersistentLabelDecisionProvider(labels));
      assertEquals("untouched", Files.readString(target));
    }
  }

  @Test
  void noFollowChannelsRejectTargetsReplacedBySymlinks(@TempDir Path temp) throws Exception {
    Path outside = temp.resolve("outside.bin");
    Files.writeString(outside, "must remain untouched");
    Path lockRoot = temp.resolve("lock-root");
    var lockProvider = new PersistentLabelDecisionProvider(lockRoot);
    Files.delete(lockRoot.resolve("labels.lock"));
    try { Files.createSymbolicLink(lockRoot.resolve("labels.lock"), outside); }
    catch (IOException | UnsupportedOperationException | SecurityException e) {
      Assumptions.assumeTrue(false, "symbolic links unavailable on this runner: " + e.getMessage());
    }
    assertThrows(IOException.class, () -> {
      try (var ignored = lockProvider.openLockChannel()) { }
    });
    assertEquals("must remain untouched", Files.readString(outside));

    Path snapshotRoot = temp.resolve("snapshot-root");
    var snapshotProvider = new PersistentLabelDecisionProvider(snapshotRoot);
    snapshotProvider.enqueue(new LabelDecisionProvider.PendingLabel("left", "right", null, null));
    Files.delete(snapshotRoot.resolve("labels.bin"));
    Files.createSymbolicLink(snapshotRoot.resolve("labels.bin"), outside);
    assertThrows(IOException.class, () -> {
      try (var ignored = snapshotProvider.openSnapshotChannel()) { }
    });
    assertEquals("must remain untouched", Files.readString(outside));
  }

  private static void runCrashProbe(Path labels,
      PersistentLabelDecisionProvider.SnapshotWriteStage stage, String operation) throws Exception {
    String executable = System.getProperty("os.name").startsWith("Windows") ? "java.exe" : "java";
    String java = Path.of(System.getProperty("java.home"), "bin", executable).toString();
    var process = new ProcessBuilder(java, "-cp", System.getProperty("java.class.path"),
        PersistentLabelCrashProbe.class.getName(), labels.toString(), stage.name(), operation)
        .redirectErrorStream(true).start();
    if (!process.waitFor(60, TimeUnit.SECONDS)) {
      process.destroyForcibly();
      throw new AssertionError("crash probe timed out");
    }
    assertEquals(73, process.exitValue(), new String(process.getInputStream().readAllBytes()));
  }

  private static long partialCount(Path labels) throws Exception {
    try (var files = Files.list(labels)) {
      return files.filter(path -> path.getFileName().toString().endsWith(".partial")).count();
    }
  }

  private static List<String> dequeueTen(PersistentLabelDecisionProvider provider, String prefix,
      CountDownLatch start) throws InterruptedException {
    start.await();
    var result = new java.util.ArrayList<String>();
    for (int i = 0; i < 10; i++) {
      var batch = provider.getPendingLabels(new LabelDecisionProvider.LabelRequest(
          prefix + "-" + i, "v1", prefix + "-key-" + i, 1));
      assertEquals(1, batch.labels().size());
      result.add(batch.labels().get(0).leftId());
    }
    return result;
  }

  private static CompatibilityRuntime runtime(RuntimeConfig config, Path labels) {
    return new CompatibilityRuntime(config, null, "zingg-0.7.0-duckdb-1.5.5.1", null,
        new PersistentLabelDecisionProvider(labels));
  }
}
