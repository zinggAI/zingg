package io.zingg.duckdb.compat;

import io.zingg.duckdb.api.DuckException;
import io.zingg.duckdb.model.ModelScorer;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/** Immutable linear scorer with optional Spark-compatible polynomial expansion. */
public final class LinearClassifier implements ModelScorer {
  private final List<String> features;
  private final List<String> baseFeatures;
  private final int polynomialDegree;
  private final double[] weights;
  private final double intercept;
  private final boolean logistic;
  private final double threshold;

  public LinearClassifier(List<String> features, double[] weights, double intercept, boolean logistic) {
    this(features, weights, intercept, logistic, 0.5d);
  }

  public LinearClassifier(List<String> features, double[] weights, double intercept,
      boolean logistic, double threshold) {
    this(features, List.of(), 0, weights, intercept, logistic, threshold);
  }

  public LinearClassifier(List<String> features, List<String> baseFeatures, int polynomialDegree,
      double[] weights, double intercept, boolean logistic, double threshold) {
    if (features == null || weights == null || features.size() != weights.length || features.isEmpty())
      throw new IllegalArgumentException("feature/weight dimensions differ");
    requireUniqueNames(features, "features");
    if (Arrays.stream(weights).anyMatch(weight -> !Double.isFinite(weight))
        || !Double.isFinite(intercept) || !Double.isFinite(threshold) || threshold < 0 || threshold > 1)
      throw new IllegalArgumentException("invalid classifier coefficients or threshold");
    List<String> checkedBaseFeatures = List.copyOf(baseFeatures == null ? List.of() : baseFeatures);
    requireUniqueNames(checkedBaseFeatures, "base features");
    if (checkedBaseFeatures.isEmpty()) {
      if (polynomialDegree != 0)
        throw new IllegalArgumentException("non-polynomial classifier degree must be zero");
    } else {
      if (PolynomialFeatures.featureCount(checkedBaseFeatures.size(), polynomialDegree, features.size())
          != features.size())
        throw new IllegalArgumentException("classifier polynomial feature dimensions differ");
    }
    this.features = List.copyOf(features);
    this.baseFeatures = checkedBaseFeatures;
    this.polynomialDegree = polynomialDegree;
    this.weights = weights.clone();
    this.intercept = intercept;
    this.logistic = logistic;
    this.threshold = threshold;
  }

  private static void requireUniqueNames(List<String> names, String field) {
    if (names.stream().anyMatch(name -> name == null || name.isBlank())
        || names.stream().distinct().count() != names.size())
      throw new IllegalArgumentException("classifier " + field + " must contain unique nonblank names");
  }

  public double score(Map<String, Object> values) {
    if (values == null) throw new DuckException("feature values required");
    double[] vector;
    if (!baseFeatures.isEmpty()) {
      double[] base = new FeatureVectorizer(baseFeatures, FeatureVectorizer.MissingPolicy.FAIL).vector(values);
      vector = PolynomialFeatures.expand(base, polynomialDegree);
    } else {
      vector = new FeatureVectorizer(features, FeatureVectorizer.MissingPolicy.FAIL).vector(values);
    }
    return scoreVector(vector);
  }

  public double scoreVector(double[] vector) {
    if (vector == null || vector.length != weights.length)
      throw new DuckException("feature vector dimensions differ");
    double value = intercept;
    for (int index = 0; index < weights.length; index++) value += weights[index] * vector[index];
    return logistic ? 1d / (1d + Math.exp(-value)) : value;
  }

  public List<String> features() { return features; }
  public double[] weights() { return weights.clone(); }
  public double intercept() { return intercept; }
  public boolean logistic() { return logistic; }
  public double threshold() { return threshold; }
  public List<String> baseFeatures() { return baseFeatures; }
  public int polynomialDegree() { return polynomialDegree; }
}
