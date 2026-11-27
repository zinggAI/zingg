package io.zingg.duckdb.model;
import java.time.Instant;
public record ImportProvenance(String sourcePath,String sourceVersion,String importerVersion,Instant importedAt,String sha256){public ImportProvenance{if(sourcePath==null||sourcePath.isBlank())throw new IllegalArgumentException("sourcePath required");if(importerVersion==null||importerVersion.isBlank())throw new IllegalArgumentException("importerVersion required");if(importedAt==null)importedAt=Instant.now();}}
