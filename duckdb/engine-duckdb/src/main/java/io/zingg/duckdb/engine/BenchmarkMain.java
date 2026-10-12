package io.zingg.duckdb.engine;

import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.List;

/**
 * Small dependency-free benchmark runner for repeatable smoke and capacity runs.
 * It reports raw samples and descriptive timing summaries in JSON and is intentionally separate from correctness tests.
 */
public final class BenchmarkMain {
  private BenchmarkMain() {}

  public static void main(String[] args) throws Exception {
    int size = integerArg(args, "--size", 1000);
    int repetitions = integerArg(args, "--repetitions", 5);
    if (size < 2 || repetitions < 1) throw new IllegalArgumentException("size >= 2 and repetitions >= 1 are required");
    boolean sampleResources = java.util.Arrays.asList(args).contains("--resource-sampling");
    if (java.util.Arrays.asList(args).contains("--graph-compare")) {
      graphCompare(size, repetitions, sampleResources, stringArg(args, "--shape", "path"));
      return;
    }
    if (java.util.Arrays.asList(args).contains("--arrow-only")) {
      arrowOnly(size, repetitions, sampleResources);
      return;
    }
    long[] startup = new long[repetitions], sql = new long[repetitions], graph = new long[repetitions];
    long[] arrowWrite = new long[repetitions], arrowRead = new long[repetitions];
    int edgeCount = size - 1;
    for (int run = -1; run < repetitions; run++) { // exclude one cold pass from measured samples
      long startupNanos, sqlNanos, graphNanos, arrowWriteNanos, arrowReadNanos;
      long begin = System.nanoTime();
      try (var runtime = new DuckRuntime("jdbc:duckdb:")) { startupNanos = System.nanoTime() - begin; }
      try (var connection = DriverManager.getConnection("jdbc:duckdb:")) {
        begin = System.nanoTime();
        try (var statement = connection.createStatement(); var rows = statement.executeQuery(
            "select sum(i), avg(i) from range(" + size + ") t(i)")) { rows.next(); }
      sqlNanos = System.nanoTime() - begin;
      }
      var arrow = java.nio.file.Files.createTempFile("zingg-duckdb-benchmark-", ".arrow");
      try (var runtime = new DuckRuntime("jdbc:duckdb:")) {
        try (var job = (DuckJob) runtime.openJob()) {
          begin = System.nanoTime();
          job.sql("select i, 'benchmark' as label from range(" + size + ") t(i)").writeArrow(arrow);
          arrowWriteNanos = System.nanoTime() - begin;
          begin = System.nanoTime();
          if (new DuckPipeReader(job).read(List.of(arrow), DuckPipeReader.UnionMode.MATCH_BY_NAME).count() != size)
            throw new IllegalStateException("Arrow benchmark cardinality mismatch");
          arrowReadNanos = System.nanoTime() - begin;
        }
      } finally { java.nio.file.Files.deleteIfExists(arrow); }
      List<GraphOutput.Edge> edges = new ArrayList<>(edgeCount);
      for (int node = 0; node < edgeCount; node++) edges.add(new GraphOutput.Edge(node, node + 1, 1d));
      begin = System.nanoTime();
      int produced = GraphOutput.transitiveEdges(edges).size();
      graphNanos = System.nanoTime() - begin;
      if (produced != size * (size - 1) / 2) throw new IllegalStateException("graph result cardinality mismatch");
      if (run >= 0) {
        startup[run] = startupNanos;
        sql[run] = sqlNanos;
        graph[run] = graphNanos;
        arrowWrite[run] = arrowWriteNanos;
        arrowRead[run] = arrowReadNanos;
      }
    }
    System.out.println("{\"benchmark\":\"zingg-duckdb-smoke\",\"size\":" + size
        + ",\"repetitions\":" + repetitions + ",\"warmupRuns\":1,\"graphInputEdges\":" + edgeCount
        + ",\"startupMedianNs\":" + median(startup) + ",\"sqlMedianNs\":" + median(sql)
        + ",\"graphMedianNs\":" + median(graph) + ",\"arrowWriteMedianNs\":" + median(arrowWrite)
        + ",\"arrowReadMedianNs\":" + median(arrowRead)
        + ",\"startupSamplesNs\":" + samples(startup) + ",\"sqlSamplesNs\":" + samples(sql)
        + ",\"graphSamplesNs\":" + samples(graph) + ",\"arrowWriteSamplesNs\":" + samples(arrowWrite)
        + ",\"arrowReadSamplesNs\":" + samples(arrowRead)
        + ",\"startupStats\":" + stats(startup) + ",\"sqlStats\":" + stats(sql)
        + ",\"graphStats\":" + stats(graph) + ",\"arrowWriteStats\":" + stats(arrowWrite)
        + ",\"arrowReadStats\":" + stats(arrowRead) + "}");
  }

