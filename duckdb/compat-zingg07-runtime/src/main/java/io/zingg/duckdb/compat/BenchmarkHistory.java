package io.zingg.duckdb.compat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.Instant;
import java.util.*;

/** Append-only benchmark history with explicit, profile-scoped regression gates. */
public final class BenchmarkHistory {
  public record Thresholds(double maxRuntimeRatio, double maxRssRatio, double maxSpillRatio, double maxOutputRatio) {
    public Thresholds {
      if (!Double.isFinite(maxRuntimeRatio) || !Double.isFinite(maxRssRatio) || !Double.isFinite(maxSpillRatio)
          || !Double.isFinite(maxOutputRatio) || maxRuntimeRatio < 0 || maxRssRatio < 0 || maxSpillRatio < 0 || maxOutputRatio < 0)
        throw new IllegalArgumentException("benchmark thresholds must be finite and non-negative");
    }
    public static Thresholds defaults() { return new Thresholds(1.20, 1.25, 1.25, 1.20); }
  }

  public record Measurement(String profile, String inputDigest, String configurationDigest,
      long runtimeMillis, long rssBytes, long spillBytes, long outputBytes, Instant capturedAt) {
    public Measurement {
      if (profile == null || profile.isBlank() || inputDigest == null || inputDigest.isBlank()
          || configurationDigest == null || configurationDigest.isBlank() || capturedAt == null)
        throw new IllegalArgumentException("benchmark identity is required");
      if (runtimeMillis < 0 || rssBytes < 0 || spillBytes < 0 || outputBytes < 0)
        throw new IllegalArgumentException("benchmark measurements cannot be negative");
    }
  }

  public record Comparison(boolean compatible, String reason, double runtimeRatio,
      double rssRatio, double spillRatio, double outputRatio) {}

  private final Path file;
  public BenchmarkHistory(Path file) { this.file = Objects.requireNonNull(file).toAbsolutePath().normalize(); }

  public void append(Measurement measurement) throws IOException {
    Objects.requireNonNull(measurement);
    Files.createDirectories(file.getParent());
    String line = "{\"profile\":"+q(measurement.profile())+",\"inputDigest\":"+q(measurement.inputDigest())
        +",\"configurationDigest\":"+q(measurement.configurationDigest())+",\"runtimeMillis\":"+measurement.runtimeMillis()
        +",\"rssBytes\":"+measurement.rssBytes()+",\"spillBytes\":"+measurement.spillBytes()
        +",\"outputBytes\":"+measurement.outputBytes()+",\"capturedAt\":"+q(measurement.capturedAt().toString())+"}\n";
    Files.writeString(file, line, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
  }

  public Comparison compare(Measurement current, Measurement baseline, Thresholds thresholds) {
    Objects.requireNonNull(current); Objects.requireNonNull(baseline); Objects.requireNonNull(thresholds);
    if (!current.profile().equals(baseline.profile()) || !current.inputDigest().equals(baseline.inputDigest())
        || !current.configurationDigest().equals(baseline.configurationDigest()))
      return new Comparison(false, "profile, input, or configuration identity differs", 0, 0, 0, 0);
    double runtime = ratio(current.runtimeMillis(), baseline.runtimeMillis());
    double rss = ratio(current.rssBytes(), baseline.rssBytes());
    double spill = ratio(current.spillBytes(), baseline.spillBytes());
    double output = ratio(current.outputBytes(), baseline.outputBytes());
    boolean ok = runtime <= thresholds.maxRuntimeRatio() && rss <= thresholds.maxRssRatio()
        && spill <= thresholds.maxSpillRatio() && output <= thresholds.maxOutputRatio();
    return new Comparison(ok, ok ? "within thresholds" : "benchmark regression threshold exceeded", runtime, rss, spill, output);
  }

  private static double ratio(long current, long baseline) { return baseline == 0 ? (current == 0 ? 1d : Double.POSITIVE_INFINITY) : (double) current / baseline; }
  private static String q(String value) { return "\""+value.replace("\\","\\\\").replace("\"","\\\"")+"\""; }
}
