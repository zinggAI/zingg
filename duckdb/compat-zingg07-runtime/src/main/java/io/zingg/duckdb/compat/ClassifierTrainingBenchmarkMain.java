package io.zingg.duckdb.compat;

import io.zingg.duckdb.api.RuntimeConfig;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;

/** Reproducible diagnostic for native classifier training across row counts and feature widths. */
public final class ClassifierTrainingBenchmarkMain {
  private static final String ACTIVE_DUCKDB_VERSION =
      new ProfileRegistry().require(ProfileRegistry.DEFAULT_PROFILE_ID).duckdbVersion();

  private ClassifierTrainingBenchmarkMain() {}

  public static void main(String[] args) throws Exception {
    int repetitions = intArg(args, "--repetitions", 3);
    int iterations = intArg(args, "--iterations", 20);
    long[] rowCounts = longListArg(args, "--rows", new long[] {10_000, 100_000});
    int[] widths = intListArg(args, "--widths", new int[] {2, 8});
    Path output = Path.of(stringArg(args, "--output", ""));
    if (repetitions < 1 || iterations < 1) throw new IllegalArgumentException("repetitions and iterations must be positive");
    if (rowCounts.length == 0 || widths.length == 0) throw new IllegalArgumentException("rows and widths must not be empty");
    for (long rows : rowCounts) if (rows < 2) throw new IllegalArgumentException("each row count must be >= 2");
    for (int width : widths) if (width < 1) throw new IllegalArgumentException("each feature width must be >= 1");

    var cases = new ArrayList<CaseResult>();
    for (long rows : rowCounts) for (int width : widths)
      cases.add(runCase(rows, width, repetitions, iterations));
    String report = json(cases, repetitions, iterations);
    if (output.toString().isBlank() || "-".equals(output.toString())) System.out.println(report);
    else {
      Path target = output.toAbsolutePath().normalize();
      if (Files.exists(target)) throw new java.nio.file.FileAlreadyExistsException(target.toString());
      Files.createDirectories(target.getParent());
      Path staged = Files.createTempFile(target.getParent(), ".classifier-benchmark-", ".partial");
      try {
        Files.writeString(staged, report + System.lineSeparator(), java.nio.charset.StandardCharsets.UTF_8);
        Files.move(staged, target, java.nio.file.StandardCopyOption.ATOMIC_MOVE);
      } finally { Files.deleteIfExists(staged); }
    }
  }

