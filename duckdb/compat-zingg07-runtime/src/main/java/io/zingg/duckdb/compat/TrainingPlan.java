package io.zingg.duckdb.compat;

import io.zingg.duckdb.api.Frame;
import io.zingg.duckdb.engine.DuckExpr;

public final class TrainingPlan {
  public record Config(String blockingExpression, String blockingColumn, long maxRows) {
    public Config {
      if (blockingExpression == null || blockingExpression.isBlank()) {
        throw new IllegalArgumentException("blocking expression required");
      }
      blockingExpression = io.zingg.duckdb.engine.SqlSafety.predicate(blockingExpression);
      if (blockingColumn == null || blockingColumn.isBlank()) {
        throw new IllegalArgumentException("blocking column required");
      }
      if (maxRows < 1) {
        throw new IllegalArgumentException("maxRows must be positive");
      }
    }
  }

  public Frame prepare(Frame input, Config config) {
    // Bound the relation before adding the window column.  Filtering a large
    // row_number window afterwards makes DuckDB lower the plan through
    // arg_min/arg_max; DuckDB rejects the default n=1,000,000 boundary even
    // when the source relation contains only a few rows.
    Frame bounded = input
        .withColumn(config.blockingColumn(), new DuckExpr(config.blockingExpression()))
        .sort("z_zid")
        .limit(config.maxRows());
    return bounded
        .selectExpr("*", "row_number() OVER (ORDER BY z_zid) AS z_training_id")
        .cache();
  }
}
