package io.zingg.duckdb.compat;

import io.zingg.duckdb.api.DuckException;
import java.util.Objects;

/**
 * Registers locally executable native transformations. Their native contracts
 * are kept distinct from upstream Zingg v0.7 differential parity.
 */
public final class LocalPhaseExecutors {
  private LocalPhaseExecutors() {}

  public static void register(CompatibilityRuntime runtime,
      TrainingPlan.Config trainingConfig, Matcher.MatchConfig matchConfig) {
    Objects.requireNonNull(runtime, "runtime is required");
    Objects.requireNonNull(trainingConfig, "training config is required");
    Objects.requireNonNull(matchConfig, "match config is required");
    runtime.registerPhaseExecutor(ZinggJob.Phase.FIND_TRAINING_DATA,
        (job, input) -> job.trainingData(input, trainingConfig));
    runtime.registerPhaseExecutor(ZinggJob.Phase.MATCH,
        (job, input) -> job.matchCandidates(input, matchConfig));
  }

  /** Register the Spark-free native training phase when an artifact target is configured. */
  public static void registerNativeTraining(CompatibilityRuntime runtime, NativeTrainingConfig config) {
    Objects.requireNonNull(runtime, "runtime is required");
    Objects.requireNonNull(config, "native training config is required");
    runtime.registerPhaseExecutor(ZinggJob.Phase.TRAIN, (job, input) -> {
      job.trainNative(input, config);
      return input.cache();
    });
  }

  /** Register native graph closure for scored-pair rows; not an upstream-v0.7 parity claim. */
  public static void registerNativeLink(CompatibilityRuntime runtime) {
    Objects.requireNonNull(runtime, "runtime is required");
    runtime.registerPhaseExecutor(ZinggJob.Phase.LINK,
        (job, input) -> new NativeLinkPhase().execute(job, input, runtime.clock()));
  }

  public static DuckException unsupported(ZinggJob.Phase phase) {
    return new DuckException("phase requires v0.7 differential contract: " + phase);
  }
}
