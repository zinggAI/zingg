package io.zingg.duckdb.compat;

import io.zingg.duckdb.api.DuckException;
import io.zingg.duckdb.model.ModelArtifactJson;
import java.nio.charset.StandardCharsets;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.PosixFilePermissions;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;
import java.util.Map;

/** Imports the pinned Spark ML VectorAssembler/PolynomialExpansion/LogisticRegression fixture. */
public final class SparkMlClassifierImporter {
  private static final long MAX_METADATA_BYTES = 1L * 1024 * 1024;
  private static final long MAX_PARQUET_BYTES = 512L * 1024 * 1024;
  private static final String SPARK_VERSION = "3.0.1";

  public Path importModel(Path modelRoot, String profile, Path output) {
    try {
      Path root = modelRoot.toAbsolutePath().normalize();
      if (Files.isSymbolicLink(root) || !Files.isDirectory(root, LinkOption.NOFOLLOW_LINKS))
        throw new DuckException("invalid Spark classifier root");
      Path best = root.resolve("classifier/best.model/bestModel");
      requireDirectory(modelRoot, best);
      String pipeline = readMetadata(best);
      requireStageContract(pipeline, "org.apache.spark.ml.PipelineModel", "stageUids");
      if (!SPARK_VERSION.equals(stringField(pipeline, "sparkVersion")))
        throw new DuckException("unsupported Spark classifier version");
      List<String> stageUids = stringArrayField(pipeline, "stageUids");
      if (stageUids.size() != 3)
        throw new DuckException("unsupported Spark classifier stage sequence");
      Path stages = best.resolve("stages");
      requireDirectory(modelRoot, stages);
      Path assemblerPath = findStage(stages, "org.apache.spark.ml.feature.VectorAssembler");
      Path polynomialPath = findStage(stages, "org.apache.spark.ml.feature.PolynomialExpansion");
      Path logisticPath = findStage(stages, "org.apache.spark.ml.classification.LogisticRegressionModel");
      String assembler = readMetadata(assemblerPath);
      String polynomial = readMetadata(polynomialPath);
      String logistic = readMetadata(logisticPath);
      List<String> inputCols = stringArrayField(assembler, "inputCols");
      int degree = integerField(polynomial, "degree");
      double threshold = decimalField(logistic, "threshold");
      requireStageContract(assembler, "org.apache.spark.ml.feature.VectorAssembler", "inputCols");
      requireStageContract(polynomial, "org.apache.spark.ml.feature.PolynomialExpansion", "degree");
      requireStageContract(logistic, "org.apache.spark.ml.classification.LogisticRegressionModel", "threshold");
      List<String> discoveredStageUids = List.of(stringField(assembler, "uid"),
          stringField(polynomial, "uid"), stringField(logistic, "uid"));
      if (!stageUids.equals(discoveredStageUids))
        throw new DuckException("unsupported Spark classifier stage sequence");
      if (!SPARK_VERSION.equals(stringField(assembler, "sparkVersion"))
          || !SPARK_VERSION.equals(stringField(polynomial, "sparkVersion"))
          || !SPARK_VERSION.equals(stringField(logistic, "sparkVersion")))
        throw new DuckException("unsupported Spark classifier version");
      if (inputCols.size() != 18 || degree != 3) throw new DuckException("unsupported Spark classifier metadata");
      if (threshold <= 0d || threshold >= 1d)
        throw new DuckException("unsupported Spark classifier threshold: expected a value strictly between 0 and 1");
      if (inputCols.stream().anyMatch(String::isBlank) || inputCols.stream().distinct().count() != inputCols.size())
        throw new DuckException("invalid Spark classifier inputCols");
      if (!stringParamField(assembler, "outputCol").equals(stringParamField(polynomial, "inputCol"))
          || !stringParamField(polynomial, "outputCol").equals(stringParamField(logistic, "featuresCol")))
        throw new DuckException("unsupported Spark classifier feature-column wiring");
      try (var parquet = snapshotParquet(findParquet(logisticPath.resolve("data")))) {
        try (var connection = DriverManager.getConnection("jdbc:duckdb:"); var statement = connection.prepareStatement("select numClasses, numFeatures, interceptVector, coefficientMatrix, isMultinomial from read_parquet(?)")) {
          statement.setString(1, parquet.path().toString());
          try (var rows = statement.executeQuery()) {
            if (!rows.next() || rows.getInt(1) != 2 || rows.getInt(2) != 1329 || rows.getBoolean(5)) throw new DuckException("unsupported Spark classifier shape");
            List<Double> interceptValues = numbersFromValues(rows.getObject(3).toString());
            if (interceptValues.size() != 1) throw new DuckException("Spark classifier intercept dimension mismatch");
            double intercept = interceptValues.get(0);
            double[] weights = numbersFromValues(rows.getObject(4).toString()).stream().mapToDouble(Double::doubleValue).toArray();
            if (weights.length != 1329) throw new DuckException("Spark classifier coefficient dimension mismatch");
            if (rows.next()) throw new DuckException("Spark classifier Parquet must contain exactly one row");
            return new NativeClassifierArtifact().write(new NativeClassifierArtifact.Config(profile, PolynomialFeatures.sqlTerms(inputCols, degree), weights, intercept, true, output, threshold, inputCols, degree));
          }
        }
      }
    } catch (DuckException e) { throw e; } catch (Exception e) { throw new DuckException("Spark classifier import failed", e); }
  }

