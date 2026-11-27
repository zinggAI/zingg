package io.zingg.duckdb.api;
public enum OutputFormat { CSV, PARQUET, JSON, ARROW;
  public static OutputFormat fromPath(java.nio.file.Path path){String n=path.getFileName().toString().toLowerCase(java.util.Locale.ROOT);if(n.endsWith(".parquet"))return PARQUET;if(n.endsWith(".json")||n.endsWith(".jsonl"))return JSON;if(n.endsWith(".arrow")||n.endsWith(".feather"))return ARROW;return CSV;}
}
