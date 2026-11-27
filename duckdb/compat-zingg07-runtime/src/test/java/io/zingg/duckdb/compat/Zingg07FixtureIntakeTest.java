package io.zingg.duckdb.compat;

import static org.junit.jupiter.api.Assertions.*;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.security.MessageDigest;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.HexFormat;
import java.sql.DriverManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.io.TempDir;

/** Verifies the checked-in upstream fixture before importer tests consume it. */
class Zingg07FixtureIntakeTest {
  private static final String ROOT = "/external/zingg-v07-model-48cb157/";

  @Test
  void manifestCoversEveryFixtureFile() throws Exception {
    var manifest = read(ROOT + "SHA256SUMS");
    var rows = manifest.lines().filter(line -> !line.isBlank()).toList();
    assertEquals(45, rows.size());
    Path sourceRoot = Path.of("src", "test", "resources").resolve(ROOT.substring(1)).toAbsolutePath().normalize();
    Set<String> expectedPaths = new HashSet<>();
    for (var row : rows) {
      var split = row.split("  ", 2);
      assertEquals(2, split.length, row);
      assertTrue(split[0].matches("[0-9A-Fa-f]{64}"), "invalid SHA-256 in manifest: " + row);
      assertFalse(split[1].isBlank() || split[1].startsWith("/") || split[1].contains("\\"),
          "fixture paths must be relative POSIX paths: " + split[1]);
      assertFalse(java.util.Arrays.stream(split[1].split("/"))
          .anyMatch(part -> part.isBlank() || part.equals(".") || part.equals("..")),
          "fixture path has an unsafe component: " + split[1]);
      Path file = sourceRoot.resolve(split[1]).normalize();
      assertTrue(file.startsWith(sourceRoot), "fixture path escapes root: " + split[1]);
      assertTrue(Files.isRegularFile(file, LinkOption.NOFOLLOW_LINKS), "missing or linked fixture file: " + split[1]);
      assertTrue(expectedPaths.add(split[1]), "duplicate fixture path: " + split[1]);
      String actualHash = HexFormat.of().withUpperCase().formatHex(
          MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(file)));
      assertEquals(split[0].toUpperCase(), actualHash, "SHA-256 mismatch: " + split[1]);
      // Maven excludes Spark's hidden .crc files from the classpath; hash them
      // from the source tree while requiring all consumable resources on it.
      if (!file.getFileName().toString().startsWith(".")) assertNotNull(get(ROOT + split[1]));
    }
    Set<String> actualPaths = new HashSet<>();
    for (String subdirectory : List.of("block", "classifier")) {
      try (var paths = Files.walk(sourceRoot.resolve(subdirectory))) {
        paths.filter(path -> Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS))
            .map(sourceRoot::relativize)
            .map(path -> path.toString().replace('\\', '/'))
            .forEach(actualPaths::add);
      }
    }
    assertEquals(expectedPaths, actualPaths, "manifest must cover exactly every blocking/classifier model file");
  }

  @Test
  void classifierMetadataPreservesPipelineContract() {
    var assembler = read(ROOT + "classifier/best.model/bestModel/stages/0_vecAssembler_ca0ffaafe2a1/metadata/part-00000");
    var polynomial = read(ROOT + "classifier/best.model/bestModel/stages/1_poly_83edb6284120/metadata/part-00000");
    var logistic = read(ROOT + "classifier/best.model/bestModel/stages/2_logreg_a0b6b01da133/metadata/part-00000");
    assertTrue(assembler.contains("z_sim0") && assembler.contains("z_sim17"));
    assertTrue(polynomial.contains("\"degree\":3"));
    assertTrue(logistic.contains("\"threshold\":0.4"));
    assertTrue(logistic.contains("\"featuresCol\":\"z_feature\""));
    assertTrue(logistic.contains("\"predictionCol\":\"z_prediction\""));
  }

  @Test
  void classifierParquetShapeIsReadThroughDuckdb() throws Exception {
    var resource = ROOT + "classifier/best.model/bestModel/stages/2_logreg_a0b6b01da133/data/part-00000-1868afb6-4fe2-43d9-bdfe-fea9fa59bd7e-c000.snappy.parquet";
    var path = java.nio.file.Files.createTempFile("zingg07-model-", ".parquet");
    try (var input = get(resource)) {
      java.nio.file.Files.copy(input, path, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
    }
    try (var connection = DriverManager.getConnection("jdbc:duckdb:");
         var statement = connection.prepareStatement("select numClasses, numFeatures, interceptVector, coefficientMatrix, isMultinomial from read_parquet(?)")) {
      statement.setString(1, path.toString());
      try (var rows = statement.executeQuery()) {
        assertTrue(rows.next());
        assertEquals(2, rows.getInt(1));
        assertEquals(1329, rows.getInt(2));
        assertTrue(rows.getObject(3).toString().contains("-17.79201795127379"));
        assertTrue(rows.getObject(4).toString().contains("0.050045410025680616"));
        assertFalse(rows.getBoolean(5));
        assertFalse(rows.next());
      }
    } finally {
      java.nio.file.Files.deleteIfExists(path);
    }
  }

  @Test
  void classifierParquetSnapshotIsPrivateAndDeletedOnClose(@TempDir Path temp) throws Exception {
    var resource = ROOT + "classifier/best.model/bestModel/stages/2_logreg_a0b6b01da133/data/part-00000-1868afb6-4fe2-43d9-bdfe-fea9fa59bd7e-c000.snappy.parquet";
    var source = temp.resolve("source.parquet");
    try (var input = get(resource)) { Files.copy(input, source, java.nio.file.StandardCopyOption.REPLACE_EXISTING); }
    Path snapshotPath;
    try (var snapshot = SparkMlClassifierImporter.snapshotParquet(source)) {
      snapshotPath = snapshot.path();
      assertNotEquals(source, snapshotPath);
      assertEquals(Files.size(source), Files.size(snapshotPath));
      assertArrayEquals(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(source)),
          MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(snapshotPath)));
      try {
        assertEquals(Set.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE),
            Files.getPosixFilePermissions(snapshotPath));
      } catch (UnsupportedOperationException unsupported) {
        // Windows ACL behavior is inherited from the system temporary directory.
      }
    }
    assertFalse(Files.exists(snapshotPath), "temporary classifier snapshot must be removed on close");
    Files.deleteIfExists(source);
  }

  @Test
  void classifierParquetSnapshotRejectsSymbolicLinkSource(@TempDir Path temp) throws Exception {
    var source = temp.resolve("source.parquet");
    Files.write(source, new byte[] {1});
    var link = source.resolveSibling(source.getFileName() + ".link");
    try {
      Files.createSymbolicLink(link, source);
    } catch (UnsupportedOperationException | java.io.IOException | SecurityException unavailable) {
      Assumptions.abort("file symlink creation is unavailable: " + unavailable.getClass().getSimpleName());
    }
    assertThrows(Exception.class, () -> SparkMlClassifierImporter.snapshotParquet(link));
    Files.deleteIfExists(link);
    Files.deleteIfExists(source);
  }

  @Test
  void sparkClassifierImporterProducesNativeArtifact() throws Exception {
    var root = java.nio.file.Files.createTempDirectory("zingg07-import-");
    try {
      var output = root.resolve("classifier.model");
      var fixture = java.nio.file.Files.createTempDirectory("zingg07-fixture-");
      copyFixture(fixture);
      var artifact = new SparkMlClassifierImporter().importModel(fixture, "zingg-0.7.0", output);
      assertEquals(output.toAbsolutePath().normalize(), artifact);
      assertTrue(java.nio.file.Files.size(output) > 0);
      var loaded = io.zingg.duckdb.model.ModelReader.load(output, 64L * 1024 * 1024);
      var scorer = (LinearClassifier) new ModelScorerRegistry().create(loaded);
      var values = new java.util.LinkedHashMap<String,Object>();
      for (int i = 0; i < 18; i++) values.put("z_sim" + i, 0d);
      assertEquals(0.4d, scorer.threshold());
      assertEquals(1329, scorer.weights().length);
      assertEquals(oracleProbability("zeros"), scorer.score(values), 1e-12);
      for (int i = 0; i < 18; i++) values.put("z_sim" + i, 1d);
      assertEquals(oracleProbability("ones"), scorer.score(values), 1e-12);
      for (int i = 0; i < 18; i++) values.put("z_sim" + i, (i + 1) / 18d);
      assertEquals(oracleProbability("ramp"), scorer.score(values), 1e-12);
    } finally {
      // The model writer uses atomic publication; test cleanup is limited to its
      // uniquely allocated temporary directories.
      try (var paths = java.nio.file.Files.walk(root)) { paths.sorted(java.util.Comparator.reverseOrder()).forEach(p -> p.toFile().delete()); }
    }
  }

  @Test
  void sparkClassifierImporterPreservesNonDefaultThresholdAndRejectsInvalidValues() throws Exception {
    Path fixture = Files.createTempDirectory("zingg07-threshold-");
    try {
      copyFixture(fixture);
      Path metadata = fixture.resolve(
          "classifier/best.model/bestModel/stages/2_logreg_a0b6b01da133/metadata/part-00000");
      String original = Files.readString(metadata);
      assertTrue(original.contains("\"threshold\":0.4"));
      Files.writeString(metadata, original.replace("\"threshold\":0.4", "\"threshold\":0.73"));

      Path imported = fixture.resolve("non-default-threshold.model");
      new SparkMlClassifierImporter().importModel(fixture, "zingg-0.7.0", imported);
      var scorer = (LinearClassifier) new ModelScorerRegistry().create(
          io.zingg.duckdb.model.ModelReader.load(imported, 64L * 1024 * 1024));
      assertEquals(0.73d, scorer.threshold());
      assertEquals(1329, scorer.weights().length);

      var featureColumns = java.util.stream.IntStream.range(0, 18)
          .mapToObj(index -> "z_sim" + index).toList();
      var recordsCsv = new StringBuilder("id,block,").append(String.join(",", featureColumns)).append('\n');
      recordsCsv.append("1,same-block");
      for (int i = 0; i < featureColumns.size(); i++) recordsCsv.append(",1.0");
      recordsCsv.append('\n').append("2,same-block");
      for (int i = 0; i < featureColumns.size(); i++) recordsCsv.append(",0.0");
      recordsCsv.append('\n');
      Path records = fixture.resolve("threshold-match-records.csv");
      Files.writeString(records, recordsCsv);
      var runtimeConfig = new io.zingg.duckdb.api.RuntimeConfig(
          "jdbc:duckdb:" + fixture.resolve("threshold-match.duckdb"), 1, 0, null, 1);
      try (var runtime = new CompatibilityRuntime(runtimeConfig, null);
           var workflow = new PersistedMatchWorkflow(runtime)) {
        workflow.restartFromModel(imported);
        var input = workflow.read(List.of(records), ZinggJob.Phase.MATCH, null);
        var matches = workflow.match(input, new Matcher.MatchConfig("id", "block", "TRUE", 2.0d)).collect();
        assertEquals(List.of("1:2"), matches.stream()
            .map(row -> row.get("id") + ":" + row.get("z_id")).toList(),
            "the imported model threshold must override the conflicting MatchConfig threshold");
        assertEquals(1, ((Number) matches.get(0).get("z_prediction")).intValue());
        assertEquals(oracleProbability("ones"), ((Number) matches.get(0).get("z_score")).doubleValue(), 1e-12);
      }

      for (String invalidThreshold : List.of("0.0", "1.0")) {
        Files.writeString(metadata, original.replace("\"threshold\":0.4",
            "\"threshold\":" + invalidThreshold));
        Path destination = fixture.resolve("invalid-threshold-" + invalidThreshold + ".model");
        Files.writeString(destination, "previous-artifact-must-survive");
        var failure = assertThrows(io.zingg.duckdb.api.DuckException.class,
            () -> new SparkMlClassifierImporter().importModel(fixture, "zingg-0.7.0", destination));
        assertEquals("unsupported Spark classifier threshold: expected a value strictly between 0 and 1",
            failure.getMessage());
        assertEquals("previous-artifact-must-survive", Files.readString(destination));
      }
    } finally {
      try (var paths = Files.walk(fixture)) {
        paths.sorted(java.util.Comparator.reverseOrder()).forEach(path -> path.toFile().delete());
      }
    }
  }

  @Test
  void importedSparkClassifierScoresPersistedMatchPairsAndFeedsNativeLink(@TempDir Path temp)
      throws Exception {
    Path fixture = temp.resolve("fixture");
    copyFixture(fixture);
    Path model = temp.resolve("imported-classifier.model");
    new SparkMlClassifierImporter().importModel(fixture, "zingg-0.7.0", model);

    var featureColumns = java.util.stream.IntStream.range(0, 18)
        .mapToObj(index -> "z_sim" + index).toList();
    var csv = new StringBuilder("id,block,").append(String.join(",", featureColumns)).append('\n');
    for (int row = 0; row < 4; row++) {
      csv.append(row + 1).append(",same-block");
      for (int feature = 0; feature < featureColumns.size(); feature++) {
        double value = switch (row) {
          case 0 -> 0d;
          case 1 -> 1d;
          case 2 -> (feature + 1) / 18d;
          default -> 0d;
        };
        csv.append(',').append(value);
      }
      csv.append('\n');
    }
    Path records = temp.resolve("match-records.csv");
    Files.writeString(records, csv);

    Path linkInput = temp.resolve("accepted-pairs.csv");
    var runtimeConfig = new io.zingg.duckdb.api.RuntimeConfig(
        "jdbc:duckdb:" + temp.resolve("imported-match.duckdb"), 2, 0, null, 2);
    try (var runtime = new CompatibilityRuntime(runtimeConfig, null);
         var workflow = new PersistedMatchWorkflow(runtime)) {
      workflow.restartFromModel(model);
      var input = workflow.read(List.of(records), ZinggJob.Phase.MATCH, null);
      // A config threshold of 2.0 rejects every probability; the persisted
      // Spark classifier's threshold 0.4 must remain authoritative.
      var matches = workflow.match(input, new Matcher.MatchConfig("id", "block", "TRUE", 2.0)).collect();
      assertEquals(List.of("2:3", "2:4"), matches.stream()
          .map(row -> row.get("id") + ":" + row.get("z_id")).sorted().toList());
      assertTrue(matches.stream().allMatch(row -> ((Number) row.get("z_prediction")).intValue() == 1));
      for (var match : matches)
        assertEquals(oracleProbability("ones"), ((Number) match.get("z_score")).doubleValue(), 1e-12);
      var linkCsv = new StringBuilder("z_zid,z_z_zid,z_score\n");
      for (var match : matches)
        linkCsv.append(match.get("id")).append(',').append(match.get("z_id")).append(',')
            .append(match.get("z_score")).append('\n');
      Files.writeString(linkInput, linkCsv);
    }

    Path linkedEntities = temp.resolve("linked-entities.csv");
    try (var runtime = new CompatibilityRuntime(new io.zingg.duckdb.api.RuntimeConfig(
        "jdbc:duckdb:" + temp.resolve("native-link.duckdb"), 2, 0, null, 2), null)) {
      LocalPhaseExecutors.registerNativeLink(runtime);
      var result = new ZinggPipeline(runtime).execute(new PipelineConfig(
          ZinggJob.Phase.LINK, List.of(linkInput), linkedEntities, "TRUE"));
      assertEquals(2, result.inputRows());
      assertEquals(3, result.outputRows());
    }
    var output = Files.readAllLines(linkedEntities);
    assertEquals(4, output.size()); // header and the 3 entities in one connected component
    assertTrue(output.get(0).contains("z_zid") && output.get(0).contains("z_minScore")
        && output.get(0).contains("z_maxScore") && output.get(0).contains("z_cluster"));
    assertTrue(output.stream().skip(1).anyMatch(line -> line.startsWith("2,")));
    assertTrue(output.stream().skip(1).anyMatch(line -> line.startsWith("3,")));
    assertTrue(output.stream().skip(1).anyMatch(line -> line.startsWith("4,")));
  }

  @Test
  void sparkClassifierImporterDiscoversStageIdsAndParquetFilename() throws Exception {
    var fixture = Files.createTempDirectory("zingg07-discovery-");
    try {
      copyFixture(fixture);
      Path stages = fixture.resolve("classifier/best.model/bestModel/stages");
      Files.move(stages.resolve("0_vecAssembler_ca0ffaafe2a1"), stages.resolve("stage-a"));
      Files.move(stages.resolve("1_poly_83edb6284120"), stages.resolve("stage-b"));
      Files.move(stages.resolve("2_logreg_a0b6b01da133"), stages.resolve("stage-c"));
      Path data = stages.resolve("stage-c/data");
      try (var files = Files.list(data)) {
        Files.move(files.filter(path -> path.toString().endsWith(".parquet")).findFirst().orElseThrow(), data.resolve("part-arbitrary-name.parquet"));
      }
      Path output = fixture.resolve("discovered.model");
      assertEquals(output.toAbsolutePath().normalize(), new SparkMlClassifierImporter().importModel(fixture, "zingg-0.7.0", output));
      assertTrue(Files.isDirectory(output));
      assertEquals(io.zingg.duckdb.model.ModelType.CLASSIFIER.name(),
          io.zingg.duckdb.model.ModelReader.load(output, 64L * 1024 * 1024).manifest().modelType());
    } finally {
      try (var paths = Files.walk(fixture)) { paths.sorted(java.util.Comparator.reverseOrder()).forEach(p -> p.toFile().delete()); }
    }
  }

  @Test
  void ambiguousSparkClassifierStageFailsWithoutPublishingModel() throws Exception {
    var fixture = Files.createTempDirectory("zingg07-ambiguous-stage-");
    try {
      copyFixture(fixture);
      Path stages = fixture.resolve("classifier/best.model/bestModel/stages");
      Path source = stages.resolve("0_vecAssembler_ca0ffaafe2a1");
      Path duplicate = stages.resolve("duplicate-assembler");
      Files.createDirectories(duplicate.resolve("metadata"));
      Files.copy(source.resolve("metadata/part-00000"), duplicate.resolve("metadata/part-00000"));
      Path output = fixture.resolve("out.model");
      var error = assertThrows(io.zingg.duckdb.api.DuckException.class,
          () -> new SparkMlClassifierImporter().importModel(fixture, "zingg-0.7.0", output));
      assertEquals("ambiguous Spark classifier stage: org.apache.spark.ml.feature.VectorAssembler", error.getMessage());
      assertFalse(Files.exists(output));
    } finally {
      try (var paths = Files.walk(fixture)) { paths.sorted(java.util.Comparator.reverseOrder()).forEach(p -> p.toFile().delete()); }
    }
  }

  @Test
  void ambiguousSparkClassifierParquetFailsWithoutPublishingModel() throws Exception {
    var fixture = Files.createTempDirectory("zingg07-ambiguous-data-");
    try {
      copyFixture(fixture);
      Path data = fixture.resolve("classifier/best.model/bestModel/stages/2_logreg_a0b6b01da133/data");
      try (var files = Files.list(data)) {
        Path parquet = files.filter(path -> path.toString().endsWith(".parquet")).findFirst().orElseThrow();
        Files.copy(parquet, data.resolve("duplicate.parquet"));
      }
      Path output = fixture.resolve("out.model");
      var error = assertThrows(io.zingg.duckdb.api.DuckException.class,
          () -> new SparkMlClassifierImporter().importModel(fixture, "zingg-0.7.0", output));
      assertEquals("ambiguous Spark classifier data files", error.getMessage());
      assertFalse(Files.exists(output));
    } finally {
      try (var paths = Files.walk(fixture)) { paths.sorted(java.util.Comparator.reverseOrder()).forEach(p -> p.toFile().delete()); }
    }
  }

  @Test
  void unsupportedSparkPolynomialMetadataFailsWithStableDiagnostic() throws Exception {
    var fixture = java.nio.file.Files.createTempDirectory("zingg07-unsupported-");
    try {
      copyFixture(fixture);
      var metadata = fixture.resolve("classifier/best.model/bestModel/stages/1_poly_83edb6284120/metadata/part-00000");
      java.nio.file.Files.writeString(metadata, read(ROOT + "classifier/best.model/bestModel/stages/1_poly_83edb6284120/metadata/part-00000").replace("\"degree\":3", "\"degree\":2"));
      var error = assertThrows(io.zingg.duckdb.api.DuckException.class, () -> new SparkMlClassifierImporter().importModel(fixture, "zingg-0.7.0", fixture.resolve("out.model")));
      assertEquals("unsupported Spark classifier metadata", error.getMessage());
    } finally {
      try (var paths = java.nio.file.Files.walk(fixture)) { paths.sorted(java.util.Comparator.reverseOrder()).forEach(p -> p.toFile().delete()); }
    }
  }

  @Test
  void unsupportedPipelineStageFailsWithoutPublishingModel() throws Exception {
    var fixture = Files.createTempDirectory("zingg07-extra-stage-");
    try {
      copyFixture(fixture);
      var root = fixture.resolve("classifier/best.model/bestModel");
      var metadata = root.resolve("metadata/part-00000");
      String pipeline = Files.readString(metadata);
      String logregUid = "logreg_a0b6b01da133";
      Files.writeString(metadata, pipeline.replace(logregUid + "\"]",
          logregUid + "\",\"scaler_unsupported\"]"));
      var extraStage = root.resolve("stages/scaler_unsupported/metadata/part-00000");
      Files.createDirectories(extraStage.getParent());
      Files.writeString(extraStage,
          "{\"class\":\"org.apache.spark.ml.feature.StandardScalerModel\","
              + "\"sparkVersion\":\"3.0.1\",\"uid\":\"scaler_unsupported\","
              + "\"paramMap\":{},\"defaultParamMap\":{}}\n");
      Path output = fixture.resolve("out.model");
      Files.writeString(output, "previous-output-must-survive");
      var error = assertThrows(io.zingg.duckdb.api.DuckException.class,
          () -> new SparkMlClassifierImporter().importModel(fixture, "zingg-0.7.0", output));
      assertEquals("unsupported Spark classifier stage sequence", error.getMessage());
      assertEquals("previous-output-must-survive", Files.readString(output));
    } finally {
      try (var paths = Files.walk(fixture)) {
        paths.sorted(java.util.Comparator.reverseOrder()).forEach(p -> p.toFile().delete());
      }
    }
  }

  @Test
  void reorderedPipelineStagesFailWithoutPublishingModel() throws Exception {
    var fixture = Files.createTempDirectory("zingg07-reordered-stages-");
    try {
      copyFixture(fixture);
      var metadata = fixture.resolve("classifier/best.model/bestModel/metadata/part-00000");
      String pipeline = Files.readString(metadata);
      String ordered = "[\"vecAssembler_ca0ffaafe2a1\",\"poly_83edb6284120\","
          + "\"logreg_a0b6b01da133\"]";
      String reordered = "[\"poly_83edb6284120\",\"vecAssembler_ca0ffaafe2a1\","
          + "\"logreg_a0b6b01da133\"]";
      Files.writeString(metadata, pipeline.replace(ordered, reordered));
      Path output = fixture.resolve("out.model");
      Files.writeString(output, "previous-output-must-survive");
      var error = assertThrows(io.zingg.duckdb.api.DuckException.class,
          () -> new SparkMlClassifierImporter().importModel(fixture, "zingg-0.7.0", output));
      assertEquals("unsupported Spark classifier stage sequence", error.getMessage());
      assertEquals("previous-output-must-survive", Files.readString(output));
    } finally {
      try (var paths = Files.walk(fixture)) {
        paths.sorted(java.util.Comparator.reverseOrder()).forEach(p -> p.toFile().delete());
      }
    }
  }

  @Test
  void disconnectedFeatureColumnsFailWithoutPublishingModel() throws Exception {
    var fixture = Files.createTempDirectory("zingg07-disconnected-");
    try {
      copyFixture(fixture);
      var polynomial = fixture.resolve(
          "classifier/best.model/bestModel/stages/1_poly_83edb6284120/metadata/part-00000");
      Files.writeString(polynomial, Files.readString(polynomial)
          .replace("\"inputCol\":\"z_featurevector\"", "\"inputCol\":\"wrong_vector\""));
      Path output = fixture.resolve("out.model");
      Files.writeString(output, "previous-output-must-survive");
      var error = assertThrows(io.zingg.duckdb.api.DuckException.class,
          () -> new SparkMlClassifierImporter().importModel(fixture, "zingg-0.7.0", output));
      assertEquals("unsupported Spark classifier feature-column wiring", error.getMessage());
      assertEquals("previous-output-must-survive", Files.readString(output));
    } finally {
      try (var paths = Files.walk(fixture)) {
        paths.sorted(java.util.Comparator.reverseOrder()).forEach(p -> p.toFile().delete());
      }
    }
  }

  @Test
  void fractionalSparkPolynomialDegreeIsRejectedWithoutPublishingModel() throws Exception {
    var fixture = java.nio.file.Files.createTempDirectory("zingg07-fractional-degree-");
    try {
      copyFixture(fixture);
      var metadata = fixture.resolve("classifier/best.model/bestModel/stages/1_poly_83edb6284120/metadata/part-00000");
      java.nio.file.Files.writeString(metadata, read(ROOT + "classifier/best.model/bestModel/stages/1_poly_83edb6284120/metadata/part-00000").replace("\"degree\":3", "\"degree\":3.9"));
      var output = fixture.resolve("out.model");
      var error = assertThrows(io.zingg.duckdb.api.DuckException.class, () -> new SparkMlClassifierImporter().importModel(fixture, "zingg-0.7.0", output));
      assertEquals("Spark metadata field must be an exact integer: degree", error.getMessage());
      assertFalse(java.nio.file.Files.exists(output));
    } finally {
      try (var paths = java.nio.file.Files.walk(fixture)) { paths.sorted(java.util.Comparator.reverseOrder()).forEach(p -> p.toFile().delete()); }
    }
  }

  @Test
  void malformedClassifierMetadataIsRejectedWithoutPublishingModel() throws Exception {
    var fixture = Files.createTempDirectory("zingg07-malformed-metadata-");
    try {
      copyFixture(fixture);
      var metadata = fixture.resolve("classifier/best.model/bestModel/stages/0_vecAssembler_ca0ffaafe2a1/metadata/part-00000");
      Files.writeString(metadata, read(ROOT + "classifier/best.model/bestModel/stages/0_vecAssembler_ca0ffaafe2a1/metadata/part-00000") + " trailing");
      Path output = fixture.resolve("out.model");
      assertThrows(io.zingg.duckdb.api.DuckException.class,
          () -> new SparkMlClassifierImporter().importModel(fixture, "zingg-0.7.0", output));
      assertFalse(Files.exists(output));
    } finally {
      try (var paths = Files.walk(fixture)) { paths.sorted(java.util.Comparator.reverseOrder()).forEach(p -> p.toFile().delete()); }
    }
  }

  @Test
  void wrongClassifierMetadataFieldTypeIsRejectedWithoutPublishingModel() throws Exception {
    var fixture = Files.createTempDirectory("zingg07-wrong-metadata-type-");
    try {
      copyFixture(fixture);
      var metadata = fixture.resolve("classifier/best.model/bestModel/stages/0_vecAssembler_ca0ffaafe2a1/metadata/part-00000");
      String source = read(ROOT + "classifier/best.model/bestModel/stages/0_vecAssembler_ca0ffaafe2a1/metadata/part-00000");
      Files.writeString(metadata, source.replaceFirst("\\\"inputCols\\\":\\[[^]]*\\]", "\\\"inputCols\\\":\\\"not-an-array\\\""));
      Path output = fixture.resolve("out.model");
      assertThrows(io.zingg.duckdb.api.DuckException.class,
          () -> new SparkMlClassifierImporter().importModel(fixture, "zingg-0.7.0", output));
      assertFalse(Files.exists(output));
    } finally {
      try (var paths = Files.walk(fixture)) { paths.sorted(java.util.Comparator.reverseOrder()).forEach(p -> p.toFile().delete()); }
    }
  }

  @Test
  void invalidUtf8ClassifierMetadataIsRejectedWithoutPublishingModel() throws Exception {
    var fixture = Files.createTempDirectory("zingg07-invalid-metadata-utf8-");
    try {
      copyFixture(fixture);
      var metadata = fixture.resolve("classifier/best.model/bestModel/stages/0_vecAssembler_ca0ffaafe2a1/metadata/part-00000");
      byte[] valid = Files.readAllBytes(metadata);
      byte[] invalid = java.util.Arrays.copyOf(valid, valid.length + 1);
      invalid[invalid.length - 1] = (byte) 0xc3;
      Files.write(metadata, invalid);
      Path output = fixture.resolve("out.model");
      assertThrows(io.zingg.duckdb.api.DuckException.class,
          () -> new SparkMlClassifierImporter().importModel(fixture, "zingg-0.7.0", output));
      assertFalse(Files.exists(output));
    } finally {
      try (var paths = Files.walk(fixture)) { paths.sorted(java.util.Comparator.reverseOrder()).forEach(p -> p.toFile().delete()); }
    }
  }

  @Test
  void parquetVectorParserRejectsPartialAndNonFiniteNumbers() {
    assertEquals(List.of(1.25d, -2d), SparkMlClassifierImporter.numbersFromValues(
        "{type=1, size=null, indices=null, values=[1.25, -2.0]}"));
    assertThrows(io.zingg.duckdb.api.DuckException.class,
        () -> SparkMlClassifierImporter.numbersFromValues("{type=1, values=[1.0, garbage]}"));
    assertThrows(io.zingg.duckdb.api.DuckException.class,
        () -> SparkMlClassifierImporter.numbersFromValues("{type=1, values=[NaN]}"));
    assertThrows(io.zingg.duckdb.api.DuckException.class,
        () -> SparkMlClassifierImporter.numbersFromValues("{type=0, values=[1.0]}"));
    assertThrows(io.zingg.duckdb.api.DuckException.class,
        () -> SparkMlClassifierImporter.numbersFromValues("{type=1, indices=[0], values=[1.0]}"));
    assertThrows(io.zingg.duckdb.api.DuckException.class,
        () -> SparkMlClassifierImporter.numbersFromValues("{type=1, values=[1.0"));
  }

  private static void copyFixture(java.nio.file.Path target) throws Exception {
    var root = target.resolve("classifier/best.model/bestModel");
    java.nio.file.Files.createDirectories(root);
    copyResource("classifier/best.model/bestModel/metadata/part-00000", root.resolve("metadata/part-00000"));
    copyResource("classifier/best.model/bestModel/stages/0_vecAssembler_ca0ffaafe2a1/metadata/part-00000", root.resolve("stages/0_vecAssembler_ca0ffaafe2a1/metadata/part-00000"));
    copyResource("classifier/best.model/bestModel/stages/1_poly_83edb6284120/metadata/part-00000", root.resolve("stages/1_poly_83edb6284120/metadata/part-00000"));
    copyResource("classifier/best.model/bestModel/stages/2_logreg_a0b6b01da133/metadata/part-00000", root.resolve("stages/2_logreg_a0b6b01da133/metadata/part-00000"));
    copyResource("classifier/best.model/bestModel/stages/2_logreg_a0b6b01da133/data/part-00000-1868afb6-4fe2-43d9-bdfe-fea9fa59bd7e-c000.snappy.parquet", root.resolve("stages/2_logreg_a0b6b01da133/data/part-00000-1868afb6-4fe2-43d9-bdfe-fea9fa59bd7e-c000.snappy.parquet"));
  }

  private static void copyResource(String resource, java.nio.file.Path target) throws Exception {
    java.nio.file.Files.createDirectories(target.getParent());
    try (var input = get(ROOT + resource)) { java.nio.file.Files.copy(input, target, java.nio.file.StandardCopyOption.REPLACE_EXISTING); }
  }

  private static double oracleProbability(String name) {
    var json = read(ROOT + "spark-differential.json");
    var match = java.util.regex.Pattern.compile("\\\"name\\\": \\\"" + name + "\\\".*?\\\"probability\\\": \\[([^,]+),\\s*([^]]+)\\]", java.util.regex.Pattern.DOTALL).matcher(json);
    assertTrue(match.find(), "missing Spark oracle case: " + name);
    return Double.parseDouble(match.group(2));
  }

  private static String read(String path) {
    try (var stream = get(path)) {
      return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
    } catch (Exception e) {
      throw new AssertionError("fixture is unreadable: " + path, e);
    }
  }

  private static InputStream get(String path) {
    var stream = Zingg07FixtureIntakeTest.class.getResourceAsStream(path);
    assertNotNull(stream, "missing fixture resource: " + path);
    return stream;
  }
}
