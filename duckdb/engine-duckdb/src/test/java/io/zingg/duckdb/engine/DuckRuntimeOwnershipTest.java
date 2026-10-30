package io.zingg.duckdb.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.zingg.duckdb.api.DuckException;
import io.zingg.duckdb.api.Frame;
import io.zingg.duckdb.api.JobHandle;
import java.nio.file.Path;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class DuckRuntimeOwnershipTest {
  @Test
  void runtimeDisablesImplicitExtensionResolutionAndLocksConfiguration(@TempDir Path temp)
      throws Exception {
    var config = new io.zingg.duckdb.api.RuntimeConfig(
        "jdbc:duckdb:" + temp.resolve("offline-policy.duckdb"), 1, 0, null, 1);
    try (var runtime = new DuckRuntime(config); var job = (DuckJob) runtime.openJob()) {
      try (var statement = job.connection().createStatement();
           var settings = statement.executeQuery("SELECT current_setting('autoinstall_known_extensions'), "
               + "current_setting('autoload_known_extensions'), current_setting('allow_community_extensions'), "
               + "current_setting('enable_external_access')")) {
        assertTrue(settings.next());
        assertEquals("false", settings.getString(1));
        assertEquals("false", settings.getString(2));
        assertEquals("false", settings.getString(3));
        assertEquals("false", settings.getString(4));
      }
      DuckException networkRead = assertThrows(DuckException.class,
          () -> job.sql("SELECT * FROM read_csv('https://example.invalid/never-fetch.csv')").count());
      var sqlFailure = assertInstanceOf(java.sql.SQLException.class, networkRead.getCause(),
          "offline URL denial must retain DuckDB's structured SQL failure");
      assertTrue(sqlFailure.getMessage() != null && !sqlFailure.getMessage().isBlank(),
          "offline URL denial must include actionable engine detail: " + sqlFailure);
      Path localCsv = temp.resolve("local-offline.csv");
      java.nio.file.Files.writeString(localCsv, "value\nlocal\n");
      assertEquals(1, job.sql("SELECT * FROM read_csv(" + DuckPath.sqlLiteral(localCsv) + ")").count(),
          "offline mode must retain the explicitly allowed local-file data path");
      assertThrows(java.sql.SQLException.class, () -> {
        try (var statement = job.connection().createStatement()) {
          statement.execute("SET autoload_known_extensions=true");
        }
      });
    }
    var overriddenUrl = new io.zingg.duckdb.api.RuntimeConfig(
        "jdbc:duckdb:" + temp.resolve("offline-policy-override.duckdb") + ";enable_external_access=true",
        1, 0, null, 1);
    assertThrows(DuckException.class, () -> new DuckRuntime(overriddenUrl),
        "a JDBC URL option must not override offline-mode security settings");
  }

  @Test
  void jobsHaveIndependentTemporaryStateAndRejectCrossOwnerJoin(@TempDir Path temp) throws Exception {
    var config = new io.zingg.duckdb.api.RuntimeConfig(
        "jdbc:duckdb:" + temp.resolve("ownership.duckdb"), 2, 0, null, 2);
    try (var runtime = new DuckRuntime(config)) {
      try (var first = runtime.openJob(); var second = runtime.openJob()) {
        try (var statement = ((DuckJob) first).connection().createStatement()) {
          statement.execute("create temporary table local_rows(value integer)");
          statement.execute("insert into local_rows values (7)");
        }
        assertEquals(1, first.sql("select * from local_rows").count());
        assertThrows(Exception.class, () -> second.sql("select * from local_rows").count());
        assertThrows(DuckException.class, () -> first.sql("select 1").join(second.sql("select 1"), "true"));
      }
    }
  }

  @Test
  void concurrentJobLimitBlocksUntilAJobCloses(@TempDir Path temp) throws Exception {
    var config = new io.zingg.duckdb.api.RuntimeConfig(
        "jdbc:duckdb:" + temp.resolve("concurrency.duckdb"), 2, 0, null, 1);
    ExecutorService executor = Executors.newSingleThreadExecutor();
    try (var runtime = new DuckRuntime(config); var first = runtime.openJob()) {
      Future<JobHandle> waiting = executor.submit(() -> runtime.openJob());
      Thread.sleep(150);
      assertEquals(false, waiting.isDone(), "the second job must wait for the configured slot");
      first.close();
      try (var second = waiting.get(5, TimeUnit.SECONDS)) {
        assertEquals(1, second.sql("select 1").count());
      }
    } finally {
      executor.shutdownNow();
      executor.awaitTermination(5, TimeUnit.SECONDS);
    }
  }

  @Test
  void repeatedJobCloseReleasesExactlyOneConnectionSlot(@TempDir Path temp) throws Exception {
    var config = new io.zingg.duckdb.api.RuntimeConfig(
        "jdbc:duckdb:" + temp.resolve("repeated-close.duckdb"), 2, 0, null, 1);
    ExecutorService executor = Executors.newSingleThreadExecutor();
    try (var runtime = new DuckRuntime(config)) {
      JobHandle first = runtime.openJob();
      first.close();
      first.close();

      try (var second = runtime.openJob()) {
        assertEquals(1, second.sql("select 1").count());
        Future<JobHandle> thirdFuture = executor.submit(() -> runtime.openJob());
        Thread.sleep(150);
        assertEquals(false, thirdFuture.isDone(),
            "closing one job twice must not release two concurrency permits");
        second.close();
        try (var third = thirdFuture.get(5, TimeUnit.SECONDS)) {
          assertEquals(1, third.sql("select 1").count());
        }
      }
    } finally {
      executor.shutdownNow();
      executor.awaitTermination(5, TimeUnit.SECONDS);
    }
  }

  @Test
  void lateMaterializationRegistrationIsRejectedAfterClose(@TempDir Path temp) {
    var config = new io.zingg.duckdb.api.RuntimeConfig(
        "jdbc:duckdb:" + temp.resolve("late-registration.duckdb"), 1, 0, null, 1);
    try (var runtime = new DuckRuntime(config)) {
      var job = (DuckJob) runtime.openJob();
      job.close();
      assertThrows(DuckException.class, () -> job.registerMaterialized("after_close"));
    }
  }

  @Test
  void failedReadOnlyJobConstructionReturnsTheOnlyJobSlot(@TempDir Path temp) {
    Path database = temp.resolve("read-only-job.duckdb");
    var writable = new io.zingg.duckdb.api.RuntimeConfig(
        "jdbc:duckdb:" + database, 1, 0, null, 1);
    try (var ignored = new DuckRuntime(writable)) {
      // Opening the runtime creates the database before the read-only attempt.
    }
    var readOnly = new io.zingg.duckdb.api.RuntimeConfig(
        "jdbc:duckdb:" + database + ";access_mode=READ_ONLY", 1, 0, null, 1);
    try (var runtime = new DuckRuntime(readOnly)) {
      for (int attempt = 0; attempt < 2; attempt++) {
        assertTimeoutPreemptively(Duration.ofSeconds(2),
            () -> assertThrows(DuckException.class, runtime::openJob),
            "a failed job constructor must not strand the sole concurrency permit");
      }
    }
  }

  @Test
  void closingAJobRemovesItsRunSchemaAndMaterializedCache(@TempDir Path temp) throws Exception {
    var config = new io.zingg.duckdb.api.RuntimeConfig(
        "jdbc:duckdb:" + temp.resolve("cleanup.duckdb"), 2, 0, null, 2);
    try (var runtime = new DuckRuntime(config)) {
      String schema;
      try (var first = runtime.openJob()) {
        schema = ((DuckJob) first).schemaName();
        assertEquals(2, first.sql("select * from (values (1), (2)) as t(value)").cache().count());
        assertEquals(1L, ((Number) first.sql("select count(*) AS n from information_schema.schemata where schema_name = '" + schema + "'").collect().get(0).get("n")).longValue());
      }
      try (var second = runtime.openJob()) {
        assertEquals(0L, ((Number) second.sql("select count(*) AS n from information_schema.schemata where schema_name = '" + schema + "'").collect().get(0).get("n")).longValue());
      }
    }
  }

  @Test
  void openingAnotherRuntimeDoesNotDeleteAnActiveRunSchema(@TempDir Path temp) throws Exception {
    var config = new io.zingg.duckdb.api.RuntimeConfig(
        "jdbc:duckdb:" + temp.resolve("concurrent-runtimes.duckdb"), 2, 0, null, 2);
    try (var firstRuntime = new DuckRuntime(config); var firstJob = firstRuntime.openJob()) {
      var cached = firstJob.sql("select * from range(3) t(value)").cache();
      assertEquals(3, cached.count());
      try (var statement = ((DuckJob) firstJob).connection().createStatement()) {
        statement.execute("CREATE SCHEMA user_data");
        statement.execute("CREATE TABLE user_data.keep_me(value INTEGER)");
        statement.execute("INSERT INTO user_data.keep_me VALUES (17)");
      }
      var first = (DuckJob) firstJob;
      try (var secondRuntime = new DuckRuntime(config); var secondJob = secondRuntime.openJob()) {
        assertEquals(1L, ((Number) secondJob.sql("SELECT COUNT(*) AS n FROM information_schema.tables WHERE table_schema = "
            + DuckExpr.literal(first.schemaName()).sql() + " AND table_name LIKE 'zd_cache_%'")
            .collect().get(0).get("n")).longValue());
        assertEquals(3, cached.count(), "opening a second runtime must preserve the active run's cached table");
        assertEquals(17, ((Number) secondJob.sql("SELECT value FROM user_data.keep_me")
            .collect().get(0).get("value")).intValue(),
            "startup recovery must preserve user schemas outside the reserved run namespace");
      }
    }
  }

  @Test
  void preparedBatchFramePreservesTypesAndCleansFailedMaterialization(@TempDir Path temp) throws Exception {
    var config = new io.zingg.duckdb.api.RuntimeConfig(
        "jdbc:duckdb:" + temp.resolve("prepared-batch-frame.duckdb"), 2, 0, null, 1);
    String schema;
    try (var runtime = new DuckRuntime(config); var handle = runtime.openJob()) {
      var job = (DuckJob) handle;
      schema = job.schemaName();
      var rows = new java.util.ArrayList<java.util.List<Object>>();
      for (int i = 0; i < 2_501; i++) rows.add(java.util.List.of((long) i, i / 10.0d));
      Frame frame = job.createFrame("batch_rows", java.util.List.of("id", "score"),
          java.util.List.of("BIGINT", "DOUBLE"), rows);
      assertEquals(2_501, frame.count());
      assertEquals(java.sql.Types.BIGINT, frame.schema().get(0).jdbcType());
      assertEquals(java.sql.Types.DOUBLE, frame.schema().get(1).jdbcType());

      assertThrows(DuckException.class, () -> job.createFrame("bad_rows",
          java.util.List.of("value"), java.util.List.of("VARCHAR"),
          java.util.List.of(java.util.List.of("ok"), java.util.List.of("wrong", "shape"))));
      assertEquals(0L, ((Number) handle.sql("SELECT COUNT(*) AS n FROM information_schema.tables WHERE table_schema = '"
          + schema + "' AND table_name = 'bad_rows'").collect().get(0).get("n")).longValue());
    }
    try (var runtime = new DuckRuntime(config); var handle = runtime.openJob()) {
      assertEquals(0L, ((Number) handle.sql("SELECT COUNT(*) AS n FROM information_schema.schemata WHERE schema_name = '"
          + schema + "'").collect().get(0).get("n")).longValue());
    }
  }

  @Test
  void cancelledTokenIsRejectedBeforeQueryExecution(@TempDir Path temp) throws Exception {
    var config = new io.zingg.duckdb.api.RuntimeConfig(
        "jdbc:duckdb:" + temp.resolve("cancel-before-start.duckdb"), 2, 0, null, 1);
    try (var runtime = new DuckRuntime(config); var job = runtime.openJob()) {
      var cancellation = new DuckCancellation();
      cancellation.cancel();
      assertThrows(DuckException.class,
          () -> job.sql("select * from range(1000000)").collect(cancellation));
    }
    try (var recovered = new DuckRuntime(config)) {
      try (var job = recovered.openJob()) {
        assertEquals(1, job.sql("select 1").count());
      }
    }
  }

  @Test
  void inFlightCancellationInterruptsQueryAndKeepsJobUsable(@TempDir Path temp) throws Exception {
    var config = new io.zingg.duckdb.api.RuntimeConfig(
        "jdbc:duckdb:" + temp.resolve("cancel-in-flight.duckdb"), 2, 0, null, 1);
    ExecutorService executor = Executors.newSingleThreadExecutor();
    try (var runtime = new DuckRuntime(config); var job = runtime.openJob()) {
      var cancellation = new DuckCancellation();
      Future<?> running = executor.submit(() -> job.sql("select sum(i) from range(1000000000000) t(i)")
          .collect(cancellation));
      Thread.sleep(100);
      cancellation.cancel();
      var failure = assertThrows(java.util.concurrent.ExecutionException.class,
          () -> running.get(5, TimeUnit.SECONDS));
      assertInstanceOf(DuckException.class, failure.getCause());
      assertEquals(1, job.sql("select 1").count(), "cancellation must not poison the job connection");
    } finally {
      executor.shutdownNow();
      executor.awaitTermination(5, TimeUnit.SECONDS);
    }
  }

  @Test
  void requestScopedCancellationCancelsOrdinaryFrameStatements(@TempDir Path temp) throws Exception {
    var config = new io.zingg.duckdb.api.RuntimeConfig(
        "jdbc:duckdb:" + temp.resolve("request-cancel.duckdb"), 2, 0, null, 1);
    ExecutorService executor = Executors.newSingleThreadExecutor();
    try (var runtime = new DuckRuntime(config)) {
      var cancellation = new DuckCancellation();
      Future<?> running = executor.submit(() -> {
        try (var ignored = DuckCancellation.install(cancellation);
             var job = (DuckJob) runtime.openJob()) {
          return job.sql("SELECT sum(i) FROM range(1000000000000) t(i)").count();
        }
      });
      Thread.sleep(100);
      cancellation.cancel();
      var failure = assertThrows(java.util.concurrent.ExecutionException.class,
          () -> running.get(5, TimeUnit.SECONDS));
      assertInstanceOf(DuckException.class, failure.getCause());
      try (var job = runtime.openJob()) {
        assertEquals(1, job.sql("SELECT 1").count(),
            "a cancelled worker request must leave the runtime available for its next request");
      }
    } finally {
      executor.shutdownNow();
      executor.awaitTermination(5, TimeUnit.SECONDS);
    }
  }

  @Test
  void scheduledCancellationEnforcesQueryDeadline(@TempDir Path temp) throws Exception {
    var config = new io.zingg.duckdb.api.RuntimeConfig(
        "jdbc:duckdb:" + temp.resolve("cancel-deadline.duckdb"), 2, 0, null, 1);
    ExecutorService executor = Executors.newSingleThreadExecutor();
    try (var runtime = new DuckRuntime(config); var job = runtime.openJob()) {
      var cancellation = new DuckCancellation();
      var deadline = cancellation.cancelAfter(Duration.ofMillis(100));
      try {
        Future<?> running = executor.submit(() -> job.sql("select sum(i) from range(1000000000000) t(i)")
            .collect(cancellation));
        var failure = assertThrows(java.util.concurrent.ExecutionException.class,
            () -> running.get(5, TimeUnit.SECONDS));
        assertInstanceOf(DuckException.class, failure.getCause());
      } finally {
        deadline.cancel(false);
      }
      assertEquals(1, job.sql("select 1").count(), "deadline cancellation must not poison the job connection");
    } finally {
      executor.shutdownNow();
      executor.awaitTermination(5, TimeUnit.SECONDS);
    }
  }

  @Test
  void configuredMemoryAndTempBudgetsAreAppliedAndObservable(@TempDir Path temp) throws Exception {
    Path tempDirectory = temp.resolve("duckdb-temp");
    var config = new io.zingg.duckdb.api.RuntimeConfig(
        "jdbc:duckdb:" + temp.resolve("budgets.duckdb"), 2, 64L * 1024 * 1024,
        tempDirectory, 1, 16L * 1024 * 1024);
    try (var runtime = new DuckRuntime(config)) {
      var diagnostics = runtime.diagnostics();
      assertEquals(tempDirectory.toAbsolutePath().normalize().toString(), diagnostics.tempDirectory());
      assertEquals("64.0 MiB", diagnostics.memoryLimit());
      assertEquals("16.0 MiB", diagnostics.maxTempDirectorySize());
    }
  }
}
