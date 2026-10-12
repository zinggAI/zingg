package io.zingg.duckdb.model;
import java.util.Map;
public interface ModelScorer { double score(Map<String,Object> features); }
