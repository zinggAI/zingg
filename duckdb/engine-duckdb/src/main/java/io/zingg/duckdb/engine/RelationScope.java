package io.zingg.duckdb.engine;

/** Lifetime/visibility contract for a relation handle. */
public enum RelationScope {
  QUERY_ONLY,
  CONNECTION_LOCAL,
  WORKER_SHARED,
  PERSISTED
}
