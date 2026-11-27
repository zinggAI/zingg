package io.zingg.duckdb.compat;

import io.zingg.duckdb.api.DuckException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.OpenOption;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.PosixFilePermissions;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;

/** Atomic, checksum-verified local snapshot of applied and pending label state. */
public final class PersistentLabelDecisionProvider implements LabelDecisionProvider {
  private static final int MAGIC_V1 = 0x5a4c4431; // ZLD1, applied requests only
  private static final int MAGIC_V2 = 0x5a4c4432; // ZLD2, pending queue and delivery receipts
  private static final int MAGIC_V3 = 0x5a4c4433; // ZLD3, idempotent enqueue receipts
  private static final int JOURNAL_MAGIC_V1 = 0x5a4c4a31; // ZLJ1, append-only applied requests
  private static final int MAX_BYTES = 64 * 1024 * 1024;
  private static final int MAX_ENTRIES = 100_000;
  private static final long JOURNAL_COMPACTION_THRESHOLD = 1L * 1024 * 1024;
  private static final ReentrantLock JVM_LOCK = new ReentrantLock();
  private final Path directory;
  private final Path snapshot;
  private final Path appliedJournal;
  private final Path lockFile;
  private final SnapshotWriteObserver writeObserver;
  private InMemoryLabelDecisionProvider delegate;
  enum SnapshotWriteStage { AFTER_TEMP_CREATE, AFTER_TEMP_FORCE, AFTER_ATOMIC_MOVE }
  @FunctionalInterface interface SnapshotWriteObserver { void after(SnapshotWriteStage stage); }
  private record Delivery(String schemaVersion, String idempotencyKey, int limit,
      List<PendingLabel> labels, boolean hasMore) {}
  private record Enqueue(String schemaVersion, String idempotencyKey, PendingLabel label) {}
  private record Snapshot(List<ApplyLabelsRequest> applied, List<PendingLabel> pending,
      List<Delivery> deliveries, List<Enqueue> enqueues) {
    private static Snapshot empty() { return new Snapshot(List.of(), List.of(), List.of(), List.of()); }
  }
  static final class BoundedOutputStream extends OutputStream {
    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    private final int limit;
    BoundedOutputStream(int limit) { if (limit < 0) throw new IllegalArgumentException("negative limit"); this.limit = limit; }
    @Override public void write(int value) {
      if (buffer.size() >= limit) throw new DuckException("label snapshot exceeds size limit");
      buffer.write(value);
    }
    @Override public void write(byte[] bytes, int offset, int length) {
      Objects.checkFromIndexSize(offset, length, bytes.length);
      if (length > limit - buffer.size()) throw new DuckException("label snapshot exceeds size limit");
      buffer.write(bytes, offset, length);
    }
    byte[] bytes() { return buffer.toByteArray(); }
  }

  public PersistentLabelDecisionProvider(Path directory) {
    this(directory, stage -> {});
  }

  PersistentLabelDecisionProvider(Path directory, SnapshotWriteObserver writeObserver) {
    this.directory = Objects.requireNonNull(directory, "directory").toAbsolutePath().normalize();
    this.writeObserver = Objects.requireNonNull(writeObserver, "writeObserver");
    this.snapshot = this.directory.resolve("labels.bin");
    this.appliedJournal = this.directory.resolve("labels.applied.log");
    this.lockFile = this.directory.resolve("labels.lock");
    try {
      Files.createDirectories(this.directory);
      if (Files.isSymbolicLink(this.directory) || Files.isSymbolicLink(snapshot)
          || Files.isSymbolicLink(lockFile) || Files.isSymbolicLink(appliedJournal))
        throw new DuckException("label snapshot cannot be a symbolic link");
      JVM_LOCK.lock();
      try (var channel = openLockChannel();
           var ignored = channel.lock()) {
        cleanupOrphanPartials();
        this.delegate = replay(currentSnapshot().applied());
      } finally { JVM_LOCK.unlock(); }
    } catch (IOException e) { throw new DuckException("label snapshot cannot be loaded", e); }
  }

