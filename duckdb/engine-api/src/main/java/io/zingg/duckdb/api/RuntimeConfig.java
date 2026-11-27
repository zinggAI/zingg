package io.zingg.duckdb.api;

import java.nio.file.Path;
import java.util.Set;

public record RuntimeConfig(String databaseUrl, int threads, long memoryLimitBytes, Path tempDirectory, int maxConcurrentJobs, long maxTempDirectoryBytes, boolean offlineMode, Set<String> allowedExtensions) {
  public RuntimeConfig(String databaseUrl, int threads, long memoryLimitBytes, Path tempDirectory, int maxConcurrentJobs) {
    this(databaseUrl, threads, memoryLimitBytes, tempDirectory, maxConcurrentJobs, 0, true, Set.of());
  }
  public RuntimeConfig(String databaseUrl, int threads, long memoryLimitBytes, Path tempDirectory, int maxConcurrentJobs, long maxTempDirectoryBytes) {
    this(databaseUrl, threads, memoryLimitBytes, tempDirectory, maxConcurrentJobs, maxTempDirectoryBytes, true, Set.of());
  }
  public RuntimeConfig {
    if (databaseUrl==null||databaseUrl.isBlank()) databaseUrl="jdbc:duckdb:";
    if (threads<1) throw new IllegalArgumentException("threads must be positive");
    if (memoryLimitBytes<0) throw new IllegalArgumentException("memory limit cannot be negative");
    if (maxConcurrentJobs<1) throw new IllegalArgumentException("maxConcurrentJobs must be positive");
    if (maxTempDirectoryBytes<0) throw new IllegalArgumentException("max temp directory size cannot be negative");
    allowedExtensions=Set.copyOf(allowedExtensions==null?Set.of():allowedExtensions);
    if (allowedExtensions.stream().anyMatch(x->x==null||x.isBlank()||!x.matches("[a-z0-9_]+"))) throw new IllegalArgumentException("invalid extension allowlist entry");
  }
  public static RuntimeConfig defaults(){return new RuntimeConfig("jdbc:duckdb:",Runtime.getRuntime().availableProcessors(),0,null,1,0,true,Set.of());}
  public ConnectorPolicy connectorPolicy(){return new ConnectorPolicy(offlineMode,allowedExtensions);}
}
