package io.zingg.duckdb.legacy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.zingg.duckdb.api.DuckException;
import io.zingg.duckdb.model.ImportLimits;
import io.zingg.duckdb.model.LegacyImporterSpec;
import io.zingg.duckdb.model.ModelArtifactJson;
import io.zingg.duckdb.model.ModelReader;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.apache.spark.ml.PipelineModel;
import org.apache.spark.ml.Transformer;
import org.apache.spark.ml.classification.LogisticRegressionModel;
import org.apache.spark.ml.feature.PolynomialExpansion;
import org.apache.spark.ml.feature.VectorAssembler;
import org.apache.spark.ml.linalg.Vectors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.io.TempDir;

class SparkClassifierImporterMainTest {
  @Test
  void isolatedLauncherRejectsOutputNestedUnderInputBeforeStartingChild(@TempDir Path temp) throws Exception {
    Path input = temp.resolve("input");
    Files.createDirectories(input);
    Path launcher = temp.resolve("launcher");
    Files.writeString(launcher, "unused");
    Path output = input.resolve("nested/output");
    var spec = new LegacyImporterSpec("zingg-classifier-spark35", "test", launcher,
        List.of(), ImportLimits.defaults());

    DuckException failure = assertThrows(DuckException.class, () ->
        new ClassifierImporterLauncher(ImportLimits.defaults()).launch(spec, input, output));

    assertTrue(failure.getMessage().contains("outside input directory"));
    assertFalse(Files.exists(output), "nested output must be rejected before it is created");
  }

  @Test
  void isolatedLauncherRejectsSymlinkOutputWithoutTouchingExternalDirectory(@TempDir Path temp) throws Exception {
    Path input = temp.resolve("input");
    Path external = temp.resolve("external");
    Files.createDirectories(input);
    Files.createDirectories(external);
    Path sentinel = external.resolve("sentinel");
    Files.writeString(sentinel, "preserve-me");
    Path output = temp.resolve("output-link");
    try {
      Files.createSymbolicLink(output, external);
    } catch (UnsupportedOperationException | java.io.IOException | SecurityException unavailable) {
      Assumptions.abort("directory symlink creation is unavailable: " + unavailable.getClass().getSimpleName());
    }
    Path launcher = temp.resolve("launcher");
    Files.writeString(launcher, "unused");
    var spec = new LegacyImporterSpec("zingg-classifier-spark35", "test", launcher,
        List.of(), ImportLimits.defaults());

    DuckException failure = assertThrows(DuckException.class, () ->
        new ClassifierImporterLauncher(ImportLimits.defaults()).launch(spec, input, output));

    assertTrue(failure.getMessage().contains("symbolic link"));
    assertEquals("preserve-me", Files.readString(sentinel));
  }

  @Test
  void supportedOrderedPipelineImportsAndEscapesFeatureNames(@TempDir Path temp) throws Exception {
    String unusualInput = "feature\"\\\n";
    PipelineModel pipeline = pipeline(
        new String[] {unusualInput, "second"}, "assembled", "assembled", "expanded", "expanded");
    Path output = temp.resolve("classifier.model");

    Path artifact = SparkClassifierImporterMain.importPipeline(
        pipeline, temp.resolve("source-model"), output);

    assertEquals(output, artifact);
    var loaded = ModelReader.load(artifact, 1024 * 1024);
    assertEquals(9, loaded.manifest().features().size());
    var payload = ModelArtifactJson.parseObject(new String(loaded.payload(), java.nio.charset.StandardCharsets.UTF_8));
    assertEquals(List.of(unusualInput, "second"), payload.get("assemblerInputCols"));
    assertEquals(new BigDecimal("3"), payload.get("polynomialDegree"));
    assertEquals(new BigDecimal("0.4"), payload.get("threshold"));
  }

  @Test
  void reorderedStagesAreRejectedAndExistingOutputIsPreserved(@TempDir Path temp) throws Exception {
    Path output = sentinel(temp);
    PipelineModel valid = pipeline(new String[] {"a", "b"}, "assembled", "assembled",
        "expanded", "expanded");
    Transformer[] ordered = valid.stages();
    PipelineModel reordered = new PipelineModel("reordered",
        new Transformer[] {ordered[1], ordered[0], ordered[2]});

    var failure = assertThrows(IllegalArgumentException.class, () ->
        SparkClassifierImporterMain.importPipeline(reordered, temp.resolve("source"), output));

    assertEquals("unsupported classifier pipeline stage sequence", failure.getMessage());
    assertEquals("previous-model", Files.readString(output));
  }

  @Test
  void extraStageIsRejectedBeforePublication(@TempDir Path temp) throws Exception {
    Path output = sentinel(temp);
    Transformer[] stages = pipeline(new String[] {"a", "b"}, "assembled", "assembled",
        "expanded", "expanded").stages();
    PipelineModel extra = new PipelineModel("extra",
        new Transformer[] {stages[0], stages[1], stages[2], new PolynomialExpansion()});

    var failure = assertThrows(IllegalArgumentException.class, () ->
        SparkClassifierImporterMain.importPipeline(extra, temp.resolve("source"), output));

    assertEquals("unsupported classifier pipeline stage sequence", failure.getMessage());
    assertEquals("previous-model", Files.readString(output));
  }

  @Test
  void disconnectedFeatureColumnsAreRejectedBeforePublication(@TempDir Path temp) throws Exception {
    Path output = sentinel(temp);
    PipelineModel disconnected = pipeline(new String[] {"a", "b"}, "assembled",
        "wrong-input", "expanded", "expanded");

    var failure = assertThrows(IllegalArgumentException.class, () ->
        SparkClassifierImporterMain.importPipeline(disconnected, temp.resolve("source"), output));

    assertEquals("unsupported classifier feature-column wiring", failure.getMessage());
    assertEquals("previous-model", Files.readString(output));
  }

  @Test
  void classifierFeatureColumnMustMatchPolynomialOutput(@TempDir Path temp) throws Exception {
    Path output = sentinel(temp);
    PipelineModel disconnected = pipeline(new String[] {"a", "b"}, "assembled",
        "assembled", "expanded", "different-feature-column");

    assertThrows(IllegalArgumentException.class, () ->
        SparkClassifierImporterMain.importPipeline(disconnected, temp.resolve("source"), output));
    assertEquals("previous-model", Files.readString(output));
    assertFalse(Files.isDirectory(output));
  }

  private static PipelineModel pipeline(String[] inputs, String assemblerOutput,
      String polynomialInput, String polynomialOutput, String classifierInput) {
    VectorAssembler assembler = new VectorAssembler().setInputCols(inputs).setOutputCol(assemblerOutput);
    PolynomialExpansion polynomial = new PolynomialExpansion().setInputCol(polynomialInput)
        .setOutputCol(polynomialOutput).setDegree(3);
    LogisticRegressionModel logistic = new LogisticRegressionModel(
        "test-logistic", Vectors.dense(new double[9]), 0.25).setThreshold(0.4)
        .setFeaturesCol(classifierInput);
    return new PipelineModel("test-pipeline", new Transformer[] {assembler, polynomial, logistic});
  }

  private static Path sentinel(Path temp) throws Exception {
    Path output = temp.resolve("classifier.model");
    Files.writeString(output, "previous-model");
    return output;
  }
}