  /** Compares sparse entity aggregation with explicitly materialized component-pair closure. */
  private static void graphCompare(int size, int repetitions, boolean sampleResources, String shape) throws Exception {
    long pairCount = expectedClosurePairs(size, shape);
    if (pairCount > GraphOutput.DEFAULT_MAX_TRANSITIVE_PAIRS)
      throw new IllegalArgumentException("graph comparison size exceeds the default transitive pair budget: " + pairCount);
    List<GraphOutput.Edge> edges = graphEdges(size, shape);
    long[] entityNanos = new long[repetitions], closureNanos = new long[repetitions];
    long[] entityRows = new long[repetitions], closureRows = new long[repetitions];
    long[] entityOutputBytes = new long[repetitions], closureOutputBytes = new long[repetitions];
    long[] entitySerializationNanos = new long[repetitions], closureSerializationNanos = new long[repetitions];
    long[] entityPeakRss = new long[repetitions], closurePeakRss = new long[repetitions];
    long[] entityRssSamples = new long[repetitions], closureRssSamples = new long[repetitions];
    var scratch = java.nio.file.Files.createTempDirectory("zingg-duckdb-graph-benchmark-");
    try {
      for (int run = -1; run < repetitions; run++) {
        boolean entityFirst = (run & 1) == 0;
        GraphMeasurement entity;
        GraphMeasurement closure;
        if (entityFirst) {
          entity = measureEntities(edges, size, scratch, sampleResources);
          closure = measureClosure(edges, pairCount, scratch, sampleResources);
        } else {
          closure = measureClosure(edges, pairCount, scratch, sampleResources);
          entity = measureEntities(edges, size, scratch, sampleResources);
        }
        if (entity.rows() != size || closure.rows() != pairCount)
          throw new IllegalStateException("graph benchmark output cardinality mismatch");
        if (run >= 0) {
          entityNanos[run] = entity.elapsedNanos(); closureNanos[run] = closure.elapsedNanos();
          entityRows[run] = entity.rows(); closureRows[run] = closure.rows();
          entityOutputBytes[run] = entity.outputBytes(); closureOutputBytes[run] = closure.outputBytes();
          entitySerializationNanos[run] = entity.serializationNanos();
          closureSerializationNanos[run] = closure.serializationNanos();
          entityPeakRss[run] = entity.maxObservedRssBytes(); closurePeakRss[run] = closure.maxObservedRssBytes();
          entityRssSamples[run] = entity.resourceSamples(); closureRssSamples[run] = closure.resourceSamples();
        }
      }
    } finally {
      try (var paths = java.nio.file.Files.walk(scratch)) {
        for (var path : paths.sorted(java.util.Comparator.reverseOrder()).toList())
          java.nio.file.Files.deleteIfExists(path);
      }
    }
    System.out.println("{\"benchmark\":\"zingg-duckdb-graph-compare\",\"shape\":\"" + shape + "\",\"vertices\":"
        + size + ",\"inputEdges\":" + edges.size() + ",\"closurePairs\":" + pairCount
        + ",\"repetitions\":" + repetitions + ",\"warmupRuns\":1,\"resourceSamplingEnabled\":"
        + sampleResources + ",\"resourceSamplingIntervalMs\":" + (sampleResources ? 50 : 0)
        + ",\"entityScoresMedianNs\":" + median(entityNanos) + ",\"entityScoresSamplesNs\":" + samples(entityNanos)
        + ",\"entityScoresStats\":" + stats(entityNanos)
        + ",\"entityScoresRows\":" + samples(entityRows) + ",\"entityScoresOutputBytes\":" + samples(entityOutputBytes)
        + ",\"entityScoresSerializationSamplesNs\":" + samples(entitySerializationNanos)
        + ",\"entityScoresMaxObservedRssBytes\":" + samples(entityPeakRss)
        + ",\"entityScoresResourceSampleCounts\":" + samples(entityRssSamples)
        + ",\"transitiveEdgesMedianNs\":" + median(closureNanos)
        + ",\"transitiveEdgesSamplesNs\":" + samples(closureNanos)
        + ",\"transitiveEdgesStats\":" + stats(closureNanos)
        + ",\"transitiveEdgesRows\":" + samples(closureRows)
        + ",\"transitiveEdgesOutputBytes\":" + samples(closureOutputBytes)
        + ",\"transitiveEdgesSerializationSamplesNs\":" + samples(closureSerializationNanos)
        + ",\"transitiveEdgesMaxObservedRssBytes\":" + samples(closurePeakRss)
        + ",\"transitiveEdgesResourceSampleCounts\":" + samples(closureRssSamples) + "}");
  }

