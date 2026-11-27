package io.zingg.duckdb.api;
public interface JobHandle extends AutoCloseable { JobId id(); Frame table(String name); Frame sql(String sql); @Override void close(); }
