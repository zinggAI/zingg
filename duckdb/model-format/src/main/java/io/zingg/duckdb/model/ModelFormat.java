package io.zingg.duckdb.model;

import io.zingg.duckdb.api.DuckException;

/** Version policy for the neutral model envelope. */
public final class ModelFormat {
  public static final String CURRENT = "zingg-0.1-native";
  private ModelFormat() {}

  public static void requireSupported(String format) {
    if (!CURRENT.equals(format)) throw new DuckException("unsupported model format version: " + format);
  }
}
