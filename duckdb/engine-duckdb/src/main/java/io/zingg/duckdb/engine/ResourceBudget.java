package io.zingg.duckdb.engine;
public record ResourceBudget(long memoryLimitBytes,long maxRows,long maxInputBytes,long maxOutputBytes,long maxCollectBytes,long maxSpillBytes){
 public ResourceBudget{if(memoryLimitBytes<0||maxRows<0||maxInputBytes<0||maxOutputBytes<0||maxCollectBytes<0||maxSpillBytes<0)throw new IllegalArgumentException("resource limits cannot be negative");}
 public ResourceBudget(long memoryLimitBytes,long maxRows,long maxInputBytes,long maxOutputBytes,long maxSpillBytes){this(memoryLimitBytes,maxRows,maxInputBytes,maxOutputBytes,0,maxSpillBytes);}
 public ResourceBudget(long memoryLimitBytes,long maxRows,long maxSpillBytes){this(memoryLimitBytes,maxRows,0,0,0,maxSpillBytes);}
 public static ResourceBudget unlimited(){return new ResourceBudget(0,0,0,0,0,0);}
 public void enforceRows(long rows){if(maxRows>0&&rows>maxRows)throw new io.zingg.duckdb.api.DuckException("row budget exceeded: "+rows+" > "+maxRows);}
 public void enforceInputBytes(long bytes){if(maxInputBytes>0&&bytes>maxInputBytes)throw new io.zingg.duckdb.api.DuckException("input byte budget exceeded: "+bytes+" > "+maxInputBytes);}
 public void enforceOutputBytes(long bytes){if(maxOutputBytes>0&&bytes>maxOutputBytes)throw new io.zingg.duckdb.api.DuckException("output byte budget exceeded: "+bytes+" > "+maxOutputBytes);}
 public void enforceCollectBytes(long bytes){if(maxCollectBytes>0&&bytes>maxCollectBytes)throw new io.zingg.duckdb.api.DuckException("collect byte budget exceeded: "+bytes+" > "+maxCollectBytes);}
}
