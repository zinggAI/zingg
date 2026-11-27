package io.zingg.duckdb.compat;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class ClassifierTrainingBenchmarkMainTest {
  @Test
  void rejectsInvalidRepetitionsBeforeOpeningRuntime() {
    assertThrows(IllegalArgumentException.class, () -> ClassifierTrainingBenchmarkMain.main(
        new String[] {"--rows", "10", "--widths", "1", "--repetitions", "0"}));
  }

  @Test
  void rejectsRowsBelowBinaryClassifierMinimumBeforeOpeningRuntime() {
    assertThrows(IllegalArgumentException.class, () -> ClassifierTrainingBenchmarkMain.main(
        new String[] {"--rows", "1", "--widths", "1"}));
  }

  @Test
  void rejectsEmptyFeatureWidthBeforeOpeningRuntime() {
    assertThrows(IllegalArgumentException.class, () -> ClassifierTrainingBenchmarkMain.main(
        new String[] {"--rows", "10", "--widths", "0"}));
  }
}