  private static CaseResult runCase(long rows, int width, int repetitions, int iterations) throws Exception {
    long[] elapsed = new long[repetitions];
    long[] rssBefore = new long[repetitions], rssAfter = new long[repetitions];
    long[] spillBefore = new long[repetitions], spillAfter = new long[repetitions];
    long[] artifactBytes = new long[repetitions];
    double[] finalLoss = new double[repetitions];
    double warmupLoss = Double.NaN;
    long[] trainedRows = new long[repetitions];
    String firstGradientPhysicalPlan = "";
    String inputIdentity = "rows=" + rows + ";width=" + width + ";generator=deterministic-v1";
    String configIdentity = "width=" + width + ";iterations=" + iterations + ";learningRate=0.05;l2=0.0001;threads="
        + Runtime.getRuntime().availableProcessors() + ";jdk=" + System.getProperty("java.runtime.version")
        + ";duckdb=" + ACTIVE_DUCKDB_VERSION;
    Path root = Files.createTempDirectory("zingg-classifier-training-benchmark-");
    try {
      for (int run = -1; run < repetitions; run++) {
        Path database = root.resolve("run-" + (run + 1) + ".duckdb");
        Path temp = Files.createDirectory(root.resolve("spill-" + (run + 1)));
        Path artifact = root.resolve("model-" + (run + 1));
        NativeClassifierTrainer.Result trained;
        String gradientPlan = "";
        long beforeRss;
        long beforeSpill;
        long afterRss;
        long afterSpill;
        long elapsedNanos;
        try (var runtime = new CompatibilityRuntime(
                 new RuntimeConfig("jdbc:duckdb:" + database, Runtime.getRuntime().availableProcessors(),
                     0, temp, 1), null);
             var job = new ZinggJob(runtime)) {
          var features = featureNames(width);
          var input = generatedInput(job, rows, features);
          beforeRss = residentBytes();
          beforeSpill = directoryBytes(temp);
          long begin = System.nanoTime();
          var trainingConfig = new NativeClassifierTrainer.Config(
              "benchmark-native-logistic", features, "label", artifact, iterations, 0.05, 1e-4, rows);
          if (run < 0) {
            var diagnostic = job.trainClassifierWithDiagnostics(input, trainingConfig);
            trained = diagnostic.result();
            gradientPlan = diagnostic.firstGradientPhysicalPlan();
            if (gradientPlan.isBlank()) throw new IllegalStateException("warmup did not capture the initial gradient plan");
            firstGradientPhysicalPlan = gradientPlan;
          } else {
            trained = job.trainClassifier(input, trainingConfig);
          }
          elapsedNanos = System.nanoTime() - begin;
          afterRss = residentBytes();
          afterSpill = directoryBytes(temp);
          if (beforeRss <= 0 || afterRss <= 0 || beforeSpill < 0 || afterSpill < 0)
            throw new IllegalStateException("cannot collect Linux process RSS or isolated spill-directory measurements");
          // Independent SQL calculation over the same frozen deterministic input;
          // this is a numerical-stability signal, not a Spark parity oracle.
          double measuredLoss = runLoss(job, rows, width, trained.weights(), trained.intercept());
          if (run < 0) warmupLoss = measuredLoss;
          else finalLoss[run] = measuredLoss;
        }
        if (trained.rows() != rows || !Files.isDirectory(artifact))
          throw new IllegalStateException("training output cardinality or artifact mismatch");
        if (!Double.isFinite(run < 0 ? warmupLoss : finalLoss[run]))
          throw new IllegalStateException("classifier benchmark produced non-finite validation loss");
        if (run >= 0 && finalLoss[run] >= Math.log(2d))
          throw new IllegalStateException("classifier training failed to beat the random-classifier log-loss baseline");
        long artifactSize = directoryBytes(artifact);
        if (artifactSize <= 0) throw new IllegalStateException("classifier artifact is empty or unreadable");
        if (run >= 0) {
          elapsed[run] = elapsedNanos;
          rssBefore[run] = beforeRss;
          rssAfter[run] = afterRss;
          spillBefore[run] = beforeSpill;
          spillAfter[run] = afterSpill;
          artifactBytes[run] = artifactSize;
          trainedRows[run] = trained.rows();
        }
        deleteTree(artifact);
      }
    } finally { deleteTree(root); }
    return new CaseResult(rows, width, inputIdentity, sha256(inputIdentity),
        configIdentity, sha256(configIdentity), elapsed, trainedRows,
        rssBefore, rssAfter, spillBefore, spillAfter, artifactBytes, finalLoss, warmupLoss,
        firstGradientPhysicalPlan);
  }

  private static io.zingg.duckdb.api.Frame generatedInput(ZinggJob job, long rows, List<String> features) {
    StringBuilder query = new StringBuilder("SELECT ");
    for (int i = 0; i < features.size(); i++) {
      if (i > 0) query.append(", ");
      query.append(featureExpression(i)).append(" AS ").append(quote(features.get(i)));
    }
    query.append(", CASE WHEN ((i * 3 + 5) % 1009) < 504 THEN 1 ELSE 0 END AS label FROM range(")
        .append(rows).append(") t(i)");
    return job.sql(query.toString());
  }

