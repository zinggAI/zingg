package io.zingg.duckdb.api;

/** Read-only runtime settings and JVM memory counters exposed for operations. */
public record RuntimeDiagnostics(String memoryLimit, String maxTempDirectorySize, String tempDirectory,
                                 String threads, long heapUsedBytes, long heapMaxBytes,
                                 long processResidentBytes, long tempDirectoryUsedBytes) {
  public RuntimeDiagnostics(String memoryLimit, String maxTempDirectorySize, String tempDirectory,
                            String threads, long heapUsedBytes, long heapMaxBytes) {
    this(memoryLimit, maxTempDirectorySize, tempDirectory, threads, heapUsedBytes, heapMaxBytes, -1, -1);
  }
}
