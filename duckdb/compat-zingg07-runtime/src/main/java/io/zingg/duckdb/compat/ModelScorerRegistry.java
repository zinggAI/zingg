package io.zingg.duckdb.compat;

import io.zingg.duckdb.api.DuckException;
import io.zingg.duckdb.model.ModelArtifactJson;
import io.zingg.duckdb.model.ModelReader;
import io.zingg.duckdb.model.ModelScorer;
import io.zingg.duckdb.model.ModelType;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

public final class ModelScorerRegistry {
  private static final Set<String> NATIVE_CLASSIFIER_FIELDS = Set.of("kind", "features", "weights", "intercept",
      "logistic", "threshold", "assemblerInputCols", "polynomialDegree");

  private final Map<String, Supplier<ModelScorer>> scorers = new HashMap<>();

  public ModelScorerRegistry() {
    register("linear", () -> new LinearClassifier(List.of("score"), new double[] {1d}, 0d, true));
    register("threshold", () -> new ClassifierScorer(0.5d));
  }

  public void register(String type, Supplier<ModelScorer> scorer) {
    if (type == null || !type.matches("[a-z][a-z0-9_-]*")) throw new IllegalArgumentException("invalid scorer type");
    if (scorers.putIfAbsent(type, Objects.requireNonNull(scorer)) != null)
      throw new DuckException("scorer already registered: " + type);
  }

  public ModelScorer create(String type) {
    var factory = scorers.get(type);
    if (factory == null) throw new DuckException("unsupported scorer type: " + type);
    return factory.get();
  }

  public ModelScorer create(ModelReader.LoadedModel model) {
    if (model == null) throw new DuckException("model is required");
    if (!ModelType.CLASSIFIER.name().equals(model.manifest().modelType()))
      throw new DuckException("model is not a classifier");
    String payload = new String(model.payload(), StandardCharsets.UTF_8);
    Map<String, Object> fields = ModelArtifactJson.parseObject(payload);
    if (!fields.keySet().equals(NATIVE_CLASSIFIER_FIELDS))
      throw new DuckException("native classifier payload has missing or unknown fields");
    if (!"linear-classifier".equals(fields.get("kind")))
      throw new DuckException("unsupported native classifier payload: " + fields.get("kind"));

    List<String> features = stringList(fields.get("features"), "features");
    requireUniqueNames(features, "features");
    if (!model.manifest().features().equals(features)) {
      int mismatch = 0;
      while (mismatch < features.size() && mismatch < model.manifest().features().size()
          && features.get(mismatch).equals(model.manifest().features().get(mismatch))) mismatch++;
      throw new DuckException("classifier manifest/payload features differ at index " + mismatch
          + " (manifest count=" + model.manifest().features().size() + ", payload count=" + features.size() + ")");
    }
    double[] weights = numberList(fields.get("weights"), "weights");
    if (features.isEmpty() || features.size() != weights.length)
      throw new DuckException("classifier feature/weight dimensions differ");
    double intercept = finiteNumber(fields.get("intercept"), "intercept");
    double threshold = finiteNumber(fields.get("threshold"), "threshold");
    if (!(fields.get("logistic") instanceof Boolean logistic))
      throw new DuckException("classifier logistic field must be boolean");
    List<String> baseFeatures = stringList(fields.get("assemblerInputCols"), "assemblerInputCols");
    requireUniqueNames(baseFeatures, "assemblerInputCols");
    int degree = exactInt(fields.get("polynomialDegree"), "polynomialDegree");
    if (baseFeatures.isEmpty()) {
      if (degree != 0) throw new DuckException("classifier polynomial metadata is inconsistent");
    } else {
      try {
        if (PolynomialFeatures.featureCount(baseFeatures.size(), degree, weights.length) != weights.length)
          throw new DuckException("classifier polynomial feature dimensions differ");
      } catch (IllegalArgumentException failure) {
        throw new DuckException("classifier polynomial feature dimensions differ", failure);
      }
    }
    return new LinearClassifier(features, baseFeatures, degree, weights, intercept, logistic, threshold);
  }

  private static void requireUniqueNames(List<String> names, String field) {
    if (names.stream().anyMatch(String::isBlank) || names.stream().distinct().count() != names.size())
      throw new DuckException("classifier " + field + " must contain unique nonblank names");
  }

  private static List<String> stringList(Object value, String field) {
    if (!(value instanceof List<?> list) || list.stream().anyMatch(item -> !(item instanceof String)))
      throw new DuckException("classifier field must be a string array: " + field);
    return list.stream().map(String.class::cast).toList();
  }

  private static double[] numberList(Object value, String field) {
    if (!(value instanceof List<?> list) || list.stream().anyMatch(item -> !(item instanceof BigDecimal)))
      throw new DuckException("classifier field must be a numeric array: " + field);
    double[] numbers = new double[list.size()];
    for (int index = 0; index < numbers.length; index++) numbers[index] = finiteNumber(list.get(index), field);
    return numbers;
  }

  private static double finiteNumber(Object value, String field) {
    if (!(value instanceof BigDecimal number)) throw new DuckException("classifier field must be numeric: " + field);
    double result = number.doubleValue();
    if (!Double.isFinite(result)) throw new DuckException("classifier field must be finite: " + field);
    return result;
  }

  private static int exactInt(Object value, String field) {
    if (!(value instanceof BigDecimal number)) throw new DuckException("classifier field must be numeric: " + field);
    try { return number.intValueExact(); }
    catch (ArithmeticException error) { throw new DuckException("classifier field must be an integer: " + field, error); }
  }

  public Set<String> types() { return Set.copyOf(scorers.keySet()); }
}