  private void cleanupOrphanPartials() throws IOException {
    try (var candidates = Files.newDirectoryStream(directory, ".labels-*.partial")) {
      for (Path candidate : candidates) {
        Path normalized = candidate.toAbsolutePath().normalize();
        if (!directory.equals(normalized.getParent()) || !Files.isRegularFile(normalized, LinkOption.NOFOLLOW_LINKS))
          continue;
        Files.deleteIfExists(normalized);
      }
    }
  }

  @Override public synchronized LabelBatch getPendingLabels(LabelRequest request) {
    Objects.requireNonNull(request, "request");
    JVM_LOCK.lock();
    try {
    if (Files.isSymbolicLink(lockFile)) throw new DuckException("label snapshot cannot be a symbolic link");
    try (var channel = openLockChannel();
         var ignored = channel.lock()) {
      Snapshot current = currentSnapshot();
      for (Delivery prior : current.deliveries()) if (prior.idempotencyKey().equals(request.idempotencyKey())) {
        if (!prior.schemaVersion().equals(request.schemaVersion()) || prior.limit() != request.limit())
          throw new DuckException("idempotency key reused with different pending-label request");
        return new LabelBatch(request.requestId(), request.schemaVersion(), prior.labels(), prior.hasMore());
      }
      int count = Math.min(request.limit(), current.pending().size());
      List<PendingLabel> labels = List.copyOf(current.pending().subList(0, count));
      List<PendingLabel> remaining = List.copyOf(current.pending().subList(count, current.pending().size()));
      boolean hasMore = !remaining.isEmpty();
      var deliveries = new ArrayList<>(current.deliveries());
      deliveries.add(new Delivery(request.schemaVersion(), request.idempotencyKey(), request.limit(), labels, hasMore));
      writeSnapshot(new Snapshot(current.applied(), remaining, List.copyOf(deliveries), current.enqueues()));
      return new LabelBatch(request.requestId(), request.schemaVersion(), labels, hasMore);
    } catch (IOException e) { throw new DuckException("label snapshot lock failed", e); }
    } finally { JVM_LOCK.unlock(); }
  }

  /** Add one pending pair; its queue position survives worker reconstruction. */
  public synchronized void enqueue(PendingLabel label) {
    Objects.requireNonNull(label, "label");
    JVM_LOCK.lock();
    try {
    if (Files.isSymbolicLink(lockFile)) throw new DuckException("label snapshot cannot be a symbolic link");
    try (var channel = openLockChannel();
         var ignored = channel.lock()) {
      Snapshot current = currentSnapshot();
      if (current.pending().size() >= MAX_ENTRIES) throw new DuckException("pending-label queue is full");
      var pending = new ArrayList<>(current.pending());
      pending.add(label);
      writeSnapshot(new Snapshot(current.applied(), List.copyOf(pending), current.deliveries(), current.enqueues()));
    } catch (IOException e) { throw new DuckException("label snapshot lock failed", e); }
    } finally { JVM_LOCK.unlock(); }
  }

  /** Queue a pair exactly once per producer key, including after a process restart. */
  public synchronized boolean enqueueIdempotent(String schemaVersion, String idempotencyKey, PendingLabel label) {
    if (schemaVersion == null || schemaVersion.isBlank() || idempotencyKey == null || idempotencyKey.isBlank())
      throw new IllegalArgumentException("schema version and idempotency key are required");
    Objects.requireNonNull(label, "label");
    JVM_LOCK.lock();
    try {
      if (Files.isSymbolicLink(lockFile)) throw new DuckException("label snapshot cannot be a symbolic link");
      try (var channel = openLockChannel();
           var ignored = channel.lock()) {
        Snapshot current = currentSnapshot();
        for (Enqueue prior : current.enqueues()) if (prior.idempotencyKey().equals(idempotencyKey)) {
          if (!prior.schemaVersion().equals(schemaVersion) || !prior.label().equals(label))
            throw new DuckException("idempotency key reused with different pending-label payload");
          return true;
        }
        if (current.pending().size() >= MAX_ENTRIES || current.enqueues().size() >= MAX_ENTRIES)
          throw new DuckException("pending-label queue or receipt store is full");
        var pending = new ArrayList<>(current.pending());
        pending.add(label);
        var enqueues = new ArrayList<>(current.enqueues());
        enqueues.add(new Enqueue(schemaVersion, idempotencyKey, label));
        writeSnapshot(new Snapshot(current.applied(), List.copyOf(pending), current.deliveries(),
            List.copyOf(enqueues)));
        return false;
      } catch (IOException e) { throw new DuckException("label snapshot lock failed", e); }
    } finally { JVM_LOCK.unlock(); }
  }

