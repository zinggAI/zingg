package io.zingg.duckdb.compat;
import java.util.*;
public record CompatibilityProfile(String id,String zinggVersion,String duckdbVersion,String sparkReference,Map<String,String> rules){public CompatibilityProfile{if(id==null||id.isBlank()||zinggVersion==null||duckdbVersion==null)throw new IllegalArgumentException("profile identity required");rules=Collections.unmodifiableMap(new LinkedHashMap<>(rules==null?Map.of():rules));}}
