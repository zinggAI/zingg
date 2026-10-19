package io.zingg.duckdb.engine;

import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/** Best-effort periodic RSS and DuckDB temp-directory sampling for diagnostics. */
final class BenchmarkResourceMonitor implements AutoCloseable {
  private final Path spillDirectory;
  private final long intervalMillis;
  private final AtomicLong maxRssBytes = new AtomicLong(-1);
  private final AtomicLong maxSpillBytes = new AtomicLong(-1);
  private final AtomicInteger samples = new AtomicInteger();
  private volatile boolean running = true;
  private final Thread sampler;

  BenchmarkResourceMonitor(Path spillDirectory, long intervalMillis) {
    if (spillDirectory == null || intervalMillis < 1) throw new IllegalArgumentException("spill directory and positive interval required");
    this.spillDirectory = spillDirectory;
    this.intervalMillis = intervalMillis;
    sample();
    sampler = new Thread(this::sampleWhileRunning, "zingg-benchmark-resource-sampler");
    sampler.setDaemon(true);
    sampler.start();
  }

  private void sampleWhileRunning() {
    while (running) {
      try { Thread.sleep(intervalMillis); }
      catch (InterruptedException e) { Thread.currentThread().interrupt(); return; }
      if (running) sample();
    }
  }

  private void sample() {
    updateMaximum(maxRssBytes, ProcessResourceSnapshot.residentBytes());
    updateMaximum(maxSpillBytes, ProcessResourceSnapshot.directoryBytes(spillDirectory.toString()));
    samples.incrementAndGet();
  }

  private static void updateMaximum(AtomicLong maximum, long value) {
    if (value < 0) return;
    maximum.accumulateAndGet(value, Math::max);
  }

  @Override public void close() {
    running = false;
    try { sampler.join(2500); }
    catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    sample(); // include the end boundary; timing must stop before close() is called
  }

  long maxRssBytes() { return maxRssBytes.get(); }
  long maxSpillBytes() { return maxSpillBytes.get(); }
  int samples() { return samples.get(); }
  long intervalMillis() { return intervalMillis; }
}
