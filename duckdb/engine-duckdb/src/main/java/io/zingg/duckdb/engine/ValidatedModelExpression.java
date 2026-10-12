package io.zingg.duckdb.engine;

import io.zingg.duckdb.api.Expression;

/** A classifier-score expression restricted to the engine's numeric model-expression grammar. */
public final class ValidatedModelExpression implements Expression {
  private final String sql;

  public ValidatedModelExpression(String sql) {
    this.sql = SqlSafety.modelNumericExpression(sql);
  }

  @Override
  public String sql() {
    return sql;
  }
}
