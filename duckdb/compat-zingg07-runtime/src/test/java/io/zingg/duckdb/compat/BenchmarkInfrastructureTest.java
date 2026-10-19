package io.zingg.duckdb.compat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.zingg.duckdb.api.RuntimeDiagnostics;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class BenchmarkInfrastructureTest {
  @Test
  void historyComparesOnlyMatchingIdentities(@TempDir Path temp) throws Exception {
    var history = new BenchmarkHistory(temp.resolve("history.jsonl"));
    var baseline = new BenchmarkHistory.Measurement("profile", "input", "config", 100, 100, 100, 100, Instant.now());
    var current = new BenchmarkHistory.Measurement("profile", "input", "config", 110, 120, 100, 119, Instant.now());
    history.append(baseline);
    assertEquals(1, Files.readAllLines(temp.resolve("history.jsonl")).size());
    assertTrue(history.compare(current, baseline, BenchmarkHistory.Thresholds.defaults()).compatible());
    var different = new BenchmarkHistory.Measurement("profile", "other-input", "config", 1, 1, 1, 1, Instant.now());
    assertFalse(history.compare(different, baseline, BenchmarkHistory.Thresholds.defaults()).compatible());
  }

  @Test
  void profileCaptureIsWriteOnce(@TempDir Path temp) throws Exception {
    var diagnostics = new RuntimeDiagnostics("1 GiB", "1 GiB", ".tmp", "2", 10, 100, 20, 0);
    var capture = new ProfileCapture("profile", "build", "request", diagnostics, "select 1", 12, 3, 20, "input", "config", Instant.now());
    Path written = capture.write(temp);
    assertTrue(Files.size(written) > 0);
    assertThrows(java.nio.file.FileAlreadyExistsException.class, () -> capture.write(temp));
  }

  @Test
  void benchmarkComparisonRejectsRegressionsAndHandlesZeroBaselines() {
    var now = Instant.now();
    var baseline = new BenchmarkHistory.Measurement("profile", "input", "config", 0, 0, 0, 0, now);
    var nonZero = new BenchmarkHistory.Measurement("profile", "input", "config", 1, 0, 0, 0, now);
    var zeroComparison = new BenchmarkHistory(Path.of("ignored.json")).compare(
        nonZero, baseline, BenchmarkHistory.Thresholds.defaults());
    assertFalse(zeroComparison.compatible());
    assertEquals(Double.POSITIVE_INFINITY, zeroComparison.runtimeRatio());

    var current = new BenchmarkHistory.Measurement("profile", "input", "config", 130, 100, 100, 100, now);
    var ordinaryBaseline = new BenchmarkHistory.Measurement("profile", "input", "config", 100, 100, 100, 100, now);
    var regression = new BenchmarkHistory(Path.of("ignored.json")).compare(
        current, ordinaryBaseline, BenchmarkHistory.Thresholds.defaults());
    assertFalse(regression.compatible());
    assertEquals("benchmark regression threshold exceeded", regression.reason());
  }
}
