package io.zingg.duckdb.engine;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ProcessResourceSnapshotTest {
  @Test
  void reportsCurrentProcessResidentMemory() {
    assertTrue(ProcessResourceSnapshot.residentBytes() > 0,
        "current platform must provide a process RSS measurement");
  }
}