  private static List<GraphOutput.Edge> graphEdges(int size, String shape) {
    var edges = new ArrayList<GraphOutput.Edge>();
    switch (shape) {
      case "path" -> {
        for (int node = 0; node < size - 1; node++) edges.add(new GraphOutput.Edge(node, node + 1, 1d));
      }
      case "star" -> {
        for (int node = 1; node < size; node++) edges.add(new GraphOutput.Edge(0, node, 1d));
      }
      case "dense" -> {
        for (int left = 0; left < size; left++)
          for (int right = left + 1; right < size; right++)
            edges.add(new GraphOutput.Edge(left, right, 1d));
      }
      case "disconnected" -> {
        for (int node = 0; node + 1 < size; node += 2)
          edges.add(new GraphOutput.Edge(node, node + 1, 1d));
        if ((size & 1) != 0) edges.add(new GraphOutput.Edge(size - 1, 0, 1d));
      }
      default -> throw new IllegalArgumentException("unsupported graph shape: " + shape
          + " (expected path, star, dense, or disconnected)");
    }
    return List.copyOf(edges);
  }

  private static long expectedClosurePairs(int size, String shape) {
    return switch (shape) {
      case "path", "star", "dense" -> Math.multiplyExact((long) size, size - 1L) / 2L;
      case "disconnected" -> size % 2 == 0 ? size / 2L : 3L + (size - 3L) / 2L;
      default -> throw new IllegalArgumentException("unsupported graph shape: " + shape);
    };
  }

  private static GraphMeasurement measureEntities(List<GraphOutput.Edge> edges, int expectedRows,
      java.nio.file.Path scratch, boolean sampleResources) throws Exception {
    var output = scratch.resolve("entity-scores.csv");
    long baselineRss = ProcessResourceSnapshot.residentBytes();
    BenchmarkResourceMonitor monitor = sampleResources ? new BenchmarkResourceMonitor(scratch, 50) : null;
    List<GraphOutput.EntityScore> result;
    long begin = System.nanoTime();
    long elapsed;
    try {
      result = GraphOutput.entityScores(edges, () -> 1_700_000_000_000L);
      elapsed = System.nanoTime() - begin;
    }
    finally { if (monitor != null) monitor.close(); }
    if (result.size() != expectedRows) throw new IllegalStateException("entityScores cardinality mismatch");
    long serializationBegin = System.nanoTime();
    try (var writer = java.nio.file.Files.newBufferedWriter(output, java.nio.charset.StandardCharsets.UTF_8)) {
      writer.write("id,min,max,cluster\n");
      for (var row : result) writer.write(row.id() + "," + row.min() + "," + row.max() + "," + row.cluster() + "\n");
    }
    long serializationNanos = System.nanoTime() - serializationBegin;
    long peakRss = monitor == null ? -1 : Math.max(monitor.maxRssBytes(), baselineRss);
    int samples = monitor == null ? 0 : monitor.samples();
    long bytes = java.nio.file.Files.size(output);
    java.nio.file.Files.delete(output);
    return new GraphMeasurement(elapsed, result.size(), bytes, serializationNanos, peakRss, samples);
  }

