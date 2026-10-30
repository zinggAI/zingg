package io.zingg.duckdb.compat;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.nio.charset.StandardCharsets;
import io.zingg.duckdb.api.CompatibilityClock;
import io.zingg.duckdb.api.DuckException;
import io.zingg.duckdb.api.RuntimeConfig;
import io.zingg.duckdb.model.ModelFormat;
import io.zingg.duckdb.model.ModelManifest;
import io.zingg.duckdb.model.ModelReader;
import org.junit.jupiter.api.Test;

class CompatibilityFunctionsTest {
  @Test
  void blockingKeysNormalizeAndUseSignedJavaHash() {
    assertEquals("Mixed", new BlockingKey(BlockingKey.Mode.RAW).apply("Mixed"));
    assertEquals("mixed", new BlockingKey(BlockingKey.Mode.TRIM_LOWER).apply("  Mixed "));
    assertEquals(Integer.toString("Aa".hashCode()), new BlockingKey(BlockingKey.Mode.JAVA_HASH).apply("Aa"));
    assertNull(new BlockingKey(BlockingKey.Mode.RAW).apply(null));
  }

  @Test
  void polynomialFeaturesUseRepeatedIndexOrdering() {
    assertArrayEquals(new double[] {2, 4, 3, 6, 9}, PolynomialFeatures.expand(new double[] {2, 3}, 2));
    assertEquals(5, PolynomialFeatures.featureCount(2, 2, 5));
    assertEquals(1329, PolynomialFeatures.featureCount(18, 3, 1329));
    assertEquals(List.of("(COALESCE(\"a\", 0))",
        "(COALESCE(\"a\", 0) * COALESCE(\"a\", 0))", "(COALESCE(\"b\", 0))",
        "(COALESCE(\"a\", 0) * COALESCE(\"b\", 0))",
        "(COALESCE(\"b\", 0) * COALESCE(\"b\", 0))"),
        PolynomialFeatures.sqlTerms(List.of("a", "b"), 2));
    assertThrows(IllegalArgumentException.class, () -> PolynomialFeatures.expand(new double[0], 2));
    assertThrows(IllegalArgumentException.class, () -> PolynomialFeatures.featureCount(100_000, 8, 10_000));
  }

  @Test
  void unsupportedPhasesFailExplicitlyInsteadOfReturningIdentity() {
    var registry = new PhaseRegistry();
    assertThrows(io.zingg.duckdb.api.DuckException.class,
        () -> registry.execute(ZinggJob.Phase.TRAIN, null, null));
  }

  @Test
  void duplicatePhaseRegistrationFailsExplicitly() {
    var registry = new PhaseRegistry();
    PhaseExecutor executor = (job, input) -> input;
    registry.register(ZinggJob.Phase.MATCH, executor);
    assertThrows(io.zingg.duckdb.api.DuckException.class,
        () -> registry.register(ZinggJob.Phase.MATCH, executor));
  }

  @Test
  void localPhaseRegistrationExposesOnlyImplementedNativePhases() {
    var runtime = new CompatibilityRuntime("jdbc:duckdb:", CompatibilityClock.system());
    try {
      LocalPhaseExecutors.register(runtime,
          new TrainingPlan.Config("1", "block", 10),
          new Matcher.MatchConfig("id", "block", "1.0", 0.5));
      assertEquals(java.util.Set.of(ZinggJob.Phase.FIND_TRAINING_DATA, ZinggJob.Phase.MATCH),
          runtime.phases().phases());
      assertThrows(io.zingg.duckdb.api.DuckException.class,
          () -> runtime.phases().execute(ZinggJob.Phase.TRAIN, null, null));
      assertEquals("phase requires v0.7 differential contract: LINK",
          LocalPhaseExecutors.unsupported(ZinggJob.Phase.LINK).getMessage());
    } finally {
      runtime.close();
    }
  }

  @Test
  void nativeTrainingCanBeRegisteredAsAnExplicitPhase() {
    var runtime = new CompatibilityRuntime("jdbc:duckdb:", CompatibilityClock.system());
    try {
      var config = NativeTrainingConfig.defaults(java.nio.file.Path.of("target", "phase-training"), "1", "block");
      LocalPhaseExecutors.registerNativeTraining(runtime, config);
      assertEquals(java.util.Set.of(ZinggJob.Phase.TRAIN), runtime.phases().phases());
    } finally {
      runtime.close();
    }
  }