  @Override public synchronized List<LabelDecision> acceptedDecisions() {
    JVM_LOCK.lock();
    try {
    if (Files.isSymbolicLink(lockFile)) throw new DuckException("label snapshot cannot be a symbolic link");
    try (var channel = openLockChannel();
         var ignored = channel.lock()) {
      if (Files.isSymbolicLink(snapshot) || Files.isSymbolicLink(lockFile))
        throw new DuckException("label snapshot cannot be a symbolic link");
      delegate = replay(currentSnapshot().applied());
      return delegate.acceptedDecisions();
    } catch (IOException e) { throw new DuckException("label snapshot lock failed", e); }
    } finally { JVM_LOCK.unlock(); }
  }

  @Override public synchronized ApplyLabelsResult applyLabels(ApplyLabelsRequest request) {
    Objects.requireNonNull(request, "request");
    JVM_LOCK.lock();
    try {
    if (Files.isSymbolicLink(lockFile)) throw new DuckException("label snapshot cannot be a symbolic link");
    try (var channel = openLockChannel();
         var ignored = channel.lock()) {
      if (Files.isSymbolicLink(snapshot) || Files.isSymbolicLink(lockFile))
        throw new DuckException("label snapshot cannot be a symbolic link");
      Snapshot current = currentSnapshot();
      var staged = replay(current.applied());
      ApplyLabelsResult result = staged.applyLabels(request);
      if (!result.replay()) {
        // Keep the established on-disk bootstrap contract (a first write
        // creates labels.bin), then append subsequent applied requests as
        // durable deltas instead of rewriting the cumulative snapshot.
        if (!Files.exists(snapshot, LinkOption.NOFOLLOW_LINKS)) writeSnapshot(current);
        appendAppliedRequest(request);
        var compactedApplied = new ArrayList<>(current.applied());
        compactedApplied.add(request);
        compactAppliedJournalIfNeeded(new Snapshot(List.copyOf(compactedApplied), current.pending(),
             current.deliveries(), current.enqueues()));
      }
      delegate = staged;
      return result;
    } catch (IOException e) { throw new DuckException("label snapshot lock failed", e); }
    } finally { JVM_LOCK.unlock(); }
  }

  private static InMemoryLabelDecisionProvider replay(List<ApplyLabelsRequest> requests) {
    var provider = new InMemoryLabelDecisionProvider();
    for (var request : requests) {
      if (provider.applyLabels(request).replay())
        throw new DuckException("duplicate idempotency key in label snapshot");
    }
    return provider;
  }

  private Snapshot currentSnapshot() throws IOException {
    if (Files.isSymbolicLink(snapshot) || Files.isSymbolicLink(lockFile)
        || Files.isSymbolicLink(appliedJournal))
      throw new DuckException("label snapshot cannot be a symbolic link");
    Snapshot base = Files.exists(snapshot, LinkOption.NOFOLLOW_LINKS) ? readSnapshot() : Snapshot.empty();
    if (!Files.exists(appliedJournal, LinkOption.NOFOLLOW_LINKS)) return base;
    var applied = new ArrayList<>(base.applied());
    var keys = new HashSet<String>();
    for (ApplyLabelsRequest request : applied) keys.add(request.idempotencyKey());
    for (ApplyLabelsRequest request : readAppliedJournal()) {
      if (!keys.add(request.idempotencyKey())) {
        if (applied.stream().noneMatch(existing -> existing.idempotencyKey().equals(request.idempotencyKey())
            && existing.equals(request)))
          throw new DuckException("applied request journal conflicts with snapshot");
        continue;
      }
      applied.add(request);
    }
    return new Snapshot(List.copyOf(applied), base.pending(), base.deliveries(), base.enqueues());
  }