  private static void requireDirectory(Path root, Path path) throws Exception {
    Path normalizedRoot = root.toAbsolutePath().normalize();
    Path normalized = path.toAbsolutePath().normalize();
    if (!normalized.startsWith(normalizedRoot))
      throw new DuckException("invalid Spark classifier directory: " + path.getFileName());
    Path current = normalizedRoot;
    if (Files.isSymbolicLink(current) || !Files.isDirectory(current, LinkOption.NOFOLLOW_LINKS))
      throw new DuckException("invalid Spark classifier directory: " + current.getFileName());
    for (Path part : normalizedRoot.relativize(normalized)) {
      current = current.resolve(part);
      if (Files.isSymbolicLink(current) || !Files.isDirectory(current, LinkOption.NOFOLLOW_LINKS))
        throw new DuckException("invalid Spark classifier directory: " + current.getFileName());
    }
  }

  private static Path findStage(Path stages, String className) throws Exception {
    Path match = null;
    try (var entries = Files.list(stages)) {
      for (Path stage : entries.toList()) {
        if (Files.isSymbolicLink(stage) || !Files.isDirectory(stage, LinkOption.NOFOLLOW_LINKS)) continue;
        Path metadata = stage.resolve("metadata/part-00000");
        if (Files.isSymbolicLink(metadata) || !Files.isRegularFile(metadata, LinkOption.NOFOLLOW_LINKS)) continue;
        String json = readBounded(metadata, MAX_METADATA_BYTES);
        if (!json.contains("\"class\":\"" + className + "\"")) continue;
        if (match != null) throw new DuckException("ambiguous Spark classifier stage: " + className);
        match = stage;
      }
    }
    if (match == null) throw new DuckException("Spark classifier stage missing: " + className);
    return match;
  }

  private static String readMetadata(Path stage) throws Exception {
    Path metadata = stage.resolve("metadata/part-00000");
    if (Files.isSymbolicLink(metadata) || !Files.isRegularFile(metadata, LinkOption.NOFOLLOW_LINKS))
      throw new DuckException("Spark classifier metadata missing: " + stage.getFileName());
    return readBounded(metadata, MAX_METADATA_BYTES);
  }

