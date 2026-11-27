package io.zingg.duckdb.engine;

import io.zingg.duckdb.api.DuckException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Statement;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.LockSupport;

/** Best-effort early cancellation when an unpublished output exceeds its byte budget. */
final class OutputByteGuard implements AutoCloseable {
  private static final long POLL_NANOS = 2_000_000L;

  private final Path path;
  private final Path tempDirectory;
  private final long baselineTempBytes;
  private final long limit;
  private final Statement statement;
  private final AtomicBoolean stopped = new AtomicBoolean();
  private final AtomicLong observedBytes = new AtomicLong();
  private final AtomicLong observedTempBytes = new AtomicLong();
  private final AtomicBoolean exceeded = new AtomicBoolean();
  private final AtomicBoolean inspectionFailed = new AtomicBoolean();
  private final Thread watcher;

  OutputByteGuard(Path path, ResourceBudget budget, Statement statement, Path tempDirectory) {
    this.path = path;
    this.limit = budget.maxOutputBytes();
    this.statement = statement;
    this.tempDirectory = tempDirectory == null ? null : tempDirectory.toAbsolutePath().normalize();
    this.baselineTempBytes = this.tempDirectory == null ? 0L
        : ProcessResourceSnapshot.directoryBytes(this.tempDirectory.toString());
    if (limit > 0) {
      watcher = new Thread(this::watch, "zingg-output-budget");
      watcher.setDaemon(true);
      watcher.start();
    } else {
      watcher = null;
    }
  }

  private void watch() {
    while (!stopped.get()) {
      try {
        long size = Files.exists(path) ? Files.size(path) : 0L;
        observedBytes.accumulateAndGet(size, Math::max);
        long tempBytes = tempDirectory == null ? 0L
            : ProcessResourceSnapshot.directoryBytes(tempDirectory.toString());
        if (tempBytes < 0L) {
          inspectionFailed.set(true);
          cancelStatement();
          return;
        }
        observedTempBytes.accumulateAndGet(tempBytes, Math::max);
        long tempDelta = Math.max(0L, tempBytes - baselineTempBytes);
        boolean stagedIsInTemp = tempDirectory != null && path.toAbsolutePath().normalize().startsWith(tempDirectory);
        boolean over = stagedIsInTemp ? tempDelta > limit
            : tempDelta > limit || size > limit - tempDelta;
        if (over) {
          exceeded.set(true);
          cancelStatement();
          return;
        }
      } catch (IOException failure) {
        inspectionFailed.set(true);
        cancelStatement();
        return;
      }
      LockSupport.parkNanos(POLL_NANOS);
    }
  }

  private void cancelStatement() {
    try { statement.cancel(); }
    catch (Exception ignored) { /* The caller reports the observed limit after JDBC unwinds. */ }
  }

  void throwIfExceeded() {
    if (exceeded.get())
      throw new DuckException("output byte budget exceeded: staged="
          + observedBytes.get() + ", tempDelta=" + Math.max(0L, observedTempBytes.get() - baselineTempBytes)
          + " > " + limit + " bytes");
    if (inspectionFailed.get())
      throw new DuckException("cannot inspect staged output while enforcing byte budget: " + path);
  }

  @Override public void close() {
    stopped.set(true);
    if (watcher == null) return;
    watcher.interrupt();
    try { watcher.join(1000); }
    catch (InterruptedException e) { Thread.currentThread().interrupt(); }
  }
}
