package io.zingg.duckdb.compat;

import io.zingg.duckdb.api.DuckException;
import io.zingg.duckdb.api.Frame;
import io.zingg.duckdb.api.CancellationToken;
import io.zingg.duckdb.engine.DuckCancellation;
import io.zingg.duckdb.model.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Comparator;
import java.util.TreeMap;

/** Produces a neutral, Spark-free native TRAIN artifact. */
public final class NativeTrainingService {
  private final String importerVersion;
  public NativeTrainingService() { this("zingg-duckdb-native-0.1"); }
  public NativeTrainingService(String importerVersion) {
    if (importerVersion == null || importerVersion.isBlank()) throw new IllegalArgumentException("importerVersion is required");
    this.importerVersion = importerVersion;
  }
  public Result train(Frame input, NativeTrainingConfig config) {
    if (input == null || config == null) throw new IllegalArgumentException("training input and config are required");
    var requestCancellation = DuckCancellation.current();
    CancellationToken cancellation = requestCancellation == null
        ? CancellationToken.none() : requestCancellation;
    cancellation.throwIfCancelled();
    Frame prepared = new TrainingPlan().prepare(input, new TrainingPlan.Config(config.blockingExpression(), config.blockingColumn(), config.maxRows()));
    cancellation.throwIfCancelled();
    long rows = prepared.count(cancellation);
    // TreeMap makes the serialized tree independent of input partition/order.
    // A plain string sentinel makes a real blocking value such as
    // "__NULL__" indistinguishable from SQL NULL. Keep nullness in the key
    // type so histogram counts and serialized models remain injective.
    var frequencies = new TreeMap<BlockingKey,Long>(Comparator
        .comparing(BlockingKey::isNull).thenComparing(BlockingKey::value,
            Comparator.nullsFirst(String::compareTo)));
    int blockingIndex = prepared.columns().indexOf(config.blockingColumn());
    if (blockingIndex < 0) throw new DuckException("training blocking column is not present: " + config.blockingColumn());
    var trainingRows = prepared.collect(cancellation);
    for (int rowIndex = 0; rowIndex < trainingRows.size(); rowIndex++) {
      if ((rowIndex & 0x3ff) == 0) cancellation.throwIfCancelled();
      var row = trainingRows.get(rowIndex);
      Object value = row.values().get(blockingIndex);
      BlockingKey key = value == null ? new BlockingKey(true, null)
          : new BlockingKey(false, String.valueOf(value));
      frequencies.merge(key, 1L, Long::sum);
    }
    long maxBucketSize = frequencies.values().stream().mapToLong(Long::longValue).max().orElse(0L);
    String frequencyJson = frequencies.entrySet().stream()
        .map(e -> "{\"isNull\":" + e.getKey().isNull() + ",\"key\":"
            + (e.getKey().isNull() ? "null" : "\"" + esc(e.getKey().value()) + "\"")
            + ",\"count\":" + e.getValue() + "}")
        .collect(java.util.stream.Collectors.joining(","));
    String payload = "{\"kind\":\"duckdb-blocking-histogram\",\"profile\":\"" + esc(config.profile())
        + "\",\"blockingExpression\":\"" + esc(config.blockingExpression())
        + "\",\"blockingColumn\":\"" + esc(config.blockingColumn())
        + "\",\"features\":[" + config.features().stream().map(f -> "\"" + esc(f) + "\"").collect(java.util.stream.Collectors.joining(","))
        + "],\"sampleRows\":" + rows + ",\"distinctBlockingKeys\":" + frequencies.size()
        + ",\"maxBucketSize\":" + maxBucketSize + ",\"blockingFrequencies\":[" + frequencyJson + "]}";
    try {
      cancellation.throwIfCancelled();
      Path root = config.artifactDirectory().toAbsolutePath().normalize();
      var manifest = new ModelManifest("duckdb-native-0.1", "0.1.0", "zingg-0.1-native", ModelType.BACKEND_BLOCKING_HISTOGRAM.name(), config.features(), "");
      var provenance = new ImportProvenance(root.toString(), "0.7.0", importerVersion, Instant.now(), "");
      var writerCancellation = DuckCancellation.current();
      new ModelArtifactWriter(root, writerCancellation == null ? () -> false : writerCancellation::isCancelled)
          .write(manifest, payload.getBytes(StandardCharsets.UTF_8), provenance,
              config.maxArtifactBytes());
      return new Result(root, rows, prepared.columns());
    } catch (Exception e) {
      if (e instanceof DuckException de) throw de;
      if (e instanceof java.io.IOException io && io.getMessage() != null
          && io.getMessage().startsWith("model artifact byte budget exceeded:"))
        throw new DuckException(io.getMessage(), io);
      throw new DuckException("native training artifact write failed", e);
    }
  }
  private static String esc(String value) {
    StringBuilder out = new StringBuilder(value.length() + 8);
    for (int i = 0; i < value.length(); i++) {
      char c = value.charAt(i);
      switch (c) {
        case '\\' -> out.append("\\\\");
        case '"' -> out.append("\\\"");
        case '\b' -> out.append("\\b");
        case '\f' -> out.append("\\f");
        case '\n' -> out.append("\\n");
        case '\r' -> out.append("\\r");
        case '\t' -> out.append("\\t");
        default -> { if (c < 0x20) out.append(String.format("\\u%04x", (int) c)); else out.append(c); }
      }
    }
    return out.toString();
  }
  public record Result(Path artifactDirectory, long sampledRows, java.util.List<String> columns) { public Result { columns = java.util.List.copyOf(columns); } }
  private record BlockingKey(boolean isNull, String value) {}
}
