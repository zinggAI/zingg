package io.zingg.duckdb.model;
public record ImportLimits(long maxBytes,int maxDepth,long maxReferences,long timeoutMillis){
 public static final long MAX_BYTES=4L*1024*1024*1024; public static final int MAX_DEPTH=4096; public static final long MAX_REFERENCES=10_000_000; public static final long MAX_TIMEOUT_MILLIS=3_600_000;
 public ImportLimits{if(maxBytes<1||maxBytes>MAX_BYTES||maxDepth<1||maxDepth>MAX_DEPTH||maxReferences<1||maxReferences>MAX_REFERENCES||timeoutMillis<1||timeoutMillis>MAX_TIMEOUT_MILLIS)throw new IllegalArgumentException("import limits outside allowed range");}
 public static ImportLimits defaults(){return new ImportLimits(256L*1024*1024,64,1_000_000,120_000);}
}