  /**
   * Applied-label requests are append-only deltas. Pending delivery state still
   * uses the checksummed snapshot because it is a queue mutation, while the
   * high-volume label stream no longer rewrites every historical request.
   */
  private void appendAppliedRequest(ApplyLabelsRequest request) {
    try {
      byte[] payload = encodeAppliedRequest(request);
      if (payload.length > MAX_BYTES - 44) throw new DuckException("label journal record is too large");
      var envelope = new ByteArrayOutputStream(payload.length + 44);
      try (var data = new DataOutputStream(envelope)) {
        data.writeInt(JOURNAL_MAGIC_V1);
        data.writeInt(payload.length);
        data.write(payload);
        data.write(sha256(payload));
      }
      byte[] record = envelope.toByteArray();
      try (var channel = openAppliedJournalChannel()) {
        byte[] existing = Files.exists(appliedJournal, LinkOption.NOFOLLOW_LINKS)
            ? Files.readAllBytes(appliedJournal) : new byte[0];
        long valid = validJournalLength(existing);
        if (valid + record.length > MAX_BYTES)
          throw new DuckException("label journal exceeds size limit");
        channel.truncate(valid);
        channel.position(valid);
        var bytes = ByteBuffer.wrap(record);
        while (bytes.hasRemaining()) channel.write(bytes);
        channel.force(true);
      }
    } catch (IOException e) {
      throw new DuckException("applied label journal cannot be persisted", e);
    }
  }

  private List<ApplyLabelsRequest> readAppliedJournal() throws IOException {
    byte[] bytes = Files.readAllBytes(appliedJournal);
    if (bytes.length > MAX_BYTES) throw new DuckException("label journal size is invalid");
    var result = new ArrayList<ApplyLabelsRequest>();
    int offset = 0;
    while (offset + 8 <= bytes.length) {
      int magic = java.nio.ByteBuffer.wrap(bytes, offset, 4).getInt();
      int length = java.nio.ByteBuffer.wrap(bytes, offset + 4, 4).getInt();
      if (magic != JOURNAL_MAGIC_V1 || length < 0 || length > MAX_BYTES - 44)
        throw new DuckException("label journal header is corrupt at byte " + offset);
      long end = (long) offset + 8L + length + 32L;
      if (end > bytes.length) break; // an uncommitted final append
      byte[] payload = java.util.Arrays.copyOfRange(bytes, offset + 8, offset + 8 + length);
      byte[] digest = java.util.Arrays.copyOfRange(bytes, offset + 8 + length, (int) end);
      if (!MessageDigest.isEqual(digest, sha256(payload)))
        throw new DuckException("label journal checksum mismatch");
      try (var data = new DataInputStream(new ByteArrayInputStream(payload))) {
        result.add(readAppliedRequest(data));
        if (data.available() != 0) throw new DuckException("label journal record has trailing data");
      }
      offset = (int) end;
    }
    return List.copyOf(result);
  }

  private static long validJournalLength(byte[] bytes) throws IOException {
    int offset = 0;
    while (offset + 8 <= bytes.length) {
      int magic = java.nio.ByteBuffer.wrap(bytes, offset, 4).getInt();
      int length = java.nio.ByteBuffer.wrap(bytes, offset + 4, 4).getInt();
      if (magic != JOURNAL_MAGIC_V1 || length < 0 || length > MAX_BYTES - 44)
        throw new DuckException("label journal header is corrupt at byte " + offset);
      long end = (long) offset + 8L + length + 32L;
      if (end > bytes.length) break;
      byte[] payload = java.util.Arrays.copyOfRange(bytes, offset + 8, offset + 8 + length);
      byte[] digest = java.util.Arrays.copyOfRange(bytes, offset + 8 + length, (int) end);
      if (!MessageDigest.isEqual(digest, sha256(payload)))
        throw new DuckException("label journal checksum mismatch");
      offset = (int) end;
    }
    return offset;
  }