  private static GraphMeasurement measureClosure(List<GraphOutput.Edge> edges, long expectedRows,
      java.nio.file.Path scratch, boolean sampleResources) throws Exception {
    var output = scratch.resolve("transitive-edges.csv");
    long baselineRss = ProcessResourceSnapshot.residentBytes();
    BenchmarkResourceMonitor monitor = sampleResources ? new BenchmarkResourceMonitor(scratch, 50) : null;
    List<GraphOutput.Edge> result;
    long begin = System.nanoTime();
    long elapsed;
    try {
      result = GraphOutput.transitiveEdges(edges);
      elapsed = System.nanoTime() - begin;
    }
    finally { if (monitor != null) monitor.close(); }
    if (result.size() != expectedRows) throw new IllegalStateException("transitiveEdges cardinality mismatch");
    long serializationBegin = System.nanoTime();
    try (var writer = java.nio.file.Files.newBufferedWriter(output, java.nio.charset.StandardCharsets.UTF_8)) {
      writer.write("left,right,score\n");
      for (var row : result) writer.write(row.left() + "," + row.right() + "," + row.score() + "\n");
    }
    long serializationNanos = System.nanoTime() - serializationBegin;
    long peakRss = monitor == null ? -1 : Math.max(monitor.maxRssBytes(), baselineRss);
    int samples = monitor == null ? 0 : monitor.samples();
    long bytes = java.nio.file.Files.size(output);
    java.nio.file.Files.delete(output);
    return new GraphMeasurement(elapsed, result.size(), bytes, serializationNanos, peakRss, samples);
  }

  private record GraphMeasurement(long elapsedNanos, long rows, long outputBytes, long serializationNanos,
                                  long maxObservedRssBytes, long resourceSamples) {}

