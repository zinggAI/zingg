package io.zingg.duckdb.worker;

import io.zingg.duckdb.api.DuckException;
import io.zingg.duckdb.api.RuntimeConfig;
import io.zingg.duckdb.compat.CompatibilityRuntime;
import io.zingg.duckdb.compat.NativeTrainingConfig;
import io.zingg.duckdb.engine.DuckRuntime;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/** Docker smoke helper: requires OS-level spill exhaustion on a size-limited mount. */
public final class SpillQuotaProbe {
  private SpillQuotaProbe() {}

  public static void main(String[] args) throws Exception {
    if (args.length != 1) throw new IllegalArgumentException("spill directory is required");
    Path spill = Path.of(args[0]).toAbsolutePath().normalize();
    DuckException queryFailure = null;
    var peakBytes = new AtomicLong();
    var config = new RuntimeConfig("jdbc:duckdb:", 2, 64L * 1024 * 1024,
        spill, 1, 1024L * 1024 * 1024);
    try (var runtime = new DuckRuntime(config); var job = runtime.openJob()) {
      String configuredLimit = runtime.diagnostics().maxTempDirectorySize();
      if (!configuredLimit.contains("1.0 GiB"))
        throw new AssertionError("expected a 1 GiB DuckDB spill limit, got " + configuredLimit);
      Thread watcher = new Thread(() -> {
        try {
          while (!Thread.currentThread().isInterrupted()) {
            long current;
            try (var entries = Files.list(spill)) {
              current = entries.filter(Files::isRegularFile).mapToLong(path -> {
                try { return Files.size(path); }
                catch (Exception ignored) { return 0L; }
              }).sum();
            }
            peakBytes.accumulateAndGet(current, Math::max);
            Thread.sleep(1);
          }
        } catch (InterruptedException ignored) { Thread.currentThread().interrupt(); }
        catch (Exception failure) { throw new AssertionError(failure); }
      }, "spill-quota-probe-observer");
      watcher.start();
      try {
        job.sql("SELECT sum(i) AS total FROM (SELECT i FROM range(20000000) t(i) ORDER BY hash(i))")
            .collect();
      } catch (DuckException failure) {
        queryFailure = failure;
      } finally {
        watcher.interrupt();
        watcher.join(5000);
      }
    }
    if (queryFailure == null)
      throw new AssertionError("spill workload unexpectedly completed on the bounded mount");
    String diagnostic = queryFailure.toString();
    for (Throwable cause = queryFailure.getCause(); cause != null; cause = cause.getCause())
      diagnostic += " | " + cause;
    long minimumExpectedPeak = 8L * 1024 * 1024;
    if (peakBytes.get() < minimumExpectedPeak)
      throw new AssertionError("bounded mount did not experience substantial spill pressure: peak="
          + peakBytes.get() + " bytes; failure=" + diagnostic, queryFailure);
    try (var entries = Files.list(spill)) {
      long remaining = entries.count();
      if (remaining != 0)
        throw new AssertionError("DuckDB left " + remaining + " spill entries after runtime close");
    }
    Path model = spill.resolve("oversized-native-model");
    try (var runtime = new CompatibilityRuntime(new RuntimeConfig("jdbc:duckdb:", 1,
            64L * 1024 * 1024, spill, 1, 1024L * 1024 * 1024), null);
        var job = runtime.openJob()) {
      var trainingInput = job.sql("SELECT * FROM (VALUES (1, 1, 'same'), (2, 2, 'same')) AS t(id, z_zid, block)");
      try {
        new io.zingg.duckdb.compat.NativeTrainingService().train(trainingInput,
            new NativeTrainingConfig("docker-probe", "\"block\"", "training_block",
                java.util.List.of(), model, 100, 1));
        throw new AssertionError("one-byte model artifact budget unexpectedly succeeded");
      } catch (DuckException expected) {
        if (expected.getMessage() == null
            || !expected.getMessage().startsWith("model artifact byte budget exceeded:"))
          throw expected;
      }
      if (Files.exists(model)) throw new AssertionError("rejected model left its destination directory");
    }
    try (var entries = Files.list(spill)) {
      if (entries.anyMatch(path -> path.getFileName().toString().startsWith(".model-artifact-")
          || path.getFileName().toString().equals("oversized-native-model")))
        throw new AssertionError("rejected model left destination/staging content");
    }
    int concurrentFailures = verifyConcurrentWorkersShareSpillQuota(spill);
    System.out.println("SPILL_TMPFS_QUOTA_REJECTED_AND_CLEANED peakBytes=" + peakBytes.get()
        + " maxTempDirectorySize=1GiB MODEL_ARTIFACT_BUDGET_REJECTED_AND_CLEANED"
        + " CONCURRENT_SPILL_QUOTA_REJECTED_AND_CLEANED failedWorkers=" + concurrentFailures
        + " diagnostic=" + diagnostic);
  }

  private static int verifyConcurrentWorkersShareSpillQuota(Path spill) throws Exception {
    var ready = new CountDownLatch(2);
    var start = new CountDownLatch(1);
    var peakBytes = new AtomicLong();
    var pool = Executors.newFixedThreadPool(2);
    Thread watcher = new Thread(() -> {
      try {
        while (!Thread.currentThread().isInterrupted()) {
          long current;
          try (var entries = Files.list(spill)) {
            current = entries.filter(Files::isRegularFile).mapToLong(path -> {
              try { return Files.size(path); }
              catch (Exception ignored) { return 0L; }
            }).sum();
          }
          peakBytes.accumulateAndGet(current, Math::max);
          Thread.sleep(1);
        }
      } catch (InterruptedException ignored) { Thread.currentThread().interrupt(); }
      catch (Exception failure) { throw new AssertionError(failure); }
    }, "concurrent-spill-quota-observer");
    watcher.start();
    try {
      var workers = java.util.stream.IntStream.range(0, 2).mapToObj(worker -> pool.submit(() -> {
        try (var runtime = new DuckRuntime(new RuntimeConfig("jdbc:duckdb:", 2,
                64L * 1024 * 1024, spill, 1, 1024L * 1024 * 1024));
            var job = runtime.openJob()) {
          ready.countDown();
          if (!start.await(10, TimeUnit.SECONDS))
            throw new AssertionError("concurrent spill workers did not reach the start barrier");
          try {
            job.sql("SELECT sum(i) AS total FROM (SELECT i FROM range(20000000) t(i) ORDER BY hash(i))")
                .collect();
            return null;
          } catch (DuckException expectedQuotaFailure) {
            return expectedQuotaFailure;
          }
        }
      })).toList();
      if (!ready.await(30, TimeUnit.SECONDS))
        throw new AssertionError("concurrent spill workers failed to initialize");
      start.countDown();
      int failures = 0;
      for (var worker : workers) {
        DuckException failure = worker.get(90, TimeUnit.SECONDS);
        if (failure != null) failures++;
      }
      if (failures == 0)
        throw new AssertionError("both concurrent spill workloads completed within a shared 16 MiB quota");
      if (peakBytes.get() < 8L * 1024 * 1024)
        throw new AssertionError("concurrent workers did not create substantial shared spill pressure: "
            + peakBytes.get());
      return failures;
    } finally {
      start.countDown();
      pool.shutdownNow();
      if (!pool.awaitTermination(10, TimeUnit.SECONDS))
        throw new AssertionError("concurrent spill workers did not terminate");
      watcher.interrupt();
      watcher.join(5000);
      try (var entries = Files.list(spill)) {
        long remaining = entries.count();
        if (remaining != 0)
          throw new AssertionError("concurrent workers left " + remaining + " spill entries");
      }
    }
  }
}
