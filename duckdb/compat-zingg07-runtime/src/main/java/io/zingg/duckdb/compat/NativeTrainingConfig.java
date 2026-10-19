package io.zingg.duckdb.compat;

import java.nio.file.Path;
import java.util.List;

/** Explicit, deterministic inputs for the native TRAIN phase. */
public record NativeTrainingConfig(String profile, String blockingExpression, String blockingColumn,
    List<String> features, Path artifactDirectory, long maxRows, long maxArtifactBytes) {
  public NativeTrainingConfig {
    if (profile == null || profile.isBlank()) throw new IllegalArgumentException("profile is required");
    if (blockingExpression == null || blockingExpression.isBlank()) throw new IllegalArgumentException("blockingExpression is required");
    if (blockingColumn == null || blockingColumn.isBlank()) throw new IllegalArgumentException("blockingColumn is required");
    features = List.copyOf(features == null ? List.of() : features);
    if (features.stream().anyMatch(f -> f == null || f.isBlank()))
      throw new IllegalArgumentException("blocking feature names must be non-empty");
    if (artifactDirectory == null) throw new IllegalArgumentException("artifactDirectory is required");
    if (maxRows < 1) throw new IllegalArgumentException("maxRows must be positive");
    if (maxArtifactBytes < 0) throw new IllegalArgumentException("maxArtifactBytes cannot be negative");
  }
  public NativeTrainingConfig(String profile, String blockingExpression, String blockingColumn,
      List<String> features, Path artifactDirectory, long maxRows) {
    this(profile, blockingExpression, blockingColumn, features, artifactDirectory, maxRows, 0);
  }
  public static NativeTrainingConfig defaults(Path artifactDirectory, String expression, String column) {
    return new NativeTrainingConfig("zingg-0.7.0", expression, column, List.of(), artifactDirectory, 1_000_000);
  }
}
