package io.zingg.duckdb.engine;

/** Java-compatible scalar semantics used at the narrow boundary with Zingg. */
public final class CompatibilityFunctions {
  private CompatibilityFunctions() {}
  public static int javaHash(String value) { return value==null?0:value.hashCode(); }
  public static long javaRound(double value) { return Math.round(value); }
  public static String addUniqueCol(String value,long epochMillis) { return epochMillis+":"+value; }
  public static String normalize(String value) { return value==null?null:value.trim().toLowerCase(java.util.Locale.ROOT); }
}
