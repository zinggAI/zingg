package io.zingg.duckdb.api;

import java.util.List;

/** Central logical-column lookup used by the engine and compatibility adapter. */
public final class ColumnResolver {
  private ColumnResolver() {}

  /** Resolves a backend column without silently accepting two case-insensitive matches. */
  public static String resolve(List<String> columns, String requested) {
    if (columns == null || requested == null || requested.isBlank()) return null;
    String match = null;
    for (String column : columns) {
      if (column.equalsIgnoreCase(requested)) {
        if (match != null && !match.equals(column))
          throw new DuckException("ambiguous column: " + requested);
        match = column;
      }
    }
    return match;
  }

  /** Spark's duplicate-name lookup contract is the last matching ordinal. */
  public static int lastIndexOf(List<String> columns, String requested) {
    if (columns == null || requested == null) return -1;
    for (int index = columns.size() - 1; index >= 0; index--)
      if (columns.get(index).equals(requested)) return index;
    return -1;
  }
}
