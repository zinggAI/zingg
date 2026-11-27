package io.zingg.duckdb.api;
import java.util.UUID;
public record JobId(String value) { public JobId { if(value==null||value.isBlank()) throw new IllegalArgumentException("job id is required"); } public static JobId create(){return new JobId(UUID.randomUUID().toString());} }
