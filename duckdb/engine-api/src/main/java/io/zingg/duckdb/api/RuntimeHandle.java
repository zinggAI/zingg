package io.zingg.duckdb.api;
public interface RuntimeHandle extends AutoCloseable { JobHandle openJob(); @Override void close(); }