  private void compactAppliedJournalIfNeeded(Snapshot state) throws IOException {
    if (!Files.exists(appliedJournal, LinkOption.NOFOLLOW_LINKS)
        || Files.size(appliedJournal) < JOURNAL_COMPACTION_THRESHOLD) return;
    writeSnapshot(state);
  }

  private FileChannel openAppliedJournalChannel() throws IOException {
    FileChannel channel;
    try {
      channel = FileChannel.open(appliedJournal, java.util.Set.<OpenOption>of(StandardOpenOption.CREATE,
          StandardOpenOption.READ, StandardOpenOption.WRITE, LinkOption.NOFOLLOW_LINKS),
          PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rw-------")));
    } catch (UnsupportedOperationException unsupported) {
      channel = FileChannel.open(appliedJournal, StandardOpenOption.CREATE, StandardOpenOption.READ,
          StandardOpenOption.WRITE, LinkOption.NOFOLLOW_LINKS);
    }
    try {
      requirePrivatePosixPermissions(appliedJournal);
      return channel;
    } catch (IOException | RuntimeException failure) {
      channel.close();
      throw failure;
    }
  }

  private static byte[] encodeAppliedRequest(ApplyLabelsRequest request) throws IOException {
    var bytes = new ByteArrayOutputStream();
    try (var data = new DataOutputStream(bytes)) {
      writeAppliedRequest(data, request);
    }
    return bytes.toByteArray();
  }

  private static ApplyLabelsRequest readAppliedRequest(DataInputStream data) throws IOException {
    String requestId = data.readUTF(), schema = data.readUTF(), key = data.readUTF();
    int decisionCount = data.readInt();
    if (decisionCount < 0 || decisionCount > MAX_ENTRIES)
      throw new DuckException("label journal decision count is invalid");
    var decisions = new ArrayList<LabelDecision>(decisionCount);
    for (int i = 0; i < decisionCount; i++) {
      decisions.add(new LabelDecision(data.readUTF(), data.readUTF(),
          Decision.valueOf(data.readUTF()), data.readUTF()));
    }
    return new ApplyLabelsRequest(requestId, schema, key, decisions);
  }

  FileChannel openLockChannel() throws IOException {
    FileChannel channel;
    try {
      channel = FileChannel.open(lockFile, java.util.Set.<OpenOption>of(StandardOpenOption.CREATE,
          StandardOpenOption.WRITE, LinkOption.NOFOLLOW_LINKS), PosixFilePermissions.asFileAttribute(
              PosixFilePermissions.fromString("rw-------")));
    } catch (UnsupportedOperationException unsupported) {
      channel = FileChannel.open(lockFile, StandardOpenOption.CREATE, StandardOpenOption.WRITE,
          LinkOption.NOFOLLOW_LINKS);
    }
    try {
      requirePrivatePosixPermissions(lockFile);
      return channel;
    } catch (IOException | RuntimeException failure) {
      channel.close();
      throw failure;
    }
  }

  private Snapshot readSnapshot() throws IOException {
    byte[] bytes;
    try (var channel = openSnapshotChannel()) {
      long size = channel.size();
      if (size < 44 || size > MAX_BYTES) throw new DuckException("label snapshot size is invalid");
      var content = ByteBuffer.allocate((int) size);
      while (content.hasRemaining()) {
        int read = channel.read(content);
        if (read < 0) throw new DuckException("label snapshot changed while being read");
      }
      bytes = content.array();
    }
    try (var input = new DataInputStream(new ByteArrayInputStream(bytes))) {
      int magic = input.readInt();
      if (magic != MAGIC_V1 && magic != MAGIC_V2 && magic != MAGIC_V3)
        throw new DuckException("label snapshot format is unsupported");
      int length = input.readInt();
      if (length < 0 || length != bytes.length - 8 - 32)
        throw new DuckException("label snapshot length is invalid");
      byte[] payload = input.readNBytes(length);
      byte[] digest = input.readNBytes(32);
      if (!MessageDigest.isEqual(digest, sha256(payload)))
        throw new DuckException("label snapshot checksum mismatch");
      try (var data = new DataInputStream(new ByteArrayInputStream(payload))) {
        int count = data.readInt();
        if (count < 0 || count > MAX_ENTRIES) throw new DuckException("label request count is invalid");
        var loaded = new ArrayList<ApplyLabelsRequest>(count);
        for (int i = 0; i < count; i++) {
          String requestId = data.readUTF(), schema = data.readUTF(), key = data.readUTF();
          int decisionCount = data.readInt();
          if (decisionCount < 0 || decisionCount > MAX_ENTRIES)
            throw new DuckException("label decision count is invalid");
          var decisions = new ArrayList<LabelDecision>(decisionCount);
          for (int j = 0; j < decisionCount; j++) {
            String left = data.readUTF(), right = data.readUTF(), choice = data.readUTF(), source = data.readUTF();
            decisions.add(new LabelDecision(left, right, Decision.valueOf(choice), source));
          }
          loaded.add(new ApplyLabelsRequest(requestId, schema, key, decisions));
        }
        validateAppliedRequestKeys(loaded);
        if (magic == MAGIC_V1) {
          if (data.available() != 0) throw new DuckException("label snapshot has trailing data");
          return new Snapshot(List.copyOf(loaded), List.of(), List.of(), List.of());
        }
        int pendingCount = data.readInt();
        if (pendingCount < 0 || pendingCount > MAX_ENTRIES)
          throw new DuckException("pending-label count is invalid");
        var pending = new ArrayList<PendingLabel>(pendingCount);
        for (int i = 0; i < pendingCount; i++) pending.add(readPendingLabel(data));
        int deliveryCount = data.readInt();
        if (deliveryCount < 0 || deliveryCount > MAX_ENTRIES)
          throw new DuckException("pending-label delivery count is invalid");
        var deliveries = new ArrayList<Delivery>(deliveryCount);
        for (int i = 0; i < deliveryCount; i++) {
          String schema = data.readUTF(), key = data.readUTF();
          int limit = data.readInt(), labelCount = data.readInt();
          if (limit < 1 || limit > MAX_ENTRIES || labelCount < 0 || labelCount > limit)
            throw new DuckException("pending-label delivery is invalid");
          var labels = new ArrayList<PendingLabel>(labelCount);
          for (int j = 0; j < labelCount; j++) labels.add(readPendingLabel(data));
          deliveries.add(new Delivery(schema, key, limit, List.copyOf(labels), readBoolean(data)));
        }
        if (magic == MAGIC_V2) {
          if (data.available() != 0) throw new DuckException("label snapshot has trailing data");
          validateReceiptKeys(deliveries, List.of());
          return new Snapshot(List.copyOf(loaded), List.copyOf(pending), List.copyOf(deliveries), List.of());
        }
        int enqueueCount = data.readInt();
        if (enqueueCount < 0 || enqueueCount > MAX_ENTRIES)
          throw new DuckException("pending-label enqueue count is invalid");
        var enqueues = new ArrayList<Enqueue>(enqueueCount);
        for (int i = 0; i < enqueueCount; i++)
          enqueues.add(new Enqueue(data.readUTF(), data.readUTF(), readPendingLabel(data)));
        if (data.available() != 0) throw new DuckException("label snapshot has trailing data");
        validateReceiptKeys(deliveries, enqueues);
        return new Snapshot(List.copyOf(loaded), List.copyOf(pending), List.copyOf(deliveries),
            List.copyOf(enqueues));
      }
    } catch (IllegalArgumentException e) { throw new DuckException("label snapshot content is invalid", e); }
  }

  FileChannel openSnapshotChannel() throws IOException {
    FileChannel channel = FileChannel.open(snapshot, StandardOpenOption.READ, LinkOption.NOFOLLOW_LINKS);
    try {
      requirePrivatePosixPermissions(snapshot);
      return channel;
    } catch (IOException | RuntimeException failure) {
      channel.close();
      throw failure;
    }
  }

  private static void requirePrivatePosixPermissions(Path path) throws IOException {
    try {
      var permissions = Files.getPosixFilePermissions(path, LinkOption.NOFOLLOW_LINKS);
      if (!permissions.equals(PosixFilePermissions.fromString("rw-------")))
        throw new IOException("persistent label file permissions must be owner-only (0600): " + path.getFileName());
    } catch (UnsupportedOperationException unsupported) {
      // Windows and other providers without POSIX modes use their directory ACL policy.
    }
  }

  private void writeSnapshot(Snapshot state) {
    Path temporary = null;
    try {
      var buffer = new BoundedOutputStream(MAX_BYTES - 40);
      try (var data = new DataOutputStream(buffer)) {
        if (state.applied().size() > MAX_ENTRIES || state.pending().size() > MAX_ENTRIES
            || state.deliveries().size() > MAX_ENTRIES || state.enqueues().size() > MAX_ENTRIES)
          throw new DuckException("label snapshot entry count exceeds limit");
        data.writeInt(state.applied().size());
        for (var request : state.applied()) writeAppliedRequest(data, request);
        data.writeInt(state.pending().size());
        for (var label : state.pending()) writePendingLabel(data, label);
        data.writeInt(state.deliveries().size());
        for (var delivery : state.deliveries()) {
          data.writeUTF(delivery.schemaVersion());
          data.writeUTF(delivery.idempotencyKey());
          data.writeInt(delivery.limit());
          data.writeInt(delivery.labels().size());
          for (var label : delivery.labels()) writePendingLabel(data, label);
          data.writeBoolean(delivery.hasMore());
        }
        data.writeInt(state.enqueues().size());
        for (var enqueue : state.enqueues()) {
          data.writeUTF(enqueue.schemaVersion());
          data.writeUTF(enqueue.idempotencyKey());
          writePendingLabel(data, enqueue.label());
        }
      }
      byte[] payload = buffer.bytes();
      if (payload.length + 40 > MAX_BYTES) throw new DuckException("label snapshot exceeds size limit");
      var envelope = new ByteArrayOutputStream(payload.length + 40);
      try (var data = new DataOutputStream(envelope)) {
        data.writeInt(MAGIC_V3);
        data.writeInt(payload.length);
        data.write(payload);
        data.write(sha256(payload));
      }
      try {
        temporary = Files.createTempFile(directory, ".labels-", ".partial",
            PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rw-------")));
      } catch (UnsupportedOperationException unsupported) {
        temporary = Files.createTempFile(directory, ".labels-", ".partial");
      }
      writeObserver.after(SnapshotWriteStage.AFTER_TEMP_CREATE);
      try (var channel = FileChannel.open(temporary, StandardOpenOption.WRITE)) {
        ByteBuffer bytes = ByteBuffer.wrap(envelope.toByteArray());
        while (bytes.hasRemaining()) channel.write(bytes);
        channel.force(true);
      }
      writeObserver.after(SnapshotWriteStage.AFTER_TEMP_FORCE);
      replaceAtomically(temporary, snapshot);
      writeObserver.after(SnapshotWriteStage.AFTER_ATOMIC_MOVE);
      forceDirectoryEntryBeforeJournalTruncate();
      clearAppliedJournal();
    } catch (AtomicMoveNotSupportedException e) {
      throw new DuckException("atomic label snapshot replacement is unavailable", e);
    } catch (IOException e) { throw new DuckException("label snapshot cannot be persisted", e); }
    finally {
      if (temporary != null) try { Files.deleteIfExists(temporary); } catch (IOException ignored) { }
    }
  }

  private static void writeAppliedRequest(DataOutputStream data, ApplyLabelsRequest request)
      throws IOException {
    data.writeUTF(request.requestId());
    data.writeUTF(request.schemaVersion());
    data.writeUTF(request.idempotencyKey());
    data.writeInt(request.decisions().size());
    for (var decision : request.decisions()) {
      data.writeUTF(decision.leftId());
      data.writeUTF(decision.rightId());
      data.writeUTF(decision.decision().name());
      data.writeUTF(decision.source());
    }
  }

  private void clearAppliedJournal() throws IOException {
    if (!Files.exists(appliedJournal, LinkOption.NOFOLLOW_LINKS)) return;
    try (var channel = openAppliedJournalChannel()) {
      channel.truncate(0);
      channel.force(true);
    }
  }

  /**
   * Persist the directory entry before deleting the only durable journal copy.
   * Windows providers generally cannot force a directory handle; the atomic
   * snapshot remains the recovery source there, while POSIX providers receive
   * the required rename durability barrier.
   */
  private void forceDirectoryEntryBeforeJournalTruncate() throws IOException {
    try (var channel = FileChannel.open(directory, StandardOpenOption.READ)) {
      channel.force(true);
    } catch (UnsupportedOperationException unsupported) {
      if (!isWindows()) throw unsupported;
    } catch (IOException failure) {
      if (!isWindows()) throw failure;
    }
  }

  private static boolean isWindows() {
    return System.getProperty("os.name", "").toLowerCase(java.util.Locale.ROOT).contains("win");
  }

  /** Windows scanners can transiently deny replacement while observing the completed temp file. */
  private static void replaceAtomically(Path temporary, Path snapshot) throws IOException {
    final int attempts = 5;
    for (int attempt = 0; ; attempt++) {
      try {
        Files.move(temporary, snapshot, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        return;
      } catch (AccessDeniedException e) {
        if (attempt + 1 >= attempts) throw e;
        try {
          Thread.sleep(10L << attempt);
        } catch (InterruptedException interrupted) {
          Thread.currentThread().interrupt();
          var failure = new java.io.InterruptedIOException("interrupted while retrying atomic label snapshot replacement");
          failure.initCause(interrupted);
          throw failure;
        }
      }
    }
  }

  private static PendingLabel readPendingLabel(DataInputStream data) throws IOException {
    String left = data.readUTF(), right = data.readUTF();
    String leftPayload = readBoolean(data) ? data.readUTF() : null;
    String rightPayload = readBoolean(data) ? data.readUTF() : null;
    return new PendingLabel(left, right, leftPayload, rightPayload);
  }

  private static boolean readBoolean(DataInputStream data) throws IOException {
    int value = data.readUnsignedByte();
    if (value > 1) throw new DuckException("label snapshot boolean is invalid");
    return value == 1;
  }

  private static void validateReceiptKeys(List<Delivery> deliveries, List<Enqueue> enqueues) {
    var deliveryKeys = new HashSet<String>();
    for (Delivery delivery : deliveries) {
      if (delivery.schemaVersion().isBlank() || delivery.idempotencyKey().isBlank()
          || !deliveryKeys.add(delivery.idempotencyKey()))
        throw new DuckException("label snapshot delivery receipt is invalid or duplicated");
    }
    var enqueueKeys = new HashSet<String>();
    for (Enqueue enqueue : enqueues) {
      if (enqueue.schemaVersion().isBlank() || enqueue.idempotencyKey().isBlank()
          || !enqueueKeys.add(enqueue.idempotencyKey()))
        throw new DuckException("label snapshot enqueue receipt is invalid or duplicated");
    }
  }

  private static void validateAppliedRequestKeys(List<ApplyLabelsRequest> requests) {
    var appliedKeys = new HashSet<String>();
    for (ApplyLabelsRequest request : requests) {
      if (!appliedKeys.add(request.idempotencyKey()))
        throw new DuckException("label snapshot applied request key is duplicated");
    }
  }

  private static void writePendingLabel(DataOutputStream data, PendingLabel label) throws IOException {
    data.writeUTF(label.leftId());
    data.writeUTF(label.rightId());
    data.writeBoolean(label.leftPayload() != null);
    if (label.leftPayload() != null) data.writeUTF(label.leftPayload());
    data.writeBoolean(label.rightPayload() != null);
    if (label.rightPayload() != null) data.writeUTF(label.rightPayload());
  }

  private static byte[] sha256(byte[] bytes) {
    try { return MessageDigest.getInstance("SHA-256").digest(bytes); }
    catch (NoSuchAlgorithmException e) { throw new IllegalStateException("SHA-256 unavailable", e); }
  }
}
