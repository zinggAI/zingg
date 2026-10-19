package io.zingg.duckdb.api;
public record SplitResult(Frame matching,Frame remaining){public SplitResult{if(matching==null||remaining==null)throw new IllegalArgumentException("split frames required");if(!matching.owner().equals(remaining.owner()))throw new IllegalArgumentException("split frames must share owner");}}
