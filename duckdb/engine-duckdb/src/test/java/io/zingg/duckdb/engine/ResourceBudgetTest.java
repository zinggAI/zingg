package io.zingg.duckdb.engine;

import static org.junit.jupiter.api.Assertions.*;

import io.zingg.duckdb.api.DuckException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import java.nio.file.Files;
import java.util.List;
import java.sql.Statement;
import java.util.concurrent.atomic.AtomicBoolean;

class ResourceBudgetTest {
  @Test
  void inputAndOutputBudgetsAreIndependentFromSpillBudget() {
    var budget = new ResourceBudget(0, 0, 10, 20, 30);
    assertThrows(DuckException.class, () -> budget.enforceInputBytes(11));
    assertThrows(DuckException.class, () -> budget.enforceOutputBytes(21));
    budget.enforceInputBytes(10);
    budget.enforceOutputBytes(20);
  }

  @Test
  void collectBudgetIsIndependentAndEnforced() {
    var budget = new ResourceBudget(0, 0, 0, 0, 16, 0);
    assertThrows(DuckException.class, () -> budget.enforceCollectBytes(17));
    budget.enforceCollectBytes(16);
  }

  @Test
  void spillBudgetCancelsAnActiveStatementWhenTheTempDirectoryGrows(@TempDir Path temp)
      throws Exception {
    AtomicBoolean cancelled = new AtomicBoolean();
    Statement raw = fakeStatement(cancelled);
    Statement guarded = SpillByteGuard.track(raw, temp, 1);
    guarded.execute("SELECT 1");
    Files.writeString(temp.resolve("spill.bin"), "spill");

    long deadline = System.nanoTime() + java.util.concurrent.TimeUnit.SECONDS.toNanos(5);
    while (!cancelled.get() && System.nanoTime() < deadline) Thread.sleep(5);
    assertTrue(cancelled.get(), "spill overage must cancel the active statement");
    guarded.close();
  }

  @Test
  void spillBudgetIsSharedAcrossActiveStatements(@TempDir Path temp) throws Exception {
    AtomicBoolean firstCancelled = new AtomicBoolean();
    AtomicBoolean secondCancelled = new AtomicBoolean();
    Statement first = SpillByteGuard.track(fakeStatement(firstCancelled), temp, 1);
    Statement second = SpillByteGuard.track(fakeStatement(secondCancelled), temp, 1);
    try {
      first.execute("SELECT 1");
      second.execute("SELECT 1");
      Files.writeString(temp.resolve("shared-spill.bin"), "spill");
      long deadline = System.nanoTime() + java.util.concurrent.TimeUnit.SECONDS.toNanos(5);
      while ((!firstCancelled.get() || !secondCancelled.get()) && System.nanoTime() < deadline)
        Thread.sleep(5);
      assertTrue(firstCancelled.get(), "first active job must be cancelled by the shared cap");
      assertTrue(secondCancelled.get(), "second active job must be cancelled by the shared cap");
    } finally {
      first.close();
      second.close();
    }
  }

  private static Statement fakeStatement(AtomicBoolean cancelled) {
    return (Statement) java.lang.reflect.Proxy.newProxyInstance(
        Statement.class.getClassLoader(), new Class<?>[] {Statement.class}, (proxy, method, args) -> {
          if (method.getName().equals("cancel")) { cancelled.set(true); return null; }
          if (method.getName().equals("execute")) return false;
          if (method.getName().equals("close")) return null;
          Class<?> type = method.getReturnType();
          if (type == boolean.class) return false;
          if (type == int.class) return 0;
          if (type == long.class) return 0L;
          return null;
        });
  }

  @Test
  void rowLimitIsInclusiveAtTheBoundaryAndRejectsTheFirstExcessRow(@TempDir Path temp) {
    var config = new io.zingg.duckdb.api.RuntimeConfig(
        "jdbc:duckdb:" + temp.resolve("row-limit.duckdb"), 1, 0, null, 1);
    try (var runtime = new DuckRuntime(config);
         var job = runtime.openJob(new ResourceBudget(0, 3, 0, 0, 0, 0))) {
      var atLimit = job.sql("SELECT i FROM range(3) t(i)");
      assertEquals(3, atLimit.count());
      assertEquals(3, atLimit.collect().size());

      var justOverLimit = job.sql("SELECT i FROM range(4) t(i)");
      DuckException countFailure = assertThrows(DuckException.class, justOverLimit::count);
      assertEquals("row budget exceeded: 4 > 3", countFailure.getMessage());
      var large = job.sql("SELECT i FROM range(100000) t(i)");
      DuckException collectFailure = org.junit.jupiter.api.Assertions.assertTimeoutPreemptively(
          java.time.Duration.ofSeconds(10),
          () -> assertThrows(DuckException.class, large::collect));
      assertEquals("row budget exceeded: 4 > 3", collectFailure.getMessage());
    }
  }

