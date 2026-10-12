package io.zingg.duckdb.compat;
public record PipelineResult(long inputRows,long outputRows,String phase,String outputPath){}
