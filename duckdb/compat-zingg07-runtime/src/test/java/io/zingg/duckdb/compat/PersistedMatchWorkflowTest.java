package io.zingg.duckdb.compat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.zingg.duckdb.api.DuckException;
import io.zingg.duckdb.api.RuntimeConfig;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class PersistedMatchWorkflowTest {
  @Test
  void persistedClassifierThresholdOverridesConflictingMatchConfig(@TempDir Path temp) throws Exception {
    Path records = temp.resolve("threshold-records.csv");
    Files.writeString(records, "id,block,feature\n1,same,0.2\n2,same,0.8\n");
    Path artifact = temp.resolve("classifier-threshold-073");
    double probability = 0.60d;
    double intercept = Math.log(probability / (1.0d - probability));
    new NativeClassifierArtifact().write(new NativeClassifierArtifact.Config(
        "zingg-0.7.0", List.of("feature"), new double[] {0d}, intercept, true,
        artifact, 0.73d, List.of(), 0));

    var config = new RuntimeConfig("jdbc:duckdb:" + temp.resolve("threshold-precedence.duckdb"),
        1, 0, null, 1);
    try (var runtime = new CompatibilityRuntime(config, null);
         var workflow = new PersistedMatchWorkflow(runtime)) {
      workflow.restartFromModel(artifact);
      var input = workflow.read(List.of(records), ZinggJob.Phase.MATCH, null);
      var result = workflow.match(input, new Matcher.MatchConfig("id", "block", "TRUE", 0.10d));

      assertEquals(0, result.count(),
          "the persisted 0.73 threshold must reject probability 0.60 despite MatchConfig threshold 0.10");
      assertEquals(PersistedMatchWorkflow.State.MATCHED, workflow.state());
    }

    var persisted = (LinearClassifier) new ModelScorerRegistry().create(
        io.zingg.duckdb.model.ModelReader.load(artifact, 64L * 1024 * 1024));
    assertEquals(0.73d, persisted.threshold());
    assertEquals(probability, persisted.score(java.util.Map.of("feature", 0.2d)), 1e-12);
  }

  @Test
  void backendHistogramCannotMasqueradeAsPersistedClassifier(@TempDir Path temp) throws Exception {
    Path input = temp.resolve("records.csv");
    Files.writeString(input, "name,city\nAlice,London\nAlicia,London\nBob,Paris\n");
    var config = new RuntimeConfig("jdbc:duckdb:" + temp.resolve("workflow.duckdb"), 2, 0, null, 2);
    try (var runtime = new CompatibilityRuntime(config, null, "zingg-0.7.0-duckdb-1.5.5.1", null);
         var job = new ZinggJob(runtime);
         var workflow = new PersistedMatchWorkflow(runtime)) {
      var inputFrame = job.read(List.of(input), ZinggJob.Phase.FIND_TRAINING_DATA);
      var training = workflow.findTrainingData(inputFrame,
          new TrainingPlan.Config("lower(city)", "z_block", 100));
      assertEquals(3, training.count());
      var labels = workflow.applyLabels(new LabelDecisionProvider.ApplyLabelsRequest(
          "labels-1", "v1", "labels-1", List.of()));
      assertEquals(0, labels.applied());
      var artifact = workflow.trainMatch(inputFrame,
          NativeTrainingConfig.defaults(temp.resolve("model"), "lower(city)", "z_block"));
      assertEquals(3, artifact.sampledRows());
      assertEquals(PersistedMatchWorkflow.State.MODEL_PERSISTED, workflow.state());
      DuckException failure = assertThrows(DuckException.class, workflow::restart);
      assertTrue(failure.getMessage().contains("not a classifier"));
      assertEquals(PersistedMatchWorkflow.State.MODEL_PERSISTED, workflow.state());
    }
  }

  @Test
  void classifierIsReloadedInNewRuntimeAndChangesMatchPredictions(@TempDir Path temp) throws Exception {
    Path records = temp.resolve("records.csv");
    Files.writeString(records, "id,block,feature\n1,a,1\n2,a,-1\n3,a,1\n");
    Path artifact = temp.resolve("classifier");
    Path oppositeArtifact = temp.resolve("classifier-opposite");
    var config = new RuntimeConfig("jdbc:duckdb:" + temp.resolve("classifier.duckdb"), 3, 0, null, 3);
    var matchConfig = new Matcher.MatchConfig("id", "block", "1.0", 0.5);
    try (var runtime = new CompatibilityRuntime(config, null);
         var workflow = new PersistedMatchWorkflow(runtime)) {
      var input = workflow.read(List.of(records), ZinggJob.Phase.FIND_TRAINING_DATA, null);
      workflow.findTrainingData(input, new TrainingPlan.Config("block", "z_block", 100));
      var negative = new LabelDecisionProvider.LabelDecision("1", "2",
          LabelDecisionProvider.Decision.NON_MATCH, "test");
      var positive = new LabelDecisionProvider.LabelDecision("1", "3",
          LabelDecisionProvider.Decision.MATCH, "test");
      var applied = workflow.applyLabels(new LabelDecisionProvider.ApplyLabelsRequest(
          "labels-2", "v1", "labels-2", List.of(negative, positive)));
      assertEquals(2, applied.applied());
      var replay = workflow.applyLabels(new LabelDecisionProvider.ApplyLabelsRequest(
          "labels-2-retry", "v1", "labels-2", List.of(negative, positive)));
      assertTrue(replay.replay());
      assertEquals(2, workflow.appliedLabels());
      assertThrows(DuckException.class, () -> workflow.applyLabels(new LabelDecisionProvider.ApplyLabelsRequest(
          "labels-2-changed", "v1", "labels-2", List.of(positive, negative))));
      var pairs = new Matcher().candidates(input, matchConfig);
      workflow.trainClassifier(pairs, "id", "z_id", NativeClassifierTrainer.Config.defaults(
          "zingg-0.7.0", List.of("z_feature"), "z_label", artifact));
      assertEquals(PersistedMatchWorkflow.State.MODEL_PERSISTED, workflow.state());
      workflow.restart();
      assertEquals(PersistedMatchWorkflow.State.RESTARTED, workflow.state());
      var restartedInput = workflow.read(List.of(records), ZinggJob.Phase.MATCH, null);
      var restartedOutput = workflow.match(restartedInput, matchConfig);
      assertEquals(List.of("1:3", "2:3"), restartedOutput.collect().stream()
          .map(row -> row.get("id") + ":" + row.get("z_id"))
          .sorted().toList());
    }
    try (var runtime = new CompatibilityRuntime(config, null);
         var workflow = new PersistedMatchWorkflow(runtime)) {
      var input = workflow.read(List.of(records), ZinggJob.Phase.FIND_TRAINING_DATA, null);
      workflow.findTrainingData(input, new TrainingPlan.Config("block", "z_block", 100));
      workflow.applyLabels(new LabelDecisionProvider.ApplyLabelsRequest("labels-3", "v1", "labels-3",
          List.of(new LabelDecisionProvider.LabelDecision("1", "2", LabelDecisionProvider.Decision.MATCH, "test"),
              new LabelDecisionProvider.LabelDecision("1", "3", LabelDecisionProvider.Decision.NON_MATCH, "test"))));
      workflow.trainClassifier(new Matcher().candidates(input, matchConfig), "id", "z_id",
          NativeClassifierTrainer.Config.defaults(
              "zingg-0.7.0", List.of("z_feature"), "z_label", oppositeArtifact));
    }
    try (var runtime = new CompatibilityRuntime(config, null);
         var workflow = new PersistedMatchWorkflow(runtime)) {
      workflow.restartFromModel(artifact);
      var input = workflow.read(List.of(records), ZinggJob.Phase.MATCH, null);
      var scored = workflow.match(input, matchConfig);
      assertEquals(2, scored.count());
      assertTrue(scored.columns().contains("z_prediction"));
      assertEquals(List.of("1:3", "2:3"), scored.collect().stream()
          .map(row -> row.get("id") + ":" + row.get("z_id"))
          .sorted().toList());
      assertTrue(scored.collect().stream().allMatch(row -> ((Number) row.get("z_prediction")).intValue() == 1));
      assertEquals(PersistedMatchWorkflow.State.MATCHED, workflow.state());
      try (var opposite = new PersistedMatchWorkflow(runtime)) {
        opposite.restartFromModel(oppositeArtifact);
        var oppositeInput = opposite.read(List.of(records), ZinggJob.Phase.MATCH, null);
        var oppositeScored = opposite.match(oppositeInput, matchConfig);
        assertEquals(1, oppositeScored.count());
        assertEquals(List.of("1:2"), oppositeScored.collect().stream()
            .map(row -> row.get("id") + ":" + row.get("z_id"))
            .sorted().toList());
        assertTrue(oppositeScored.collect().stream()
            .allMatch(row -> ((Number) row.get("z_prediction")).intValue() == 1));
      }
    }
  }

  @Test
  void durableLabelsTrainAndScoreClassifierAcrossFreshRuntimeInstances(@TempDir Path temp) throws Exception {
    Path records = temp.resolve("durable-records.csv");
    Files.writeString(records, "id,block,feature\n1,a,1\n2,a,-1\n3,a,1\n");
    Path database = temp.resolve("durable-workflow.duckdb");
    Path labelStore = temp.resolve("label-store.bin");
    Path classifier = temp.resolve("durable-classifier");
    var runtimeConfig = new RuntimeConfig("jdbc:duckdb:" + database, 2, 0, null, 2);
    var matchConfig = new Matcher.MatchConfig("id", "block", "1.0", 0.5);
    var positive = new LabelDecisionProvider.LabelDecision(
        "1", "3", LabelDecisionProvider.Decision.MATCH, "reviewer");
    var negative = new LabelDecisionProvider.LabelDecision(
        "1", "2", LabelDecisionProvider.Decision.NON_MATCH, "reviewer");

    // Process/runtime 1: persist human decisions and terminate before model training.
    try (var runtime = new CompatibilityRuntime(runtimeConfig, null, "zingg-0.7.0-duckdb-1.5.5.1", null,
             new PersistentLabelDecisionProvider(labelStore));
         var workflow = new PersistedMatchWorkflow(runtime)) {
      var input = workflow.read(List.of(records), ZinggJob.Phase.FIND_TRAINING_DATA, null);
      workflow.findTrainingData(input, new TrainingPlan.Config("block", "z_block", 100));
      var applied = workflow.applyLabels(new LabelDecisionProvider.ApplyLabelsRequest(
          "durable-label-batch", "v1", "durable-labels", List.of(positive, negative)));
      assertEquals(2, applied.applied());
      assertEquals(PersistedMatchWorkflow.State.LABELS_APPLIED, workflow.state());
    }

    // Process/runtime 2: recover labels from disk, train only from those decisions, persist model.
    try (var runtime = new CompatibilityRuntime(runtimeConfig, null, "zingg-0.7.0-duckdb-1.5.5.1", null,
             new PersistentLabelDecisionProvider(labelStore));
         var workflow = new PersistedMatchWorkflow(runtime)) {
      var input = workflow.read(List.of(records), ZinggJob.Phase.FIND_TRAINING_DATA, null);
      workflow.findTrainingData(input, new TrainingPlan.Config("block", "z_block", 100));
      workflow.restoreAppliedLabels();
      assertEquals(2, workflow.appliedLabels());
      var pairs = new Matcher().candidates(input, matchConfig);
      var trained = workflow.trainClassifier(pairs, "id", "z_id", NativeClassifierTrainer.Config.defaults(
          "zingg-0.7.0", List.of("z_feature"), "z_label", classifier));
      assertEquals(2, trained.rows());
      assertEquals(PersistedMatchWorkflow.State.MODEL_PERSISTED, workflow.state());
    }

    // Process/runtime 3: load the serialized classifier in a clean workflow and score MATCH inputs.
    try (var runtime = new CompatibilityRuntime(runtimeConfig, null);
         var workflow = new PersistedMatchWorkflow(runtime)) {
      workflow.restartFromModel(classifier);
      var input = workflow.read(List.of(records), ZinggJob.Phase.MATCH, null);
      var scored = workflow.match(input, matchConfig);
      assertEquals(List.of("1:3", "2:3"), scored.collect().stream()
          .filter(row -> ((Number) row.get("z_prediction")).intValue() == 1)
          .map(row -> row.get("id") + ":" + row.get("z_id"))
          .sorted().toList());
      assertEquals(PersistedMatchWorkflow.State.MATCHED, workflow.state());
    }
  }

  @Test
  void workflowRejectsRestartBeforeModel(@TempDir Path temp) {
    var config = new RuntimeConfig("jdbc:duckdb:" + temp.resolve("invalid-workflow.duckdb"), 1, 0, null, 1);
    try (var runtime = new CompatibilityRuntime(config, null, "zingg-0.7.0-duckdb-1.5.5.1", null);
         var workflow = new PersistedMatchWorkflow(runtime)) {
      assertThrows(DuckException.class, workflow::restart);
    }
  }

  @Test
  void classifierTrainingRejectsUnmatchedAppliedDecisionWithoutArtifact(@TempDir Path temp) throws Exception {
    Path records = temp.resolve("records.csv");
    Files.writeString(records, "id,block,feature\n1,a,1\n2,a,-1\n");
    Path artifact = temp.resolve("unmatched-classifier");
    var config = new RuntimeConfig("jdbc:duckdb:" + temp.resolve("unmatched.duckdb"), 2, 0, null, 2);
    try (var runtime = new CompatibilityRuntime(config, null);
         var job = new ZinggJob(runtime);
         var workflow = new PersistedMatchWorkflow(runtime)) {
      var input = job.read(List.of(records), ZinggJob.Phase.FIND_TRAINING_DATA);
      workflow.findTrainingData(input, new TrainingPlan.Config("block", "z_block", 100));
      workflow.applyLabels(new LabelDecisionProvider.ApplyLabelsRequest("missing", "v1", "missing",
          List.of(new LabelDecisionProvider.LabelDecision("1", "2", LabelDecisionProvider.Decision.MATCH, "test"),
              new LabelDecisionProvider.LabelDecision("1", "999", LabelDecisionProvider.Decision.NON_MATCH, "test"))));
      var pairs = new Matcher().candidates(input, new Matcher.MatchConfig("id", "block", "1.0", 0.5));
      DuckException failure = assertThrows(DuckException.class, () -> workflow.trainClassifier(
          pairs, "id", "z_id", NativeClassifierTrainer.Config.defaults(
              "zingg-0.7.0", List.of("z_feature"), "z_label", artifact)));
      assertTrue(failure.getMessage().contains("do not match applied decisions"));
      assertTrue(Files.notExists(artifact));
      assertEquals(PersistedMatchWorkflow.State.LABELS_APPLIED, workflow.state());
    }
  }

  @Test
  void restartRejectsCorruptClassifierAndKeepsWorkflowUnstarted(@TempDir Path temp) throws Exception {
    Path artifact = temp.resolve("corrupt-classifier");
    new NativeClassifierArtifact().write(new NativeClassifierArtifact.Config(
        "zingg-0.7.0", List.of("z_feature"), new double[]{1.0}, 0.0, true, artifact));
    Files.writeString(artifact.resolve("model.bin"), "tampered");
    var config = new RuntimeConfig("jdbc:duckdb:" + temp.resolve("corrupt.duckdb"), 1, 0, null, 1);
    try (var runtime = new CompatibilityRuntime(config, null);
         var workflow = new PersistedMatchWorkflow(runtime)) {
      DuckException failure = assertThrows(DuckException.class, () -> workflow.restartFromModel(artifact));
      assertTrue(failure.getMessage().contains("checksum mismatch"));
      assertEquals(PersistedMatchWorkflow.State.CREATED, workflow.state());
      assertEquals(null, workflow.model());
    }
  }

  @Test
  void nulBearingPairIdsRemainDistinctThroughClassifierLabelDerivation(@TempDir Path temp) {
    var config = new RuntimeConfig("jdbc:duckdb:" + temp.resolve("nul-pair-workflow.duckdb"),
        1, 0, null, 2);
    Path artifact = temp.resolve("nul-pair-classifier");
    Path labels = temp.resolve("nul-pair-labels");
    var positive = new LabelDecisionProvider.LabelDecision("a\u0000b", "c",
        LabelDecisionProvider.Decision.MATCH, "human");
    var negative = new LabelDecisionProvider.LabelDecision("a", "b\u0000c",
        LabelDecisionProvider.Decision.NON_MATCH, "human");
    try (var runtime = new CompatibilityRuntime(config, null,
             "zingg-0.7.0-duckdb-1.5.5.1", null, new PersistentLabelDecisionProvider(labels));
         var job = new ZinggJob(runtime);
         var workflow = new PersistedMatchWorkflow(runtime)) {
      workflow.findTrainingData(job.sql("SELECT 1 AS z_zid, 1 AS seed"),
          new TrainingPlan.Config("seed", "z_block", 10));
      var applied = workflow.applyLabels(new LabelDecisionProvider.ApplyLabelsRequest(
          "nul-pairs", "v1", "nul-pairs", List.of(positive, negative)));
      assertEquals(2, applied.applied());
      assertEquals(2, workflow.appliedLabels());
    }

    try (var runtime = new CompatibilityRuntime(config, null,
             "zingg-0.7.0-duckdb-1.5.5.1", null, new PersistentLabelDecisionProvider(labels));
         var job = new ZinggJob(runtime);
         var workflow = new PersistedMatchWorkflow(runtime)) {
      workflow.findTrainingData(job.sql("SELECT 1 AS z_zid, 1 AS seed"),
          new TrainingPlan.Config("seed", "z_block", 10));
      workflow.restoreAppliedLabels();
      assertEquals(2, workflow.appliedLabels());
      assertEquals(PersistedMatchWorkflow.State.LABELS_APPLIED, workflow.state());
      var pairFeatures = job.sql("SELECT 'a' || chr(0) || 'b' AS left_id, 'c' AS right_id, 1.0 AS z_feature "
          + "UNION ALL SELECT 'a', 'b' || chr(0) || 'c', -1.0");
      var trained = workflow.trainClassifier(pairFeatures, "left_id", "right_id",
          NativeClassifierTrainer.Config.defaults("zingg-0.7.0", List.of("z_feature"), "z_label", artifact));

      assertEquals(2, trained.rows());
      assertTrue(Files.exists(artifact.resolve("manifest.json")));
    }
  }
}
