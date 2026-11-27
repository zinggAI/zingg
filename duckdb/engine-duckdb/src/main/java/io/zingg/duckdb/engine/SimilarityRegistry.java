package io.zingg.duckdb.engine;

import io.zingg.duckdb.api.DuckException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;

/** Explicit, immutable-by-default registry for two-value compatibility similarities. */
public final class SimilarityRegistry {
  private final Map<String, BiFunction<String, String, Double>> functions = new LinkedHashMap<>();
  public SimilarityRegistry() { this(false); }
  public SimilarityRegistry(boolean exactSecondStringJaccard) {
    functions.put("exact", SimilarityFunctions::exact);
    functions.put("jaccard", exactSecondStringJaccard
        ? SimilarityFunctions::secondStringJaccard : SimilarityFunctions::jaccard);
    functions.put("normalized_levenshtein", SimilarityFunctions::normalizedLevenshtein);
    functions.put("jaro", SimilarityFunctions::jaro);
    functions.put("jaro_winkler", SimilarityFunctions::jaro);
  }
  public Set<String> names() { return Collections.unmodifiableSet(functions.keySet()); }
  public double apply(String name, String left, String right) {
    var fn = functions.get(name);
    if (fn == null) throw new DuckException("unsupported similarity function: " + name);
    double value = fn.apply(left, right);
    if (!Double.isFinite(value) || value < 0d || value > 1d) throw new DuckException("similarity result outside [0,1]: " + name);
    return value;
  }
  public void register(String name, BiFunction<String, String, Double> function) {
    if (name == null || !name.matches("[a-z][a-z0-9_]*")) throw new IllegalArgumentException("invalid similarity name");
    if (functions.putIfAbsent(name, Objects.requireNonNull(function)) != null) throw new DuckException("similarity already registered: " + name);
  }
}
