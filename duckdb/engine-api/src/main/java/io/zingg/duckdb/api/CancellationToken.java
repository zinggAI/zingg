package io.zingg.duckdb.api;
public interface CancellationToken { boolean isCancelled(); default void throwIfCancelled(){if(isCancelled())throw new DuckException("operation cancelled");} static CancellationToken none(){return ()->false;} }
