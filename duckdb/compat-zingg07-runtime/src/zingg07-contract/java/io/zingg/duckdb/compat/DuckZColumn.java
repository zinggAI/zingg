package io.zingg.duckdb.compat;

import io.zingg.duckdb.api.Expression;
import java.util.Objects;

/** Profile-only expression with the relation provenance needed by ZFrame joins. */
public final class DuckZColumn implements Expression {
  private final String sql;
  private final Object relation;
  private final String columnName;
  private final String alias;

  private DuckZColumn(String sql, Object relation, String columnName, String alias) {
    this.sql = Objects.requireNonNull(sql);
    this.relation = relation;
    this.columnName = columnName;
    this.alias = alias;
  }

  static DuckZColumn column(Object relation, String name) {
    return new DuckZColumn(quote(name), Objects.requireNonNull(relation), name, null);
  }

  static DuckZColumn expression(String sql) { return new DuckZColumn(sql, null, null, null); }

  public DuckZColumn as(String name) {
    if (name == null || name.isBlank()) throw new IllegalArgumentException("column alias is required");
    return new DuckZColumn(sql, relation, columnName, name);
  }

  @Override public String sql() { return sql; }
  public String projectionSql() { return alias == null ? sql : sql + " AS " + quote(alias); }
  String directColumnName() {
    if (columnName == null) throw new UnsupportedOperationException("computed expressions cannot be used as a column name");
    return columnName;
  }
  String qualified(String side) {
    if (columnName == null) throw new UnsupportedOperationException("computed expressions cannot be qualified for a join");
    return side + "." + quote(columnName);
  }
  Object relation() { return relation; }
  String alias() { return alias; }

  static String quote(String name) { return "\"" + name.replace("\"", "\"\"") + "\""; }
}