  @Test
  void runtimeAppliesAndReportsLowResourceSettings(@TempDir Path temp) {
    var config = new io.zingg.duckdb.api.RuntimeConfig(
        "jdbc:duckdb:" + temp.resolve("limits.duckdb"), 2, 64L * 1024 * 1024,
        temp.resolve("spill"), 1, 128L * 1024 * 1024);
    try (var runtime = new DuckRuntime(config)) {
      var diagnostics = runtime.diagnostics();
      org.junit.jupiter.api.Assertions.assertTrue(diagnostics.memoryLimit().contains("64"));
      org.junit.jupiter.api.Assertions.assertTrue(diagnostics.maxTempDirectorySize().contains("128"));
      org.junit.jupiter.api.Assertions.assertEquals("2", diagnostics.threads());
    }
  }

  @Test
  void rejectedOutputLeavesDestinationAndNoPartialFile(@TempDir Path temp) throws Exception {
    Path output = temp.resolve("result.csv");
    Files.writeString(output, "original\n");
    var config = new io.zingg.duckdb.api.RuntimeConfig(
        "jdbc:duckdb:" + temp.resolve("output.duckdb"), 2, 0, null, 1);
    try (var runtime = new DuckRuntime(config);
         var job = runtime.openJob(new ResourceBudget(0, 0, 0, 1, 0, 0))) {
      assertThrows(DuckException.class,
          () -> job.sql("select * from range(100) t(i)").writeCsv(output, true));
    }
    assertEquals("original\n", Files.readString(output));
    try (var paths = Files.list(temp)) {
      assertTrue(paths.noneMatch(path -> path.getFileName().toString().endsWith(".partial")));
    }
  }

  @Test
  void oversizedCopyIsCancelledWhileWritingAndPreservesDestination(@TempDir Path temp) throws Exception {
    Path output = temp.resolve("large-result.csv");
    Files.writeString(output, "previous-complete-output\n");
    var config = new io.zingg.duckdb.api.RuntimeConfig(
        "jdbc:duckdb:" + temp.resolve("early-output-limit.duckdb"), 2, 0, null, 1);
    var budget = new ResourceBudget(0, 0, 0, 1024, 0, 0);
    try (var runtime = new DuckRuntime(config);
         var job = runtime.openJob(budget)) {
      DuckException failure = assertThrows(DuckException.class, () -> job.sql(
          "select repeat('x', 4096) as payload from range(10000) t(i)").writeCsv(output, true));
      assertTrue(failure.getMessage().contains("output byte budget exceeded"), failure.toString());
    }
    assertEquals("previous-complete-output\n", Files.readString(output));
    try (var paths = Files.list(temp)) {
      assertTrue(paths.noneMatch(path -> path.getFileName().toString().endsWith(".partial")));
    }
  }

  @Test
  void oversizedArrowExportIsCancelledAndPreservesDestination(@TempDir Path temp) throws Exception {
    Path output = temp.resolve("large-result.arrow");
    Files.writeString(output, "previous-complete-output\n");
    var config = new io.zingg.duckdb.api.RuntimeConfig(
        "jdbc:duckdb:" + temp.resolve("early-arrow-limit.duckdb"), 2, 0, null, 1);
    try (var runtime = new DuckRuntime(config);
         var job = runtime.openJob(new ResourceBudget(0, 0, 0, 1024, 0, 0))) {
      DuckException failure = assertThrows(DuckException.class, () -> job.sql(
          "select repeat('x', 4096) as payload from range(10000) t(i)").writeArrow(output));
      assertTrue(failure.getMessage().contains("output byte budget exceeded"), failure.toString());
    }
    assertEquals("previous-complete-output\n", Files.readString(output));
    try (var paths = Files.list(temp)) {
      assertTrue(paths.noneMatch(path -> path.getFileName().toString().endsWith(".partial")));
    }
  }