  @Test
  void linkOutputPreservesReleasedAsymmetry() {
    var links = java.util.List.of(
        new LinkOutput.Link(1, 10, .9),
        new LinkOutput.Link(1, 11, .8),
        new LinkOutput.Link(2, 10, .7));
    assertEquals(java.util.List.of(new LinkOutput.Link(1, 10, .9), new LinkOutput.Link(2, 10, .7)),
        LinkOutput.asymmetric(links));
    assertEquals(links, LinkOutput.preserveRight(links));
  }

  @Test
  void malformedOptionalClassifierNumbersAreRejectedInsteadOfDefaulted() {
    assertThrows(io.zingg.duckdb.api.DuckException.class,
        () -> new ModelScorerRegistry().create(new ModelReader.LoadedModel(
            new ModelManifest("zingg-0.7.0", "0.7.0", ModelFormat.CURRENT,
                "CLASSIFIER", List.of("score"), "ignored"),
            "{\"kind\":\"linear-classifier\",\"features\":[\"score\"],\"weights\":[1],\"threshold\":\"bad\"}"
                .getBytes(StandardCharsets.UTF_8))));
  }

  @Test
  void classifierManifestMustExactlyMatchPayloadFeatures() {
    var loaded = new ModelReader.LoadedModel(
        new ModelManifest("zingg-0.7.0", "0.7.0", ModelFormat.CURRENT,
            "CLASSIFIER", List.of(), "ignored"),
        ("{\"kind\":\"linear-classifier\",\"features\":[\"score\"],\"weights\":[1],"
            + "\"intercept\":0,\"logistic\":true,\"threshold\":0.5,"
            + "\"assemblerInputCols\":[],\"polynomialDegree\":0}"
        ).getBytes(StandardCharsets.UTF_8));

    DuckException failure = assertThrows(DuckException.class, () -> new ModelScorerRegistry().create(loaded));
    assertTrue(failure.getMessage().contains("manifest/payload features differ"));
  }

  @Test
  void classifierRejectsDuplicateAssemblerInputsBeforeScoring() {
    var loaded = new ModelReader.LoadedModel(
        new ModelManifest("zingg-0.7.0", "0.7.0", ModelFormat.CURRENT,
            "CLASSIFIER", List.of("x1", "x2"), "ignored"),
        ("{\"kind\":\"linear-classifier\",\"features\":[\"x1\",\"x2\"],\"weights\":[1,1],"
            + "\"intercept\":0,\"logistic\":true,\"threshold\":0.5,"
            + "\"assemblerInputCols\":[\"source\",\"source\"],\"polynomialDegree\":1}"
        ).getBytes(StandardCharsets.UTF_8));

    DuckException failure = assertThrows(DuckException.class, () -> new ModelScorerRegistry().create(loaded));
    assertTrue(failure.getMessage().contains("assemblerInputCols must contain unique nonblank names"));
  }

  @Test
  void strictProfilePersistsReleasedSimilarityQuirk() {
    var profile = new ProfileRegistry().require("zingg-0.7.0-duckdb-1.5.5.1");
    assertEquals("true", profile.rules().get("jaroWinklerDelegatesToJaro"));
    assertEquals("strict-released-quirks", profile.rules().get("profileSemantics"));
  }

  @Test
  void releasedProfileStillUsesHistoricalBlankSafeJaccard() {
    try (var runtime = new CompatibilityRuntime(
        new RuntimeConfig("jdbc:duckdb:", Runtime.getRuntime().availableProcessors(), 0, null, 1),
        CompatibilityClock.system(), ProfileRegistry.RELEASED_PROFILE_ID)) {
      assertEquals(1.0, runtime.similarities().apply("jaccard", "   ", "value"));
      assertEquals(1.0, runtime.similarities().apply("jaccard", ".,;", "!?"));
    }
  }

  @Test
  void promotedDuckdb156ProfileUsesExactSecondStringJaccard() {
    try (var runtime = new CompatibilityRuntime("jdbc:duckdb:", CompatibilityClock.system())) {
      assertEquals(ProfileRegistry.DEFAULT_PROFILE_ID, runtime.profile().id());
      assertEquals("1.5.6.0", runtime.profile().duckdbVersion());
      assertEquals("secondstring-exact", runtime.profile().rules().get("jaccardSemantics"));
      assertEquals(0.0, runtime.similarities().apply("jaccard", "   ", "value"));
      assertEquals(0.0, runtime.similarities().apply("jaccard", ".,;", "!?"));
      assertEquals(1.0, runtime.similarities().apply("jaccard", ".,;", ".,;"));
    }
  }
}
