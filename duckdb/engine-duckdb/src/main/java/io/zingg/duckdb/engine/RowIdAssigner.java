package io.zingg.duckdb.engine;
import io.zingg.duckdb.api.Frame;
import java.util.Objects;
public final class RowIdAssigner {
 private RowIdAssigner(){}
 /** Must be called after the phase-specific multi-input union. */
 public static Frame assign(Frame merged,String column){Objects.requireNonNull(merged);if(column==null||column.isBlank())throw new IllegalArgumentException("id column required");return merged.withColumn(column,new DuckExpr("row_number() OVER () - 1"));}
}
