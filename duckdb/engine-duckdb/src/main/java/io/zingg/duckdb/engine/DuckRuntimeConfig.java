package io.zingg.duckdb.engine;
import io.zingg.duckdb.api.RuntimeConfig;
public record DuckRuntimeConfig(RuntimeConfig base){ public DuckRuntimeConfig{if(base==null)throw new IllegalArgumentException("config required");} public static DuckRuntimeConfig defaults(){return new DuckRuntimeConfig(RuntimeConfig.defaults());} }
