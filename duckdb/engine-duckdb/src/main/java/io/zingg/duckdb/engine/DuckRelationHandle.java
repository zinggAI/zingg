package io.zingg.duckdb.engine;

import io.zingg.duckdb.api.JobId;
import java.util.Objects;

/** Immutable identity and lifetime metadata for a logical/materialized relation. */
public record DuckRelationHandle(String relationId, RelationScope scope, JobId ownerJob,
    String ownerConnectionId, String plan, boolean materialized) {
  public DuckRelationHandle {
    if (relationId == null || relationId.isBlank() || scope == null || ownerJob == null
        || ownerConnectionId == null || ownerConnectionId.isBlank() || plan == null || plan.isBlank())
      throw new IllegalArgumentException("relation handle identity and plan are required");
  }
}
