package io.zingg.duckdb.legacy;

import io.zingg.duckdb.model.ImportLimits;
import io.zingg.duckdb.model.ImportProvenance;
import io.zingg.duckdb.model.ModelArtifactJson;
import io.zingg.duckdb.model.ModelArtifactWriter;
import io.zingg.duckdb.model.ModelFormat;
import io.zingg.duckdb.model.ModelManifest;
import io.zingg.duckdb.model.ModelType;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.spark.ml.PipelineModel;
import org.apache.spark.ml.PipelineStage;
import org.apache.spark.ml.classification.LogisticRegressionModel;
import org.apache.spark.ml.feature.PolynomialExpansion;
import org.apache.spark.ml.feature.VectorAssembler;
import org.apache.spark.ml.tuning.CrossValidatorModel;
import org.apache.spark.sql.SparkSession;

/** Isolated Spark 3.5.5 boundary for the explicitly supported classifier pipeline. */
public final class SparkClassifierImporterMain {
  private SparkClassifierImporterMain() {}

  public static void main(String[] args) throws Exception {
    Path input = Path.of(required("ZINGG_IMPORT_INPUT"));
    Path output = Path.of(required("ZINGG_IMPORT_OUTPUT"));
    new ImportLimits(Long.parseLong(required("ZINGG_IMPORT_MAX_BYTES")),
        Integer.parseInt(required("ZINGG_IMPORT_MAX_DEPTH")),
        Long.parseLong(required("ZINGG_IMPORT_MAX_REFERENCES")),
        Long.parseLong(required("ZINGG_IMPORT_TIMEOUT_MILLIS")));
    SparkSession spark = SparkSession.builder().appName("zingg-duckdb-legacy-classifier-import")
        .master("local[1]").config("spark.ui.enabled", "false").getOrCreate();
    try {
      CrossValidatorModel crossValidator = CrossValidatorModel.load(input.toString());
      if (!(crossValidator.bestModel() instanceof PipelineModel pipeline))
        throw new IllegalArgumentException("unsupported classifier model: bestModel is not a PipelineModel");
      importPipeline(pipeline, input, output);
    } finally {
      spark.stop();
    }
  }

  static Path importPipeline(PipelineModel pipeline, Path source, Path output) throws Exception {
    if (pipeline == null || source == null || output == null)
      throw new IllegalArgumentException("pipeline, source and output are required");
    PipelineStage[] stages = pipeline.stages();
    if (stages.length != 3 || !(stages[0] instanceof VectorAssembler assembler)
        || !(stages[1] instanceof PolynomialExpansion polynomial)
        || !(stages[2] instanceof LogisticRegressionModel logistic))
      throw new IllegalArgumentException("unsupported classifier pipeline stage sequence");

    String[] inputColumns = assembler.getInputCols();
    if (inputColumns.length == 0 || Arrays.stream(inputColumns)
        .anyMatch(column -> column == null || column.isBlank())
        || new HashSet<>(Arrays.asList(inputColumns)).size() != inputColumns.length)
      throw new IllegalArgumentException("invalid classifier assembler input columns");
    if (polynomial.getDegree() != 3)
      throw new IllegalArgumentException("unsupported polynomial expansion degree: "
          + polynomial.getDegree());
    String assemblerOutput = assembler.getOutputCol();
    String polynomialInput = polynomial.getInputCol();
    String polynomialOutput = polynomial.getOutputCol();
    String classifierInput = logistic.getFeaturesCol();
    if (assemblerOutput == null || assemblerOutput.isBlank()
        || polynomialInput == null || polynomialInput.isBlank()
        || polynomialOutput == null || polynomialOutput.isBlank()
        || classifierInput == null || classifierInput.isBlank()
        || !Objects.equals(assemblerOutput, polynomialInput)
        || !Objects.equals(polynomialOutput, classifierInput))
      throw new IllegalArgumentException("unsupported classifier feature-column wiring");

    double[] weights = logistic.coefficients().toArray();
    if (logistic.numClasses() != 2 || weights.length != polynomialFeatureCount(inputColumns.length)
        || Arrays.stream(weights).anyMatch(weight -> !Double.isFinite(weight))
        || !Double.isFinite(logistic.intercept()))
      throw new IllegalArgumentException("unsupported or invalid binary classifier shape");
    double threshold = logistic.getThreshold();
    if (!Double.isFinite(threshold) || threshold < 0 || threshold > 1)
      throw new IllegalArgumentException("classifier threshold is outside [0,1]");

    List<String> features = new ArrayList<>(weights.length);
    for (int index = 0; index < weights.length; index++)
      features.add("expanded_feature_" + index);
    String featureJson = features.stream().map(ModelArtifactJson::quote)
        .collect(Collectors.joining(","));
    String weightJson = Arrays.stream(weights).mapToObj(Double::toString)
        .collect(Collectors.joining(","));
    String assemblerJson = Arrays.stream(inputColumns).map(ModelArtifactJson::quote)
        .collect(Collectors.joining(","));
    String payload = "{\"kind\":\"linear-classifier\",\"features\":[" + featureJson
        + "],\"weights\":[" + weightJson + "],\"intercept\":" + logistic.intercept()
        + ",\"logistic\":true,\"assemblerInputCols\":[" + assemblerJson
        + "],\"polynomialDegree\":" + polynomial.getDegree()
        + ",\"threshold\":" + threshold + "}";
    return new ModelArtifactWriter(output).write(
        new ModelManifest("zingg-0.7.0", "0.7.0", ModelFormat.CURRENT,
            ModelType.CLASSIFIER.name(), features, ""),
        payload.getBytes(StandardCharsets.UTF_8),
        new ImportProvenance(source.toString(), "0.7.0", "zingg-duckdb-legacy-spark35-0.1",
            Instant.now(), ""));
  }

  private static int polynomialFeatureCount(int inputs) {
    BigInteger n = BigInteger.valueOf(inputs);
    BigInteger result = n.add(BigInteger.ONE).multiply(n.add(BigInteger.TWO))
        .multiply(n.add(BigInteger.valueOf(3))).divide(BigInteger.valueOf(6))
        .subtract(BigInteger.ONE);
    return result.intValueExact();
  }

  private static String required(String name) {
    String value = System.getenv(name);
    if (value == null || value.isBlank())
      throw new IllegalArgumentException("missing importer environment: " + name);
    return value;
  }
}