  private static double runLoss(ZinggJob job, long rows, int width, double[] weights, double intercept) {
    StringBuilder z = new StringBuilder(Double.toString(intercept));
    for (int i = 0; i < width; i++) {
      z.append(" + (").append(weights[i]).append(" * ").append(featureExpression(i)).append(')');
    }
    String score = z.toString();
    String probability = "CASE WHEN (" + score + ") >= 0 THEN 1.0/(1.0+exp(-(" + score
        + "))) ELSE exp(" + score + ")/(1.0+exp(" + score + ")) END";
    String sql = "SELECT AVG(-label*ln(GREATEST(" + probability + ", 1e-15))"
        + "-(1-label)*ln(GREATEST(1-(" + probability + "), 1e-15))) AS loss FROM (SELECT CASE WHEN ((i * 3 + 5) % 1009) < 504 THEN 1.0 ELSE 0.0 END AS label, i FROM range(" + rows + ") t(i))";
    Object value = job.sql(sql).collect().get(0).get("loss");
    return ((Number) value).doubleValue();
  }

  private static List<String> featureNames(int width) {
    var result = new ArrayList<String>(width);
    for (int i = 0; i < width; i++) result.add("feature_" + i);
    return List.copyOf(result);
  }

  private static String featureExpression(int index) {
    return "CAST(((i * " + (index + 3) + " + " + (index * 17 + 5)
        + ") % 1009) / 1009.0 AS DOUBLE)";
  }

  private static String json(List<CaseResult> cases, int repetitions, int iterations) {
    StringBuilder out = new StringBuilder("{\"benchmark\":\"zingg-duckdb-native-classifier-training\",\"schemaVersion\":2,\"capturedAt\":")
        .append(q(Instant.now().toString())).append(",\"javaVersion\":").append(q(System.getProperty("java.runtime.version")))
        .append(",\"os\":").append(q(System.getProperty("os.name") + " " + System.getProperty("os.version")))
        .append(",\"architecture\":").append(q(System.getProperty("os.arch")))
        .append(",\"sourceRevision\":").append(q(System.getenv().getOrDefault("ZINGG_SOURCE_REVISION", "unrecorded")))
        .append(",\"worktreeState\":").append(q(System.getenv().getOrDefault("ZINGG_WORKTREE_STATE", "unrecorded")))
        .append(",\"containerImage\":").append(q(System.getenv().getOrDefault("ZINGG_CONTAINER_IMAGE", "unrecorded")))
        .append(",\"availableProcessors\":").append(Runtime.getRuntime().availableProcessors())
        .append(",\"maxHeapBytes\":").append(Runtime.getRuntime().maxMemory())
        .append(",\"duckdbVersion\":\"").append(ACTIVE_DUCKDB_VERSION)
        .append("\",\"warmupRuns\":1,\"repetitions\":").append(repetitions)
        .append(",\"iterations\":").append(iterations).append(",\"cases\":[");
    for (int i = 0; i < cases.size(); i++) {
      if (i > 0) out.append(',');
      CaseResult c = cases.get(i);
      out.append("{\"rows\":").append(c.rows()).append(",\"featureWidth\":").append(c.width())
          .append(",\"inputGenerator\":\"deterministic-v1\",\"inputIdentity\":").append(q(c.inputIdentity()))
          .append(",\"inputDigest\":").append(q(c.inputDigest()))
          .append(",\"trainingConfig\":").append(q(c.configIdentity()))
          .append(",\"configurationDigest\":").append(q(c.configDigest()))
          .append(",\"gradientPlanCaptureStage\":\"excluded_warmup_iteration_0\"")
          .append(",\"firstGradientPhysicalPlan\":").append(q(c.firstGradientPhysicalPlan()))
          .append(",\"gradientPlanSha256\":").append(q(sha256(c.firstGradientPhysicalPlan())))
          .append(",\"trainingRows\":").append(array(c.trainedRows()))
          .append(",\"elapsedNs\":").append(array(c.elapsed()))
          .append(",\"elapsedMedianNs\":").append(median(c.elapsed()))
          .append(",\"rowsPerSecondMedian\":").append(Math.round(c.rows() * 1_000_000_000d / median(c.elapsed())))
          .append(",\"warmupLoss\":").append(String.format(Locale.ROOT, "%.12g", c.warmupLoss()))
          .append(",\"randomClassifierBaselineLoss\":").append(String.format(Locale.ROOT, "%.12g", Math.log(2d)))
          .append(",\"lossByMeasuredRun\":").append(array(c.loss()))
          .append(",\"artifactBytes\":").append(array(c.artifactBytes()))
          .append(",\"rssBeforeBytes\":").append(array(c.rssBefore()))
          .append(",\"rssAfterBytes\":").append(array(c.rssAfter()))
          .append(",\"spillBeforeBytes\":").append(array(c.spillBefore()))
          .append(",\"spillAfterBytes\":").append(array(c.spillAfter())).append('}');
    }
    return out.append("]}").toString();
  }