  private static void arrowOnly(int size, int repetitions, boolean sampleResources) throws Exception {
    long[] write = new long[repetitions], ingest = new long[repetitions], appenderIngest = new long[repetitions];
    long[] jdbcPeakRss = new long[repetitions], jdbcPeakSpill = new long[repetitions], jdbcResourceSamples = new long[repetitions];
    long[] appenderPeakRss = new long[repetitions], appenderPeakSpill = new long[repetitions], appenderResourceSamples = new long[repetitions];
    long fileBytes = 0;
    var benchmarkRoot = java.nio.file.Files.createTempDirectory("zingg-duckdb-arrow-benchmark-");
    var spillDirectory = java.nio.file.Files.createDirectory(benchmarkRoot.resolve("spill"));
    long rssBefore = ProcessResourceSnapshot.residentBytes();
    long spillBefore = ProcessResourceSnapshot.directoryBytes(spillDirectory.toString());
    long rssAfter;
    long spillAfter;
    try {
      for (int run = -1; run < repetitions; run++) { // first pass warms the JVM and DuckDB
        var arrow = java.nio.file.Files.createTempFile(benchmarkRoot, "input-", ".arrow");
        try {
          long writeNanos;
          try (var runtime = benchmarkRuntime(spillDirectory); var job = (DuckJob) runtime.openJob()) {
            long begin = System.nanoTime();
            job.sql("select i, 'benchmark-payload' as label from range(" + size + ") t(i)").writeArrow(arrow);
            writeNanos = System.nanoTime() - begin;
          }
          long bytes = java.nio.file.Files.size(arrow);
          MeasuredImport jdbcImport;
          MeasuredImport appenderImport;
          if ((run & 1) == 0) {
            jdbcImport = measureImport(arrow, size, spillDirectory, false, sampleResources);
            appenderImport = measureImport(arrow, size, spillDirectory, true, sampleResources);
          } else {
            appenderImport = measureImport(arrow, size, spillDirectory, true, sampleResources);
            jdbcImport = measureImport(arrow, size, spillDirectory, false, sampleResources);
          }
          if (run >= 0) {
            write[run] = writeNanos;
            ingest[run] = jdbcImport.elapsedNanos();
            appenderIngest[run] = appenderImport.elapsedNanos();
            jdbcPeakRss[run] = jdbcImport.maxObservedRssBytes();
            jdbcPeakSpill[run] = jdbcImport.maxObservedSpillBytes();
            jdbcResourceSamples[run] = jdbcImport.sampleCount();
            appenderPeakRss[run] = appenderImport.maxObservedRssBytes();
            appenderPeakSpill[run] = appenderImport.maxObservedSpillBytes();
            appenderResourceSamples[run] = appenderImport.sampleCount();
            fileBytes = bytes;
          }
        } finally { java.nio.file.Files.deleteIfExists(arrow); }
      }
      rssAfter = ProcessResourceSnapshot.residentBytes();
      spillAfter = ProcessResourceSnapshot.directoryBytes(spillDirectory.toString());
    } finally {
      try (var paths = java.nio.file.Files.walk(benchmarkRoot)) {
        for (var path : paths.sorted(java.util.Comparator.reverseOrder()).toList())
          java.nio.file.Files.deleteIfExists(path);
      }
    }
    long medianIngest = median(ingest);
    long medianAppender = median(appenderIngest);
    long medianWrite = median(write);
    long rowsPerSecond = Math.round(size * 1_000_000_000d / medianIngest);
    long bytesPerSecond = Math.round(fileBytes * 1_000_000_000d / medianIngest);
    long appenderRowsPerSecond = Math.round(size * 1_000_000_000d / medianAppender);
    System.out.println("{\"benchmark\":\"zingg-duckdb-arrow-ingress\",\"size\":" + size
        + ",\"repetitions\":" + repetitions + ",\"warmupRuns\":1,\"fileBytes\":" + fileBytes
        + ",\"writeMedianNs\":" + medianWrite + ",\"ingestMedianNs\":" + medianIngest
        + ",\"ingestRowsPerSecond\":" + rowsPerSecond + ",\"ingestBytesPerSecond\":" + bytesPerSecond
        + ",\"appenderMedianNs\":" + medianAppender + ",\"appenderRowsPerSecond\":" + appenderRowsPerSecond
        + ",\"appenderSpeedup\":" + String.format(java.util.Locale.ROOT, "%.3f", medianIngest / (double) medianAppender)
        + ",\"writeSamplesNs\":" + samples(write) + ",\"jdbcIngestSamplesNs\":" + samples(ingest)
        + ",\"appenderIngestSamplesNs\":" + samples(appenderIngest)
        + ",\"writeStats\":" + stats(write) + ",\"jdbcIngestStats\":" + stats(ingest)
        + ",\"appenderIngestStats\":" + stats(appenderIngest)
        + ",\"resourceSamplingEnabled\":" + sampleResources
        + ",\"resourceSamplingIntervalMs\":" + (sampleResources ? 50 : 0)
        + ",\"jdbcMaxObservedRssBytes\":" + samples(jdbcPeakRss)
        + ",\"jdbcMaxObservedSpillBytes\":" + samples(jdbcPeakSpill)
        + ",\"jdbcResourceSampleCounts\":" + samples(jdbcResourceSamples)
        + ",\"appenderMaxObservedRssBytes\":" + samples(appenderPeakRss)
        + ",\"appenderMaxObservedSpillBytes\":" + samples(appenderPeakSpill)
        + ",\"appenderResourceSampleCounts\":" + samples(appenderResourceSamples)
        + ",\"processRssBeforeBenchmarkBytes\":" + rssBefore
        + ",\"processRssAfterBenchmarkBytes\":" + rssAfter
        + ",\"spillDirectoryBytesBefore\":" + spillBefore
        + ",\"spillDirectoryBytesAfter\":" + spillAfter + "}");
  }