  private static String readBounded(Path file, long maxBytes) throws Exception {
    byte[] bytes;
    try (var channel = FileChannel.open(file, StandardOpenOption.READ, LinkOption.NOFOLLOW_LINKS)) {
      long size = channel.size();
      if (size < 1 || size > maxBytes) throw new DuckException("Spark classifier metadata size is invalid");
      var content = ByteBuffer.allocate(Math.toIntExact(size));
      while (content.hasRemaining()) {
        int read = channel.read(content);
        if (read < 0) throw new DuckException("Spark classifier metadata size changed during read");
      }
      if (channel.size() != size) throw new DuckException("Spark classifier metadata size changed during read");
      bytes = content.array();
    }
    try {
      return StandardCharsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT)
          .onUnmappableCharacter(CodingErrorAction.REPORT).decode(ByteBuffer.wrap(bytes)).toString();
    } catch (CharacterCodingException e) {
      throw new DuckException("Spark classifier metadata is not valid UTF-8", e);
    }
  }

  private static Path findParquet(Path data) throws Exception {
    if (Files.isSymbolicLink(data) || !Files.isDirectory(data, LinkOption.NOFOLLOW_LINKS))
      throw new DuckException("Spark classifier data directory missing");
    Path match = null;
    try (var entries = Files.list(data)) {
      for (Path entry : entries.toList()) {
        if (Files.isSymbolicLink(entry) || !Files.isRegularFile(entry, LinkOption.NOFOLLOW_LINKS)
            || !entry.getFileName().toString().endsWith(".parquet")) continue;
        if (match != null) throw new DuckException("ambiguous Spark classifier data files");
        match = entry;
      }
    }
    if (match == null) throw new DuckException("Spark classifier Parquet data missing");
    return match;
  }

  static TemporaryParquet snapshotParquet(Path source) throws Exception {
    Path temporary = null;
    try (var input = FileChannel.open(source, StandardOpenOption.READ, LinkOption.NOFOLLOW_LINKS)) {
      long size = input.size();
      if (size < 1 || size > MAX_PARQUET_BYTES)
        throw new DuckException("Spark classifier Parquet size is invalid");
      try {
        temporary = Files.createTempFile("zingg-classifier-", ".parquet",
            PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rw-------")));
      } catch (UnsupportedOperationException unsupported) {
        temporary = Files.createTempFile("zingg-classifier-", ".parquet");
      }
      try (var output = FileChannel.open(temporary,
          java.util.Set.<OpenOption>of(StandardOpenOption.WRITE, LinkOption.NOFOLLOW_LINKS))) {
        var buffer = ByteBuffer.allocate(64 * 1024);
        long copied = 0;
        while (copied < size) {
          buffer.clear();
          buffer.limit((int) Math.min(buffer.capacity(), size - copied));
          int read = input.read(buffer);
          if (read < 0) throw new DuckException("Spark classifier Parquet changed while being copied");
          if (read == 0) continue;
          copied += read;
          buffer.flip();
          while (buffer.hasRemaining()) output.write(buffer);
        }
        if (input.size() != size) throw new DuckException("Spark classifier Parquet changed while being copied");
        output.force(true);
      }
      return new TemporaryParquet(temporary);
    } catch (Exception failure) {
      if (temporary != null) try { Files.deleteIfExists(temporary); } catch (Exception cleanup) { failure.addSuppressed(cleanup); }
      throw failure;
    }
  }

  record TemporaryParquet(Path path) implements AutoCloseable {
    @Override public void close() throws java.io.IOException { Files.deleteIfExists(path); }
  }

  private static Map<String,Object> metadata(String json) {
    Map<String,Object> top = ModelArtifactJson.parseObject(json);
    Object params = top.get("paramMap");
    if (!(params instanceof Map<?,?> map)) throw new DuckException("Spark metadata field missing: paramMap");
    @SuppressWarnings("unchecked") Map<String,Object> values = (Map<String,Object>) map;
    return values;
  }
  private static void requireStageContract(String json, String expectedClass, String requiredParam) {
    Map<String,Object> top = ModelArtifactJson.parseObject(json);
    if (!expectedClass.equals(top.get("class"))) throw new DuckException("unsupported Spark classifier stage class");
    Object params = top.get("paramMap");
    if (!(params instanceof Map<?,?> map) || !map.containsKey(requiredParam))
      throw new DuckException("Spark metadata field missing: " + requiredParam);
  }
  private static Object field(String json, String name) {
    Object value = metadata(json).get(name);
    if (value == null) throw new DuckException("Spark metadata field missing: " + name);
    return value;
  }
  private static String stringField(String json, String name) {
    Object value = ModelArtifactJson.parseObject(json).get(name);
    if (!(value instanceof String string)) throw new DuckException("Spark metadata field must be a string: " + name);
    return string;
  }
  private static String stringParamField(String json, String name) {
    Object value = field(json, name);
    if (!(value instanceof String string))
      throw new DuckException("Spark metadata field must be a string: " + name);
    return string;
  }
  private static List<String> stringArrayField(String json, String name) {
    Object value = field(json, name);
    if (!(value instanceof List<?> list) || list.stream().anyMatch(item -> !(item instanceof String)))
      throw new DuckException("Spark metadata field must be a string array: " + name);
    return list.stream().map(String.class::cast).toList();
  }
  private static int integerField(String json, String name) {
    Object value = field(json, name);
    if (!(value instanceof BigDecimal decimal)) throw new DuckException("Spark metadata field must be a number: " + name);
    try { return decimal.intValueExact(); }
    catch (ArithmeticException e) { throw new DuckException("Spark metadata field must be an exact integer: " + name, e); }
  }
  private static double decimalField(String json, String name) {
    Object value = field(json, name);
    if (!(value instanceof BigDecimal decimal)) throw new DuckException("Spark metadata field must be a number: " + name);
    double result = decimal.doubleValue();
    if (!Double.isFinite(result)) throw new DuckException("Spark metadata field must be finite: " + name);
    return result;
  }
  static List<Double> numbersFromValues(String text) {
    int marker = text.indexOf("values=[");
    int close = marker < 0 ? -1 : text.indexOf(']', marker + "values=[".length());
    if (!text.startsWith("{type=1,") || text.contains("indices=[")
        || marker < 0 || close < 0 || !text.trim().endsWith("}")
        || text.substring(close + 1).contains("["))
      throw new DuckException("Spark vector values missing or malformed");
    String body = text.substring(marker + "values=[".length(), close).trim();
    if (body.isEmpty()) return List.of();
    String[] tokens = body.split(",", -1);
    List<Double> result = new ArrayList<>(tokens.length);
    for (String token : tokens) {
      try {
        double value = Double.parseDouble(token.trim());
        if (!Double.isFinite(value)) throw new NumberFormatException("non-finite");
        result.add(value);
      } catch (NumberFormatException e) {
        throw new DuckException("Spark classifier vector contains an invalid number", e);
      }
    }
    return result;
  }
}
