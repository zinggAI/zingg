package io.zingg.duckdb.model;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.BooleanSupplier;

/**
 * Coordinates model publication and reads across threads and JVMs.
 *
 * <p>The directory replacement protocol uses two renames.  The OS lock makes
 * that sequence one publication transaction to cooperating readers, while the
 * JVM lock avoids {@link OverlappingFileLockException} for readers and writers
 * in the same process.  The lock file is intentionally retained beside the
 * artifact so a reader never has to create a file inside the model directory.
 */
final class ModelPublicationLock implements AutoCloseable {
  private static final Object JVM_LOCKS_MONITOR = new Object();
  private static final Map<Path, Entry> JVM_LOCKS = new HashMap<>();
  private static final long POLL_MILLIS = 20L;

  private final Path identity;
  private final Entry entry;
  private final Lock jvmLock;
  private final FileChannel channel;
  private final FileLock fileLock;
  private boolean closed;

  private ModelPublicationLock(Path identity, Entry entry, Lock jvmLock,
      FileChannel channel, FileLock fileLock) {
    this.identity = identity;
    this.entry = entry;
    this.jvmLock = jvmLock;
    this.channel = channel;
    this.fileLock = fileLock;
  }

  static ModelPublicationLock read(Path root) throws IOException {
    return acquire(root, false, () -> false);
  }

  static ModelPublicationLock write(Path root, BooleanSupplier cancelled) throws IOException {
    return acquire(root, true, Objects.requireNonNull(cancelled, "cancelled"));
  }

  private static ModelPublicationLock acquire(Path root, boolean write,
      BooleanSupplier cancelled) throws IOException {
    Path artifact = root.toAbsolutePath().normalize();
    Path parent = artifact.getParent();
    if (parent == null || !Files.isDirectory(parent, LinkOption.NOFOLLOW_LINKS))
      throw new IOException("model artifact parent directory is required: " + artifact);
    Path lockPath = parent.resolve("." + artifact.getFileName() + ".lock").toAbsolutePath().normalize();
    if (Files.isSymbolicLink(lockPath))
      throw new IOException("model publication lock cannot be a symbolic link: " + lockPath);

    Entry entry;
    synchronized (JVM_LOCKS_MONITOR) {
      entry = JVM_LOCKS.computeIfAbsent(lockPath, ignored -> new Entry());
      entry.users++;
    }
    Lock jvmLock = write ? entry.lock.writeLock() : entry.lock.readLock();
    FileChannel channel = null;
    FileLock fileLock = null;
    boolean localAcquired = false;
    try {
      acquireJvm(jvmLock, cancelled);
      localAcquired = true;
      channel = FileChannel.open(lockPath, StandardOpenOption.CREATE,
          StandardOpenOption.READ, StandardOpenOption.WRITE, LinkOption.NOFOLLOW_LINKS);
      while (fileLock == null) {
        checkCancelled(cancelled);
        try {
          fileLock = channel.tryLock(0L, Long.MAX_VALUE, !write);
        } catch (OverlappingFileLockException busyInProvider) {
          // A provider can report overlap even after the JVM gate has been
          // acquired. Treat it as transient and keep cancellation responsive.
        }
        if (fileLock == null) pause(cancelled);
      }
      return new ModelPublicationLock(lockPath, entry, jvmLock, channel, fileLock);
    } catch (IOException | RuntimeException | Error failure) {
      if (fileLock != null) try { fileLock.release(); } catch (IOException suppressed) { failure.addSuppressed(suppressed); }
      if (channel != null) try { channel.close(); } catch (IOException suppressed) { failure.addSuppressed(suppressed); }
      if (localAcquired) jvmLock.unlock();
      releaseEntry(lockPath, entry);
      throw failure;
    }
  }

  private static void acquireJvm(Lock lock, BooleanSupplier cancelled) {
    while (true) {
      checkCancelled(cancelled);
      try {
        if (lock.tryLock(POLL_MILLIS, TimeUnit.MILLISECONDS)) return;
      } catch (InterruptedException interrupted) {
        Thread.currentThread().interrupt();
        throw new ModelPublicationCancelledException("interrupted waiting for model publication lock", interrupted);
      }
    }
  }

  private static void pause(BooleanSupplier cancelled) {
    try {
      Thread.sleep(POLL_MILLIS);
    } catch (InterruptedException interrupted) {
      Thread.currentThread().interrupt();
      throw new ModelPublicationCancelledException("interrupted waiting for model publication lock", interrupted);
    }
    checkCancelled(cancelled);
  }

  private static void checkCancelled(BooleanSupplier cancelled) {
    if (cancelled.getAsBoolean())
      throw new ModelPublicationCancelledException("model artifact publication was cancelled");
  }

  private static void releaseEntry(Path identity, Entry entry) {
    synchronized (JVM_LOCKS_MONITOR) {
      if (--entry.users == 0) JVM_LOCKS.remove(identity, entry);
    }
  }

  @Override public void close() throws IOException {
    if (closed) return;
    closed = true;
    IOException failure = null;
    try { fileLock.release(); }
    catch (IOException error) { failure = error; }
    try { channel.close(); }
    catch (IOException error) { if (failure == null) failure = error; else failure.addSuppressed(error); }
    jvmLock.unlock();
    releaseEntry(identity, entry);
    if (failure != null) throw failure;
  }

  private static final class Entry {
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock(true);
    private int users;
  }

  static final class ModelPublicationCancelledException extends RuntimeException {
    private ModelPublicationCancelledException(String message) { super(message); }
    private ModelPublicationCancelledException(String message, Throwable cause) { super(message, cause); }
  }
}