  private static DuckRuntime benchmarkRuntime(java.nio.file.Path spillDirectory) {
    var config = new io.zingg.duckdb.api.RuntimeConfig("jdbc:duckdb:",
        Runtime.getRuntime().availableProcessors(), 0, spillDirectory, 1);
    return new DuckRuntime(config);
  }

  private static MeasuredImport measureImport(java.nio.file.Path arrow, int size,
      java.nio.file.Path spillDirectory, boolean useAppender, boolean sampleResources) throws Exception {
    try (var runtime = benchmarkRuntime(spillDirectory); var job = (DuckJob) runtime.openJob()) {
      if (!sampleResources) {
        long begin = System.nanoTime();
        DuckFrame imported = useAppender ? ArrowFileSupport.readWithAppender(job, arrow) : ArrowFileSupport.read(job, arrow);
        long elapsed = System.nanoTime() - begin;
        long rows = imported.count();
        if (rows != size) throw new IllegalStateException("Arrow-only " + (useAppender ? "appender" : "JDBC")
            + " benchmark cardinality mismatch: " + rows);
        return new MeasuredImport(elapsed, -1, -1, 0);
      }
      var monitor = new BenchmarkResourceMonitor(spillDirectory, 50);
      long begin = System.nanoTime();
      DuckFrame imported;
      long elapsed;
      try {
        imported = useAppender ? ArrowFileSupport.readWithAppender(job, arrow) : ArrowFileSupport.read(job, arrow);
      } finally {
        elapsed = System.nanoTime() - begin;
        monitor.close();
      }
      long rows = imported.count();
      if (rows != size) throw new IllegalStateException("Arrow-only " + (useAppender ? "appender" : "JDBC")
          + " benchmark cardinality mismatch: " + rows);
      return new MeasuredImport(elapsed, monitor.maxRssBytes(), monitor.maxSpillBytes(), monitor.samples());
    }
  }

  private record MeasuredImport(long elapsedNanos, long maxObservedRssBytes,
                                long maxObservedSpillBytes, long sampleCount) {}

  private static int integerArg(String[] args, String name, int fallback) {
    for (int i = 0; i + 1 < args.length; i++) if (name.equals(args[i])) return Integer.parseInt(args[i + 1]);
    return fallback;
  }

  private static String stringArg(String[] args, String name, String fallback) {
    for (int i = 0; i + 1 < args.length; i++) if (name.equals(args[i])) return args[i + 1];
    return fallback;
  }

  private static long median(long[] values) {
    long[] copy = values.clone(); java.util.Arrays.sort(copy); return copy[copy.length / 2];
  }

  private static String samples(long[] values) {
    StringBuilder json = new StringBuilder("[");
    for (int i = 0; i < values.length; i++) {
      if (i > 0) json.append(',');
      json.append(values[i]);
    }
    return json.append(']').toString();
  }

  private static String stats(long[] values) {
    long min = values[0], max = values[0];
    double mean = 0;
    for (long value : values) {
      min = Math.min(min, value);
      max = Math.max(max, value);
      mean += value / (double) values.length;
    }
    double squaredDeviation = 0;
    for (long value : values) {
      double deviation = value - mean;
      squaredDeviation += deviation * deviation / values.length;
    }
    return "{\"minNs\":" + min + ",\"maxNs\":" + max
        + ",\"meanNs\":" + String.format(java.util.Locale.ROOT, "%.3f", mean)
        + ",\"populationStdDevNs\":" + String.format(java.util.Locale.ROOT, "%.3f", Math.sqrt(squaredDeviation)) + "}";
  }
}