  @Test
  void everyFileFormatEnforcesOutputBudgetAndPreservesTheDestination(@TempDir Path temp)
      throws Exception {
    var config = new io.zingg.duckdb.api.RuntimeConfig(
        "jdbc:duckdb:" + temp.resolve("all-output-formats.duckdb"), 2, 0, null, 1);
    try (var runtime = new DuckRuntime(config);
         var job = runtime.openJob(new ResourceBudget(0, 0, 0, 1024, 0, 0))) {
      var frame = job.sql(
          "select repeat('x', 4096) as payload from range(10000) t(i)");
      for (var format : java.util.List.of("csv", "parquet", "json", "arrow")) {
        Path output = temp.resolve("oversized." + format);
        Files.writeString(output, "previous complete output");
        DuckException failure = assertThrows(DuckException.class, () -> {
          switch (format) {
            case "csv" -> frame.writeCsv(output, true);
            case "parquet" -> frame.writeParquet(output);
            case "json" -> frame.writeJson(output);
            case "arrow" -> frame.writeArrow(output);
            default -> throw new AssertionError(format);
          }
        });
        assertTrue(failure.getMessage().contains("output byte budget exceeded"),
            format + ": " + failure);
        assertEquals("previous complete output", Files.readString(output), format);
      }
    }
    try (var paths = Files.list(temp)) {
      assertTrue(paths.noneMatch(path -> path.getFileName().toString().endsWith(".partial")));
    }
  }

  @Test
  void outputPublicationFailureIsFailClosedForAllFileFormats(@TempDir Path temp)
      throws Exception {
    var config = new io.zingg.duckdb.api.RuntimeConfig(
        "jdbc:duckdb:" + temp.resolve("output-publication.duckdb"), 1, 0, null, 1);
    var unsupportedAtomicMove = (OutputPublisher) (staged, destination) -> {
      throw new java.nio.file.AtomicMoveNotSupportedException(staged.toString(),
          destination.toString(), "injected unsupported atomic publication");
    };
    try (var runtime = new DuckRuntime(config); var job = runtime.openJob()) {
      var frame = new DuckFrame((DuckJob) job, "SELECT 1 AS value", List.of(),
          RelationScope.QUERY_ONLY, false, unsupportedAtomicMove);
      for (var format : java.util.List.of("csv", "parquet", "json", "arrow")) {
        Path output = temp.resolve("atomic." + format);
        Files.writeString(output, "previous complete output");
        assertThrows(DuckException.class, () -> {
          switch (format) {
            case "csv" -> frame.writeCsv(output, true);
            case "parquet" -> frame.writeParquet(output);
            case "json" -> frame.writeJson(output);
            case "arrow" -> frame.writeArrow(output);
            default -> throw new AssertionError(format);
          }
        }, format);
        assertEquals("previous complete output", Files.readString(output), format);
      }
    }
    try (var paths = Files.list(temp)) {
      assertTrue(paths.noneMatch(path -> path.getFileName().toString().endsWith(".partial")));
    }
  }

  @Test
  void cancelledDirectExportNeverPublishesAnySupportedFileFormat(@TempDir Path temp)
      throws Exception {
    var config = new io.zingg.duckdb.api.RuntimeConfig(
        "jdbc:duckdb:" + temp.resolve("cancelled-output.duckdb"), 1, 0, null, 1);
    try (var runtime = new DuckRuntime(config)) {
      for (var format : java.util.List.of("csv", "parquet", "json", "arrow")) {
        Path output = temp.resolve("cancelled." + format);
        Files.writeString(output, "previous complete output");
        var cancellation = new DuckCancellation();
        try (var ignored = DuckCancellation.install(cancellation);
             var job = runtime.openJob()) {
          var frame = job.sql("SELECT 1 AS value");
          cancellation.cancel();
          DuckException failure = assertThrows(DuckException.class, () -> {
            switch (format) {
              case "csv" -> frame.writeCsv(output, true);
              case "parquet" -> frame.writeParquet(output);
              case "json" -> frame.writeJson(output);
              case "arrow" -> frame.writeArrow(output);
              default -> throw new AssertionError(format);
            }
          }, format);
          assertTrue(failure.getMessage().contains("operation cancelled"),
              format + ": " + failure);
        }
        assertEquals("previous complete output", Files.readString(output), format);
      }
    }
    try (var paths = Files.list(temp)) {
      assertTrue(paths.noneMatch(path -> path.getFileName().toString().endsWith(".partial")),
          "cancelled output must not leave any format's staged file");
    }
  }
}
