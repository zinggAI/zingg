package io.zingg.duckdb.worker;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.zingg.duckdb.protocol.ProtocolCodec;
import io.zingg.duckdb.protocol.WorkerMessage;
import io.zingg.duckdb.api.PathPolicy;
import io.zingg.duckdb.compat.CompatibilityRuntime;
import io.zingg.duckdb.compat.LocalPhaseExecutors;
import io.zingg.duckdb.compat.Matcher;
import io.zingg.duckdb.compat.TrainingPlan;
import io.zingg.duckdb.compat.ZinggJob;
import io.zingg.duckdb.api.RuntimeConfig;
import io.zingg.duckdb.protocol.PayloadCodec;
import java.io.BufferedReader;
import java.io.PipedReader;
import java.io.PipedWriter;
import java.util.ArrayList;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.channels.Channels;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ConfiguredWorkerServerTest {
  @Test
  void ambiguousLegacyTrainOperationIsRejectedInFavorOfExplicitSchemas() throws Exception {
    String requests = ProtocolCodec.encode(new WorkerMessage("legacy", "train", "")) + "\n"
        + ProtocolCodec.encode(new WorkerMessage("stop", "shutdown", "")) + "\n";
    var output = new StringWriter();
    new ConfiguredWorkerServer(null, null, 0, 0, 0, 0, 0, false)
        .serve(new StringReader(requests), output);

    var responses = java.util.Arrays.stream(output.toString().split("\\R"))
        .map(ProtocolCodec::decode).toList();
    assertEquals("legacy", responses.get(0).id());
    assertEquals("error", responses.get(0).operation());
    assertTrue(responses.get(0).payload().contains("operation unavailable"));
    assertEquals(new WorkerMessage("stop", "ok", "stopping"), responses.get(1));
  }

  @Test
  void shutdownIsBoundedWhenAnAcceptedRequestDoesNotFinishImmediately() throws Exception {
    var config = new RuntimeConfig("jdbc:duckdb:", 1, 0, null, 1);
    try (var runtime = new CompatibilityRuntime(config, null);
         var executor = Executors.newSingleThreadExecutor()) {
      var server = new ConfiguredWorkerServer(runtime, new PathPolicy(null, null),
          0, 0, 0, 0, 0, true);
      String requests = ProtocolCodec.encode(new WorkerMessage("slow", "count",
          "SELECT sum(i) FROM range(1000000000000000000) t(i)")) + "\n"
          + ProtocolCodec.encode(new WorkerMessage("stop", "shutdown", "")) + "\n";
      var output = new StringWriter();
      var completed = executor.submit(() -> {
        try {
          server.serve(new StringReader(requests), output);
        } catch (Exception failure) {
          throw new RuntimeException(failure);
        }
      });
      try {
        completed.get(9, TimeUnit.SECONDS);
      } catch (TimeoutException timeout) {
        completed.cancel(true);
        throw new AssertionError("shutdown waited beyond the bounded worker grace period", timeout);
      }
      assertTrue(output.toString().contains("stop" + '\t' + "ok" + '\t' + "stopping"), output.toString());
    }
  }

  @Test
  void boundsInputWhileReadingAndContinuesAfterOversizedLine() throws Exception {
    String prefix="boundary\tping\t";
    String exactLimit=prefix+"x".repeat(ProtocolCodec.MAX_LINE_CHARS-prefix.length());
    String oversized="y".repeat(ProtocolCodec.MAX_LINE_CHARS+1);
    String input=exactLimit+"\n"+oversized+"\n"
        +ProtocolCodec.encode(new WorkerMessage("after-error","ping",""))+"\n"
        +ProtocolCodec.encode(new WorkerMessage("shutdown","shutdown",""))+"\n";
    var output=new StringWriter();
    var server=new ConfiguredWorkerServer(null,null,0,0,0,0,0,false);

    server.serve(new StringReader(input),output);

    String[] responses=output.toString().split("\\R");
    assertEquals(4,responses.length);
    assertEquals(new WorkerMessage("boundary","ok","pong"),ProtocolCodec.decode(responses[0]));
    var oversizedResponse=ProtocolCodec.decode(responses[1]);
    assertEquals("",oversizedResponse.id());
    assertEquals("error",oversizedResponse.operation());
    assertTrue(oversizedResponse.payload().contains("size limit"));
    assertEquals(new WorkerMessage("after-error","ok","pong"),ProtocolCodec.decode(responses[2]));
    assertEquals(new WorkerMessage("shutdown","ok","stopping"),ProtocolCodec.decode(responses[3]));
  }

  @Test
  void eofDrainsAcceptedRequestsAndTerminatesServer() throws Exception {
    String input = ProtocolCodec.encode(new WorkerMessage("eof-1", "ping", "")) + "\n"
        + ProtocolCodec.encode(new WorkerMessage("eof-2", "ping", "")) + "\n"
        + ProtocolCodec.encode(new WorkerMessage("eof-3", "ping", "")) + "\n";
    var output = new StringWriter();
    var server = new ConfiguredWorkerServer(null, null, 0, 0, 0, 0, 0, false);

    server.serve(new StringReader(input), output);

    var responses = java.util.Arrays.stream(output.toString().split("\\R"))
        .map(ProtocolCodec::decode).toList();
    assertEquals(List.of(
        new WorkerMessage("eof-1", "ok", "pong"),
        new WorkerMessage("eof-2", "ok", "pong"),
        new WorkerMessage("eof-3", "ok", "pong")), responses,
        "EOF must gracefully drain all accepted work without losing correlation IDs");
  }

  @Test
  void configuredRowLimitAcceptsExactBoundaryRejectsExcessAndKeepsWorkerUsable() throws Exception {
    var config = new RuntimeConfig("jdbc:duckdb:", 1, 0, null, 1);
    var budget = new io.zingg.duckdb.engine.ResourceBudget(0, 3, 0, 0, 0, 0);
    try (var runtime = new CompatibilityRuntime(config, null,
        "zingg-0.7.0-duckdb-1.5.5.1", budget)) {
      var server = new ConfiguredWorkerServer(runtime, new PathPolicy(null, null),
          0, 0, 3, 0, 0, true);
      String input = ProtocolCodec.encode(new WorkerMessage("at-limit", "count",
              "SELECT * FROM range(3)")) + "\n"
          + ProtocolCodec.encode(new WorkerMessage("over-limit", "count",
              "SELECT * FROM range(4)")) + "\n"
          + ProtocolCodec.encode(new WorkerMessage("still-alive", "ping", "")) + "\n"
          + ProtocolCodec.encode(new WorkerMessage("stop", "shutdown", "")) + "\n";
      var output = new StringWriter();

      server.serve(new StringReader(input), output);

      var responses = java.util.Arrays.stream(output.toString().split("\\R"))
          .map(ProtocolCodec::decode).toList();
      assertEquals(4, responses.size());
      assertEquals(new WorkerMessage("at-limit", "ok", "3"), responses.get(0));
      assertEquals("over-limit", responses.get(1).id());
      assertEquals("error", responses.get(1).operation());
      assertEquals("row budget exceeded: 4 > 3", responses.get(1).payload());
      assertEquals(new WorkerMessage("still-alive", "ok", "pong"), responses.get(2));
      assertEquals(new WorkerMessage("stop", "ok", "stopping"), responses.get(3));
    }
  }

  @Test
  void runV2RowBudgetFailureRollsBackArrowImportAndKeepsWorkerUsable(@TempDir Path temp)
      throws Exception {
    Path input = temp.resolve("four-row-input.arrow");
    Path outputPath = temp.resolve("must-not-publish.csv");
    writeLargeArrowInput(input, 4, 2);
    var config = new RuntimeConfig("jdbc:duckdb:", 1, 0, null, 2);
    var budget = new io.zingg.duckdb.engine.ResourceBudget(0, 3, 0, 0, 0, 0);

    try (var runtime = new CompatibilityRuntime(config, null,
        "zingg-0.7.0-duckdb-1.5.5.1", budget)) {
      LocalPhaseExecutors.register(runtime,
          new TrainingPlan.Config("\"value\"", "z_training_block", 10),
          new Matcher.MatchConfig("z_zid", "z_training_block", "1.0", 0.5));
      var server = new ConfiguredWorkerServer(runtime, new PathPolicy(null, null),
          0, 0, 3, 0, 0, false);
      String runPayload = PayloadCodec.encode(new String[] {
          ZinggJob.Phase.FIND_TRAINING_DATA.name(), outputPath.toString(), "TRUE", "",
          "", "", "", "", input.toString()
      });
      String requests = ProtocolCodec.encode(new WorkerMessage("over-limit-run", "run_v2", runPayload)) + "\n"
          + ProtocolCodec.encode(new WorkerMessage("after-row-rejection", "ping", "")) + "\n"
          + ProtocolCodec.encode(new WorkerMessage("stop", "shutdown", "")) + "\n";
      var output = new StringWriter();

      server.serve(new StringReader(requests), output);

      var responses = java.util.Arrays.stream(output.toString().split("\\R"))
          .map(ProtocolCodec::decode).toList();
      assertEquals(3, responses.size());
      assertEquals("over-limit-run", responses.get(0).id());
      assertEquals("error", responses.get(0).operation());
      assertEquals("row budget exceeded: 4 > 3", responses.get(0).payload());
      assertEquals(new WorkerMessage("after-row-rejection", "ok", "pong"), responses.get(1));
      assertEquals(new WorkerMessage("stop", "ok", "stopping"), responses.get(2));
      org.junit.jupiter.api.Assertions.assertFalse(Files.exists(outputPath),
          "over-budget input must not publish pipeline output");
      try (var entries = Files.list(temp)) {
        assertTrue(entries.noneMatch(path -> path.getFileName().toString()
            .startsWith(".must-not-publish.csv.partial-")),
            "over-budget input must not leave an output staging file");
      }
      try (var check = runtime.openJob()) {
        var rows = check.sql("SELECT count(*) AS n FROM information_schema.tables "
            + "WHERE table_name LIKE 'zd_arrow_%'").collect();
        assertEquals(0L, ((Number) rows.get(0).get("n")).longValue(),
            "rejected later Arrow batch must drop the partial import table");
      }
    }
  }

  @Test
  void runV2AggregateInputByteBudgetRejectsBeforeImportAndKeepsWorkerUsable(@TempDir Path temp)
      throws Exception {
    Path first = temp.resolve("first-input.csv");
    Path second = temp.resolve("second-input.csv");
    Path outputPath = temp.resolve("must-not-publish.csv");
    Files.writeString(first, "id,value\n1,a\n");
    Files.writeString(second, "id,value\n2,b\n");
    long aggregateBytes = Math.addExact(Files.size(first), Files.size(second));
    long limit = aggregateBytes - 1;
    var config = new RuntimeConfig("jdbc:duckdb:", 1, 0, null, 2);
    var budget = new io.zingg.duckdb.engine.ResourceBudget(0, 0, limit, 0, 0, 0);
    try (var runtime = new CompatibilityRuntime(config, null,
        "zingg-0.7.0-duckdb-1.5.5.1", budget)) {
      LocalPhaseExecutors.register(runtime,
          new TrainingPlan.Config("\"value\"", "z_training_block", 10),
          new Matcher.MatchConfig("z_zid", "z_training_block", "1.0", 0.5));
      var server = new ConfiguredWorkerServer(runtime, new PathPolicy(null, null),
          0, 0, 0, 0, 0, false);
      String payload = PayloadCodec.encode(new String[] {
          ZinggJob.Phase.FIND_TRAINING_DATA.name(), outputPath.toString(), "TRUE", "",
          "", "", "", "", first.toString(), second.toString()
      });
      String requests = ProtocolCodec.encode(new WorkerMessage("input-budget", "run_v2", payload)) + "\n"
          + ProtocolCodec.encode(new WorkerMessage("after-input-rejection", "ping", "")) + "\n"
          + ProtocolCodec.encode(new WorkerMessage("stop", "shutdown", "")) + "\n";
      var output = new StringWriter();
      server.serve(new StringReader(requests), output);
      var responses = java.util.Arrays.stream(output.toString().split("\\R"))
          .map(ProtocolCodec::decode).toList();
      assertEquals(3, responses.size());
      assertEquals("input-budget", responses.get(0).id());
      assertEquals("error", responses.get(0).operation());
      assertEquals("input byte budget exceeded: " + aggregateBytes + " > " + limit,
          responses.get(0).payload());
      assertEquals(new WorkerMessage("after-input-rejection", "ok", "pong"), responses.get(1));
      assertEquals(new WorkerMessage("stop", "ok", "stopping"), responses.get(2));
      org.junit.jupiter.api.Assertions.assertFalse(Files.exists(outputPath));
      try (var check = runtime.openJob()) {
        var rows = check.sql("SELECT count(*) AS n FROM information_schema.tables WHERE table_name LIKE 'zd_arrow_%'").collect();
        assertEquals(0L, ((Number) rows.get(0).get("n")).longValue(),
            "aggregate preflight must reject before creating Arrow staging tables");
      }
    }
  }

  @Test
  void runV2EnforcesConfiguredInputAndOutputRootsAcrossWorkerProtocol(@TempDir Path temp)
      throws Exception {
    Path inputRoot = Files.createDirectory(temp.resolve("allowed-input"));
    Path outputRoot = Files.createDirectory(temp.resolve("allowed-output"));
    Path allowedInput = inputRoot.resolve("records.csv");
    Files.writeString(allowedInput, "id,block\n1,same\n2,same\n");
    Path outsideInput = temp.resolve("outside-input.csv");
    Files.writeString(outsideInput, "id,block\n9,external\n");
    Path outputTraversal = outputRoot.resolve("..").resolve("escaped.csv");
    Path outsideOutputTarget = temp.resolve("outside-output-target.csv");
    Files.writeString(outsideOutputTarget, "previous-external-output\n");
    Path symlinkInput = inputRoot.resolve("linked.csv");
    Path symlinkOutput = outputRoot.resolve("linked-output.csv");
    try {
      Files.createSymbolicLink(symlinkInput, outsideInput);
      Files.createSymbolicLink(symlinkOutput, outsideOutputTarget);
    } catch (UnsupportedOperationException | java.io.IOException | SecurityException unavailable) {
      org.junit.jupiter.api.Assumptions.abort("worker path symlink test unavailable: " + unavailable);
    }

    var runtimeConfig = new RuntimeConfig("jdbc:duckdb:", 1, 0, null, 1);
    try (var runtime = new CompatibilityRuntime(runtimeConfig, null,
        "zingg-0.7.0-duckdb-1.5.5.1")) {
      LocalPhaseExecutors.register(runtime,
          new TrainingPlan.Config("\"block\"", "z_training_block", 10),
          new Matcher.MatchConfig("id", "z_training_block", "1.0", 0.5));
      var server = new ConfiguredWorkerServer(runtime, new PathPolicy(inputRoot, outputRoot),
          0, 0, 0, 0, 0, false);
      String requests = ProtocolCodec.encode(runV2Request("outside-input", outputRoot.resolve("input-error.csv"),
              outsideInput)) + "\n"
          + ProtocolCodec.encode(runV2Request("output-traversal", outputTraversal, allowedInput)) + "\n"
          + ProtocolCodec.encode(runV2Request("input-symlink", outputRoot.resolve("input-link-error.csv"),
              symlinkInput)) + "\n"
          + ProtocolCodec.encode(runV2Request("output-symlink", symlinkOutput, allowedInput)) + "\n"
          + ProtocolCodec.encode(runV2Request("allowed-paths", outputRoot.resolve("valid-output.csv"),
              allowedInput)) + "\n"
          + ProtocolCodec.encode(new WorkerMessage("path-policy-ping", "ping", "")) + "\n"
          + ProtocolCodec.encode(new WorkerMessage("path-policy-stop", "shutdown", "")) + "\n";
      var output = new StringWriter();
      server.serve(new StringReader(requests), output);

      var responses = java.util.Arrays.stream(output.toString().split("\\R"))
          .map(ProtocolCodec::decode).toList();
      assertEquals(7, responses.size());
      assertPathPolicyError(responses.get(0), "outside-input", "input path escapes configured root");
      assertPathPolicyError(responses.get(1), "output-traversal", "output path escapes configured root");
      assertPathPolicyError(responses.get(2), "input-symlink", "input symbolic links are not allowed");
      assertPathPolicyError(responses.get(3), "output-symlink", "output symbolic links are not allowed");
      assertEquals("allowed-paths", responses.get(4).id());
      assertEquals("ok", responses.get(4).operation(), responses.get(4).payload());
      assertTrue(Files.isRegularFile(outputRoot.resolve("valid-output.csv")));
      assertEquals("previous-external-output\n", Files.readString(outsideOutputTarget),
          "rejected symlink output must not modify its external target");
      assertEquals(new WorkerMessage("path-policy-ping", "ok", "pong"), responses.get(5));
      assertEquals(new WorkerMessage("path-policy-stop", "ok", "stopping"), responses.get(6));
      assertTrue(Files.notExists(temp.resolve("escaped.csv")), "traversal output must not be published");
    }
  }

  private static WorkerMessage runV2Request(String id, Path output, Path input) {
    String payload = PayloadCodec.encode(new String[] {
        ZinggJob.Phase.FIND_TRAINING_DATA.name(), output.toString(), "TRUE", "",
        "", "", "", "", input.toString()
    });
    return new WorkerMessage(id, "run_v2", payload);
  }

  private static void assertPathPolicyError(WorkerMessage response, String id, String detail) {
    assertEquals(id, response.id());
    assertEquals("error", response.operation());
    assertTrue(response.payload().contains(detail), response.payload());
  }

  @Test
  void runV2OutputBudgetFailurePreservesDestinationAndKeepsWorkerUsable(@TempDir Path temp)
      throws Exception {
    Path input = temp.resolve("output-limit-input.csv");
    Path outputPath = temp.resolve("limited-output.csv");
    Files.writeString(input, "id,z_training_block\n1,same\n2,same\n");
    Files.writeString(outputPath, "previous complete output\n");
    var config = new RuntimeConfig("jdbc:duckdb:", 1, 0, null, 1);
    var budget = new io.zingg.duckdb.engine.ResourceBudget(0, 0, 0, 1, 0, 0);

    try (var runtime = new CompatibilityRuntime(config, null,
        "zingg-0.7.0-duckdb-1.5.5.1", budget)) {
      LocalPhaseExecutors.register(runtime,
          new TrainingPlan.Config("\"z_training_block\"", "z_training_block", 10),
          new Matcher.MatchConfig("id", "z_training_block", "1.0", 0.5));
      var server = new ConfiguredWorkerServer(runtime, new PathPolicy(null, null),
          0, 1, 0, 0, 0, false);
      String payload = PayloadCodec.encode(new String[] {
          ZinggJob.Phase.FIND_TRAINING_DATA.name(), outputPath.toString(), "TRUE", "",
          "", "", "", "", input.toString()
      });
      String requests = ProtocolCodec.encode(new WorkerMessage("output-limited-run", "run_v2", payload)) + "\n"
          + ProtocolCodec.encode(new WorkerMessage("after-output-rejection", "ping", "")) + "\n"
          + ProtocolCodec.encode(new WorkerMessage("stop", "shutdown", "")) + "\n";
      var output = new StringWriter();

      server.serve(new StringReader(requests), output);

      var responses = java.util.Arrays.stream(output.toString().split("\\R"))
          .map(ProtocolCodec::decode).toList();
      assertEquals(3, responses.size());
      assertEquals("output-limited-run", responses.get(0).id());
      assertEquals("error", responses.get(0).operation());
      assertTrue(responses.get(0).payload().contains("output byte budget exceeded"),
          responses.get(0).payload());
      assertEquals("previous complete output\n", Files.readString(outputPath));
      assertEquals(new WorkerMessage("after-output-rejection", "ok", "pong"), responses.get(1));
      assertEquals(new WorkerMessage("stop", "ok", "stopping"), responses.get(2));
      try (var entries = Files.list(temp)) {
        assertTrue(entries.noneMatch(path -> path.getFileName().toString()
            .startsWith(".limited-output.csv.partial-")),
            "output rejection must remove worker pipeline staging files");
      }
    }
  }

  @Test
  void trainOutputBudgetRejectsNativeModelBeforePublicationAndKeepsWorkerUsable(@TempDir Path temp)
      throws Exception {
    Path input = temp.resolve("train-budget-input.csv");
    Path artifact = temp.resolve("budget-model");
    Files.writeString(input, "id,z_training_block\n1,same\n2,same\n");
    var config = new RuntimeConfig("jdbc:duckdb:", 1, 0, null, 1);
    try (var runtime = new CompatibilityRuntime(config, null,
        "zingg-0.7.0-duckdb-1.5.5.1")) {
      var server = new ConfiguredWorkerServer(runtime, new PathPolicy(null, null),
          0, 1, 0, 0, 0, false);
      String payload = PayloadCodec.encode(new String[] {
          "zingg-0.7.0", artifact.toString(), "\"z_training_block\"",
          "z_training_block", "10", input.toString()
      });
      String requests = ProtocolCodec.encode(new WorkerMessage("model-over-limit", "train_blocking", payload)) + "\n"
          + ProtocolCodec.encode(new WorkerMessage("after-model-rejection", "ping", "")) + "\n"
          + ProtocolCodec.encode(new WorkerMessage("stop", "shutdown", "")) + "\n";
      var output = new StringWriter();

      server.serve(new StringReader(requests), output);

      var responses = java.util.Arrays.stream(output.toString().split("\\R"))
          .map(ProtocolCodec::decode).toList();
      assertEquals(3, responses.size());
      assertEquals("model-over-limit", responses.get(0).id());
      assertEquals("error", responses.get(0).operation());
      assertTrue(responses.get(0).payload().startsWith("model artifact byte budget exceeded:"),
          responses.get(0).payload());
      assertEquals(new WorkerMessage("after-model-rejection", "ok", "pong"), responses.get(1));
      assertEquals(new WorkerMessage("stop", "ok", "stopping"), responses.get(2));
      org.junit.jupiter.api.Assertions.assertFalse(Files.exists(artifact));
      try (var entries = Files.list(temp)) {
        assertTrue(entries.noneMatch(path -> path.getFileName().toString().startsWith(".model-artifact-")),
            "rejected model must not leave an artifact stage");
      }
    }
  }

  @Test
  void acceptsCancelControlWhileLongRunningRequestIsExecuting() throws Exception {
    var input = new PipedReader(64 * 1024);
    var inputWriter = new PipedWriter(input);
    var output = new PipedReader(64 * 1024);
    var outputWriter = new PipedWriter(output);
    try (var runtime = new CompatibilityRuntime("jdbc:duckdb:", null)) {
      var server = new ConfiguredWorkerServer(runtime, new PathPolicy(null, null), 0, 0, 0,
          0, 0, true);
      Thread serving = new Thread(() -> {
        try { server.serve(input, outputWriter); }
        catch (Exception failure) { throw new AssertionError(failure); }
      }, "worker-server-test");
      serving.start();
      var responses = new BufferedReader(output);
      inputWriter.write(ProtocolCodec.encode(new WorkerMessage("long-query", "count",
          "SELECT sum(i) FROM range(1000000000000) t(i)")) + "\n");
      inputWriter.flush();
      Thread.sleep(100);
      inputWriter.write(ProtocolCodec.encode(new WorkerMessage("cancel-1", "cancel", "long-query")) + "\n");
      inputWriter.flush();
      var first = ProtocolCodec.decode(responses.readLine());
      var second = ProtocolCodec.decode(responses.readLine());
      var byId = new java.util.HashMap<String, WorkerMessage>();
      byId.put(first.id(), first);
      byId.put(second.id(), second);
      assertEquals("cancel_requested", byId.get("cancel-1").payload());
      assertEquals("error", byId.get("long-query").operation());

      inputWriter.write(ProtocolCodec.encode(new WorkerMessage("after-cancel", "ping", "")) + "\n");
      inputWriter.flush();
      assertEquals(new WorkerMessage("after-cancel", "ok", "pong"),
          ProtocolCodec.decode(responses.readLine()));
      inputWriter.write(ProtocolCodec.encode(new WorkerMessage("stop", "shutdown", "")) + "\n");
      inputWriter.flush();
      assertEquals(new WorkerMessage("stop", "ok", "stopping"),
          ProtocolCodec.decode(responses.readLine()));
      serving.join(5_000);
      assertTrue(!serving.isAlive(), "server should finish after shutdown");
    }
  }

  @Test
  void boundedQueueRejectsOverflowButStillAcceptsCancellation() throws Exception {
    var input = new PipedReader(64 * 1024);
    var inputWriter = new PipedWriter(input);
    var output = new PipedReader(64 * 1024);
    var outputWriter = new PipedWriter(output);
    try (var runtime = new CompatibilityRuntime("jdbc:duckdb:", null)) {
      var server = new ConfiguredWorkerServer(runtime, new PathPolicy(null, null), 0, 0, 0,
          0, 0, true);
      Thread serving = new Thread(() -> {
        try { server.serve(input, outputWriter); }
        catch (Exception failure) { throw new AssertionError(failure); }
      }, "worker-queue-bound-test");
      serving.start();
      var responses = new BufferedReader(output);
      inputWriter.write(ProtocolCodec.encode(new WorkerMessage("long-query", "count",
          "SELECT sum(i) FROM range(1000000000000) t(i)")) + "\n");
      inputWriter.flush();
      Thread.sleep(100);
      for (int i = 0; i <= 64; i++)
        inputWriter.write(ProtocolCodec.encode(new WorkerMessage("queued-" + i, "ping", "")) + "\n");
      inputWriter.write(ProtocolCodec.encode(new WorkerMessage("cancel-full-queue", "cancel", "long-query")) + "\n");
      inputWriter.flush();

      var byId = new java.util.HashMap<String, WorkerMessage>();
      for (int i = 0; i < 67; i++) {
        var response = ProtocolCodec.decode(responses.readLine());
        byId.put(response.id(), response);
      }
      assertEquals("cancel_requested", byId.get("cancel-full-queue").payload());
      assertEquals("error", byId.get("long-query").operation());
      assertEquals("error", byId.get("queued-64").operation());
      assertEquals("worker request queue is full", byId.get("queued-64").payload());
      for (int i = 0; i < 64; i++)
        assertEquals(new WorkerMessage("queued-" + i, "ok", "pong"), byId.get("queued-" + i));

      inputWriter.write(ProtocolCodec.encode(new WorkerMessage("after-queue", "ping", "")) + "\n");
      inputWriter.flush();
      assertEquals(new WorkerMessage("after-queue", "ok", "pong"),
          ProtocolCodec.decode(responses.readLine()));
      inputWriter.write(ProtocolCodec.encode(new WorkerMessage("stop", "shutdown", "")) + "\n");
      inputWriter.flush();
      assertEquals(new WorkerMessage("stop", "ok", "stopping"),
          ProtocolCodec.decode(responses.readLine()));
      serving.join(5_000);
      assertTrue(!serving.isAlive(), "server should finish after shutdown");
    }
  }

  @Test
  void completionCancellationRaceKeepsEveryResponseCorrelatedAndWorkerUsable() throws Exception {
    final int races = 32;
    var input = new PipedReader(64 * 1024);
    var inputWriter = new PipedWriter(input);
    var output = new StringWriter();
    try (var runtime = new CompatibilityRuntime("jdbc:duckdb:", null)) {
      var server = new ConfiguredWorkerServer(runtime, new PathPolicy(null, null), 0, 0, 0,
          0, 0, false);
      Thread serving = new Thread(() -> {
        try { server.serve(input, output); }
        catch (Exception failure) { throw new AssertionError(failure); }
      }, "worker-cancel-completion-race-test");
      serving.start();
      for (int i = 0; i < races; i++) {
        inputWriter.write(ProtocolCodec.encode(new WorkerMessage("race-" + i, "ping", "")) + "\n");
        inputWriter.write(ProtocolCodec.encode(new WorkerMessage("cancel-" + i, "cancel", "race-" + i)) + "\n");
      }
      inputWriter.write(ProtocolCodec.encode(new WorkerMessage("race-stop", "shutdown", "")) + "\n");
      inputWriter.flush();
      serving.join(5_000);
      assertTrue(!serving.isAlive(), "server should finish after shutdown");

      var byId = new java.util.HashMap<String, WorkerMessage>();
      String[] responseLines = output.toString().split("\\R");
      assertEquals(races * 2 + 1, responseLines.length);
      for (String responseLine : responseLines) {
        var response = ProtocolCodec.decode(responseLine);
        assertTrue(byId.put(response.id(), response) == null,
            "response IDs must not be duplicated: " + response.id());
      }
      for (int i = 0; i < races; i++) {
        var request = byId.get("race-" + i);
        assertTrue(request != null, "missing request response race-" + i);
        var cancel = byId.get("cancel-" + i);
        assertTrue(cancel != null, "missing cancel acknowledgement cancel-" + i);
        assertEquals("ok", cancel.operation());
        assertTrue(cancel.payload().equals("cancel_requested") || cancel.payload().equals("not_active"),
            "unexpected cancel race outcome: " + cancel.payload());
        if (!request.equals(new WorkerMessage("race-" + i, "ok", "pong"))) {
          assertEquals("error", request.operation());
          assertEquals("operation cancelled", request.payload());
          assertEquals("cancel_requested", cancel.payload());
        }
      }
      assertEquals(new WorkerMessage("race-stop", "ok", "stopping"), byId.get("race-stop"));
    }
  }

  @Test
  void cancellingRunV2DuringArrowImportRemovesPartialStateAndDoesNotPublishOutput(
      @TempDir Path temp) throws Exception {
    Path input = temp.resolve("large-arrow-input.arrow");
    Path outputPath = temp.resolve("cancelled-output.csv");
    writeLargeArrowInput(input, 1_048_576, 65_536);

    try (var runtime = new CompatibilityRuntime(new RuntimeConfig("jdbc:duckdb:", 1, 0, null, 2), null)) {
    LocalPhaseExecutors.register(runtime,
        new TrainingPlan.Config("\"value\"", "z_training_block", 1_048_576),
        new Matcher.MatchConfig("z_zid", "z_training_block", "1.0", 0.5));
    var inputReader = new PipedReader(64 * 1024);
    var inputWriter = new PipedWriter(inputReader);
    var outputReader = new PipedReader(64 * 1024);
    var outputWriter = new PipedWriter(outputReader);
    var server = new ConfiguredWorkerServer(runtime, new PathPolicy(null, null), 0, 0, 0,
        0, 0, false);
    Thread serving = new Thread(() -> {
      try { server.serve(inputReader, outputWriter); }
      catch (Exception failure) { throw new AssertionError(failure); }
    }, "worker-run-v2-arrow-cancel-test");
    serving.setDaemon(true);
    serving.start();
    var responses = new BufferedReader(outputReader);
    String payload = PayloadCodec.encode(new String[] {
        ZinggJob.Phase.FIND_TRAINING_DATA.name(), outputPath.toString(), "TRUE", "",
        "", "", "", "", input.toString()
    });
    inputWriter.write(ProtocolCodec.encode(new WorkerMessage("arrow-run", "run_v2", payload)) + "\n");
    inputWriter.flush();
    Thread.sleep(100);
    inputWriter.write(ProtocolCodec.encode(
        new WorkerMessage("arrow-cancel", "cancel", "arrow-run")) + "\n");
    inputWriter.flush();

    var first = ProtocolCodec.decode(responses.readLine());
    var second = ProtocolCodec.decode(responses.readLine());
    var byId = new java.util.HashMap<String, WorkerMessage>();
    byId.put(first.id(), first);
    byId.put(second.id(), second);
    assertEquals("cancel_requested", byId.get("arrow-cancel").payload());
    assertEquals("error", byId.get("arrow-run").operation());
    assertTrue(byId.get("arrow-run").payload().equals("operation cancelled")
            || byId.get("arrow-run").payload().startsWith("Arrow IPC ingestion failed:"),
        "run_v2 must terminate with a cancellation/Arrow-import error, got: "
            + byId.get("arrow-run").payload());
    org.junit.jupiter.api.Assertions.assertFalse(Files.exists(outputPath),
        "cancelled run_v2 must not publish its output file");
    try (var entries = Files.list(temp)) {
      assertTrue(entries.noneMatch(entry -> entry.getFileName().toString()
              .startsWith(".cancelled-output.csv.partial-")),
          "cancelled run_v2 must not leave an atomic-output staging file");
    }
    try (var check = runtime.openJob()) {
      var rows = check.sql("SELECT count(*) AS n FROM information_schema.tables "
          + "WHERE table_name LIKE 'zd_arrow_%'").collect();
      assertEquals(0L, ((Number) rows.get(0).get("n")).longValue(),
          "cancelled Arrow import must not leave partial worker tables");
    }

    inputWriter.write(ProtocolCodec.encode(new WorkerMessage("after-arrow-cancel", "ping", "")) + "\n");
    inputWriter.flush();
    assertEquals(new WorkerMessage("after-arrow-cancel", "ok", "pong"),
        ProtocolCodec.decode(responses.readLine()));
    inputWriter.write(ProtocolCodec.encode(new WorkerMessage("stop", "shutdown", "")) + "\n");
    inputWriter.flush();
    assertEquals(new WorkerMessage("stop", "ok", "stopping"),
        ProtocolCodec.decode(responses.readLine()));
    serving.join(5_000);
    assertTrue(!serving.isAlive(), "server should finish after shutdown");
    inputWriter.close();
    serving.join(5_000);
    assertTrue(!serving.isAlive(), "server should exit after input closure");
    }
  }

  @Test
  void cancellingRunV2DuringArrowExportPreservesDestinationAndKeepsWorkerUsable(
      @TempDir Path temp) throws Exception {
    Path input = temp.resolve("large-arrow-export-input.csv");
    Path outputPath = temp.resolve("cancelled-arrow-export.arrow");
    try (var writer = Files.newBufferedWriter(input)) {
      writer.write("id,value\n");
      String wideValue = "x".repeat(4096);
      for (int row = 0; row < 20_000; row++) {
        writer.write(Integer.toString(row));
        writer.write(',');
        writer.write(wideValue);
        writer.write('\n');
      }
    }
    Files.writeString(outputPath, "previous complete Arrow output");

    try (var runtime = new CompatibilityRuntime(new RuntimeConfig("jdbc:duckdb:", 2, 0, null, 2), null)) {
      runtime.registerPhaseExecutor(ZinggJob.Phase.FIND_TRAINING_DATA, (job, frame) -> frame);
      var inputReader = new PipedReader(64 * 1024);
      var inputWriter = new PipedWriter(inputReader);
      var outputReader = new PipedReader(64 * 1024);
      var outputWriter = new PipedWriter(outputReader);
      var server = new ConfiguredWorkerServer(runtime, new PathPolicy(null, null), 0, 0, 0,
          0, 0, false);
      Thread serving = new Thread(() -> {
        try { server.serve(inputReader, outputWriter); }
        catch (Exception failure) { throw new AssertionError(failure); }
      }, "worker-run-v2-arrow-export-cancel-test");
      serving.setDaemon(true);
      serving.start();
      var responses = new BufferedReader(outputReader);
      String payload = PayloadCodec.encode(new String[] {
          ZinggJob.Phase.FIND_TRAINING_DATA.name(), outputPath.toString(), "TRUE", "",
          "", "", "", "", input.toString()
      });
      inputWriter.write(ProtocolCodec.encode(new WorkerMessage("arrow-export-run", "run_v2", payload)) + "\n");
      inputWriter.flush();

      long deadline = System.nanoTime() + java.time.Duration.ofSeconds(30).toNanos();
      Path growingStage = null;
      while (System.nanoTime() < deadline && growingStage == null) {
        try (var entries = Files.list(temp)) {
          growingStage = entries.filter(path -> path.getFileName().toString().contains("partial"))
              .filter(Files::isRegularFile)
              .filter(path -> {
                try { return Files.size(path) >= 64 * 1024; }
                catch (java.io.IOException ignored) { return false; }
              }).findFirst().orElse(null);
        }
        if (growingStage == null) Thread.sleep(5);
      }
      assertTrue(growingStage != null,
          "Arrow export should create a growing stage before the cancellation request");
      inputWriter.write(ProtocolCodec.encode(
          new WorkerMessage("arrow-export-cancel", "cancel", "arrow-export-run")) + "\n");
      inputWriter.flush();

      var first = ProtocolCodec.decode(responses.readLine());
      var second = ProtocolCodec.decode(responses.readLine());
      var byId = new java.util.HashMap<String, WorkerMessage>();
      byId.put(first.id(), first);
      byId.put(second.id(), second);
      assertEquals("cancel_requested", byId.get("arrow-export-cancel").payload());
      assertEquals("error", byId.get("arrow-export-run").operation());
      assertEquals("operation cancelled", byId.get("arrow-export-run").payload());
      assertEquals("previous complete Arrow output", Files.readString(outputPath));
      try (var entries = Files.list(temp)) {
        assertTrue(entries.noneMatch(path -> path.getFileName().toString().contains("partial")),
            "cancelled Arrow export must clean both pipeline and format staging files");
      }

      inputWriter.write(ProtocolCodec.encode(new WorkerMessage("after-arrow-export-cancel", "ping", "")) + "\n");
      inputWriter.flush();
      assertEquals(new WorkerMessage("after-arrow-export-cancel", "ok", "pong"),
          ProtocolCodec.decode(responses.readLine()));
      inputWriter.write(ProtocolCodec.encode(new WorkerMessage("arrow-export-stop", "shutdown", "")) + "\n");
      inputWriter.flush();
      assertEquals(new WorkerMessage("arrow-export-stop", "ok", "stopping"),
          ProtocolCodec.decode(responses.readLine()));
      serving.join(5_000);
      assertTrue(!serving.isAlive(), "server should finish after cancellation and shutdown");
      inputWriter.close();
    }
  }

  private static void writeLargeArrowInput(Path path, int rows, int batchSize) throws Exception {
    try (var allocator = new org.apache.arrow.memory.RootAllocator();
         var root = org.apache.arrow.vector.VectorSchemaRoot.create(
             new org.apache.arrow.vector.types.pojo.Schema(List.of(
                 new org.apache.arrow.vector.types.pojo.Field("value",
                     org.apache.arrow.vector.types.pojo.FieldType.nullable(
                         new org.apache.arrow.vector.types.pojo.ArrowType.Int(32, true)), null))), allocator);
         var channel = Channels.newChannel(Files.newOutputStream(path));
         var writer = new org.apache.arrow.vector.ipc.ArrowStreamWriter(root, null, channel)) {
      var vector = (org.apache.arrow.vector.IntVector) root.getVector("value");
      writer.start();
      for (int base = 0; base < rows; base += batchSize) {
        int count = Math.min(batchSize, rows - base);
        for (int row = 0; row < count; row++) vector.setSafe(row, base + row);
        root.setRowCount(count);
        writer.writeBatch();
      }
      writer.end();
    }
  }
}
