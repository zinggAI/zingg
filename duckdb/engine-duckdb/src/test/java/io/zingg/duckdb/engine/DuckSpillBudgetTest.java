package io.zingg.duckdb.engine;

import static org.junit.jupiter.api.Assertions.assertThrows;

import io.zingg.duckdb.api.RuntimeConfig;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class DuckSpillBudgetTest {
  @Test
  void boundedSortCanCompleteWithConfiguredTempBudget(@TempDir Path temp) throws Exception {
    Path database = temp.resolve("spill-success.duckdb");
    Path tempDirectory = temp.resolve("duckdb-temp-success");
    Files.createDirectories(tempDirectory);
    var config = new RuntimeConfig(
        "jdbc:duckdb:" + database, 1, 64L * 1024 * 1024, tempDirectory, 1, 512L * 1024 * 1024);
    try (var runtime = new DuckRuntime(config); var job = runtime.openJob()) {
      var observedSpill = new AtomicBoolean();
      Thread watcher = new Thread(() -> {
        try {
          while (!Thread.currentThread().isInterrupted()) {
            try (var paths = Files.list(tempDirectory)) {
              if (paths.anyMatch(path -> {
                try { return Files.isRegularFile(path) && Files.size(path) > 0; }
                catch (Exception ignored) { return false; }
              })) observedSpill.set(true);
            }
            Thread.sleep(10);
          }
        } catch (InterruptedException ignored) { Thread.currentThread().interrupt(); }
        catch (Exception ignored) { }
      }, "duckdb-spill-observer");
      watcher.start();
      var rows = job.sql(
          "select sum(i) as total from (select i from range(5000000) t(i) order by hash(i))")
          .collect();
      watcher.interrupt();
      watcher.join(5000);
      org.junit.jupiter.api.Assertions.assertEquals(1, rows.size());
      org.junit.jupiter.api.Assertions.assertTrue(observedSpill.get(), "DuckDB did not expose a non-empty spill file");
    }
  }

  @Test
  void boundedOrderedAggregationRejectsWhenMemoryBudgetCannotBeSatisfied(@TempDir Path temp) throws Exception {
    Path database = temp.resolve("spill.duckdb");
    Path tempDirectory = temp.resolve("duckdb-temp");
    Files.createDirectories(tempDirectory);
    var config = new RuntimeConfig(
        "jdbc:duckdb:" + database, 2, 16L * 1024 * 1024, tempDirectory, 1, 256L * 1024 * 1024);
    try (var runtime = new DuckRuntime(config); var job = runtime.openJob()) {
      assertThrows(io.zingg.duckdb.api.DuckException.class, () -> job.sql(
          "select sum(i) as total from (select i from range(2000000) t(i) order by hash(i))")
          .collect());
    }
  }

}