  private static long median(long[] values) { long[] sorted = values.clone(); java.util.Arrays.sort(sorted); return sorted[sorted.length / 2]; }
  private static String array(long[] values) { return java.util.Arrays.toString(values); }
  private static String array(double[] values) {
    StringBuilder out = new StringBuilder("[");
    for (int i = 0; i < values.length; i++) { if (i > 0) out.append(','); out.append(String.format(Locale.ROOT, "%.12g", values[i])); }
    return out.append(']').toString();
  }
  private static String quote(String name) { return "\"" + name.replace("\"", "\"\"") + "\""; }
  private static String q(String value) {
    StringBuilder encoded = new StringBuilder("\"");
    for (int i = 0; i < value.length(); i++) {
      char character = value.charAt(i);
      switch (character) {
        case '\\' -> encoded.append("\\\\");
        case '"' -> encoded.append("\\\"");
        case '\b' -> encoded.append("\\b");
        case '\f' -> encoded.append("\\f");
        case '\n' -> encoded.append("\\n");
        case '\r' -> encoded.append("\\r");
        case '\t' -> encoded.append("\\t");
        default -> {
          if (character < 0x20) encoded.append(String.format(Locale.ROOT, "\\u%04x", (int) character));
          else encoded.append(character);
        }
      }
    }
    return encoded.append('"').toString();
  }
  private static String sha256(String value) {
    try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(java.nio.charset.StandardCharsets.UTF_8))); }
    catch (java.security.NoSuchAlgorithmException e) { throw new IllegalStateException("SHA-256 is unavailable", e); }
  }
  private static long residentBytes() {
    try {
      for (String line : Files.readAllLines(Path.of("/proc/self/status"))) if (line.startsWith("VmRSS:"))
        return Math.multiplyExact(Long.parseLong(line.trim().split("\\s+")[1]), 1024L);
    } catch (Exception ignored) { }
    return -1;
  }
  private static long directoryBytes(Path directory) {
    long total = 0;
    try (var paths = Files.walk(directory)) {
      for (Path path : paths.filter(Files::isRegularFile).toList()) total = Math.addExact(total, Files.size(path));
      return total;
    } catch (Exception ignored) { return -1; }
  }
  private static int intArg(String[] args, String name, int fallback) { return Integer.parseInt(stringArg(args, name, Integer.toString(fallback))); }
  private static String stringArg(String[] args, String name, String fallback) {
    for (int i = 0; i + 1 < args.length; i++) if (name.equals(args[i])) return args[i + 1];
    return fallback;
  }
  private static long[] longListArg(String[] args, String name, long[] fallback) {
    String value = stringArg(args, name, "");
    return value.isBlank() ? fallback : java.util.Arrays.stream(value.split(",")).mapToLong(Long::parseLong).toArray();
  }
  private static int[] intListArg(String[] args, String name, int[] fallback) {
    String value = stringArg(args, name, "");
    return value.isBlank() ? fallback : java.util.Arrays.stream(value.split(",")).mapToInt(Integer::parseInt).toArray();
  }
  private static void deleteTree(Path root) throws Exception {
    if (!Files.exists(root)) return;
    try (var paths = Files.walk(root)) {
      for (Path path : paths.sorted(java.util.Comparator.reverseOrder()).toList()) Files.deleteIfExists(path);
    }
  }

  private record CaseResult(long rows, int width, String inputIdentity, String inputDigest,
      String configIdentity, String configDigest, long[] elapsed, long[] trainedRows,
      long[] rssBefore, long[] rssAfter, long[] spillBefore, long[] spillAfter,
      long[] artifactBytes, double[] loss, double warmupLoss, String firstGradientPhysicalPlan) {}
}
