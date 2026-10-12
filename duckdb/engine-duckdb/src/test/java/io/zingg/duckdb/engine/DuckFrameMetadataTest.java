package io.zingg.duckdb.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class DuckFrameMetadataTest {
  @Test
  void sqlFramesDiscoverColumnsBeforeComposition(@TempDir Path temp) {
    var config = new io.zingg.duckdb.api.RuntimeConfig(
        "jdbc:duckdb:" + temp.resolve("metadata.duckdb"), 1, 0, null, 1);
    try (var runtime = new DuckRuntime(config); var job = runtime.openJob()) {
      var frame = job.sql("select 7 as value, 'ok' as label");
      assertEquals(java.util.List.of("value", "label"), frame.columns());
      assertEquals(1, frame.select("value").count());
      assertEquals(List.of("value"), frame.select("VALUE").columns());
      assertEquals(List.of("value"), frame.drop("LABEL").columns());
      assertEquals(1, frame.dropDuplicates("VALUE").count());
      assertEquals(java.util.List.of("value", "label", "source"),
          frame.withColumn("source", "test").columns());
      var caseVariantReplacement = frame.withColumn("VALUE", 9);
      assertEquals(List.of("label", "VALUE"), caseVariantReplacement.columns());
      assertEquals(caseVariantReplacement.columns(), caseVariantReplacement.schema().stream()
          .map(io.zingg.duckdb.api.Column::name).toList());
      assertEquals(9, ((Number) caseVariantReplacement.collect().get(0).get("VALUE")).intValue());
      var caseVariantRename = frame.rename("VALUE", "RenamedValue");
      assertEquals(List.of("label", "RenamedValue"), caseVariantRename.columns());
      assertEquals(7, ((Number) caseVariantRename.collect().get(0).get("RenamedValue")).intValue());
      var caseOnlyRename = frame.rename("value", "VALUE");
      assertEquals(List.of("label", "VALUE"), caseOnlyRename.columns());
      assertThrows(io.zingg.duckdb.api.DuckException.class,
          () -> frame.rename("value", "LABEL"));
    }
  }

  @Test
  void validatedModelExpressionsAllowLargeSafeScoresButRejectSqlStatements(@TempDir Path temp) {
    var config = new io.zingg.duckdb.api.RuntimeConfig(
        "jdbc:duckdb:" + temp.resolve("model-expression.duckdb"), 1, 0, null, 1);
    try (var runtime = new DuckRuntime(config); var job = runtime.openJob()) {
      var input = job.sql("SELECT 1.0 AS feature");
      String expression = "list_sum(list_value(" + java.util.stream.IntStream.range(0, 600)
          .mapToObj(ignored -> "COALESCE(\"feature\", 0)")
          .collect(java.util.stream.Collectors.joining(", ")) + "))";
      assertTrue(expression.length() > 4096);
      var scored = input.withColumn("z_score", new ValidatedModelExpression(expression));
      assertEquals(600d, ((Number) scored.collect().get(0).get("z_score")).doubleValue(), 1e-12);
      var scientific = input.withColumn("scientific", new ValidatedModelExpression("1.0E-8 + .5"));
      assertEquals(0.50000001d,
          ((Number) scientific.collect().get(0).get("scientific")).doubleValue(), 1e-12);
      assertThrows(io.zingg.duckdb.api.DuckException.class,
          () -> new ValidatedModelExpression("1.0); DROP TABLE important; --"));
    }
  }

  @Test
  void explodeRefreshesChangedSchemaAndCanBeComposed(@TempDir Path temp) {
    var config = new io.zingg.duckdb.api.RuntimeConfig(
        "jdbc:duckdb:" + temp.resolve("explode-metadata.duckdb"), 1, 0, null, 1);
    try (var runtime = new DuckRuntime(config); var job = runtime.openJob()) {
      var source = job.sql("SELECT * FROM (VALUES ([10, 20], 'kept')) t(items, label)");
      var exploded = source.explode("items", "item");
      assertEquals(List.of("label", "item"), exploded.columns());
      assertEquals(List.of("label", "item"),
          exploded.schema().stream().map(io.zingg.duckdb.api.Column::name).toList());
      assertEquals(List.of(10, 20), exploded.select("item").collect().stream()
          .map(row -> ((Number) row.get("item")).intValue()).toList());
      var caseVariantExploded = source.explode("ITEMS", "Item");
      assertEquals(List.of("label", "Item"), caseVariantExploded.columns());
      assertEquals(2, caseVariantExploded.count());
      assertThrows(io.zingg.duckdb.api.DuckException.class,
          () -> source.explode("items", "LABEL"));
    }
  }

  @Test
  void aggregateJoinAndExpressionProjectionExposeActualOutputNames(@TempDir Path temp) {
    var config = new io.zingg.duckdb.api.RuntimeConfig(
        "jdbc:duckdb:" + temp.resolve("schema-transformations.duckdb"), 1, 0, null, 1);
    try (var runtime = new DuckRuntime(config); var job = runtime.openJob()) {
      var source = job.sql("SELECT * FROM (VALUES ('a', 1), ('a', 2), ('b', 3)) t(group_name, amount)");
      var aggregate = source.aggregate(new String[] {"group_name"}, "count(*) AS n");
      assertEquals(List.of("group_name", "n"), aggregate.columns());
      assertEquals(2, aggregate.select("group_name", "n").count());

      var joined = source.join(job.sql("SELECT 'a' AS group_name, 'matched' AS marker"),
          "l.group_name = r.group_name");
      assertEquals(4, joined.columns().size());
      assertEquals(List.of("group_name", "amount", "group_name_1", "marker"), joined.columns());
      assertEquals(joined.columns(), joined.schema().stream()
          .map(io.zingg.duckdb.api.Column::name).toList());
      assertTrue(joined.columns().stream().anyMatch(name -> name.startsWith("marker")));
      assertEquals(2, joined.count());
      assertEquals("matched", joined.select("group_name_1", "marker").collect().get(0).get("marker"));

      var usingJoin = source.joinUsing(job.sql("SELECT 'a' AS group_name, 99 AS amount"),
          "inner", "GROUP_NAME");
      assertEquals(List.of("group_name", "amount", "amount_1"), usingJoin.columns());
      assertEquals(usingJoin.columns(), usingJoin.schema().stream()
          .map(io.zingg.duckdb.api.Column::name).toList());
      assertEquals(99, ((Number) usingJoin.select("amount_1").collect().get(0).get("amount_1")).intValue());
      assertEquals(List.of("amount_1"), usingJoin.select("AMOUNT_1").columns());

      var projected = source.selectExpr("amount + 1");
      assertEquals(1, projected.columns().size());
      assertEquals(3, projected.count());

      var repeatedProjection = source.select("group_name", "group_name");
      assertEquals(List.of("group_name", "group_name_1"), repeatedProjection.columns());
      assertEquals(repeatedProjection.columns(), repeatedProjection.schema().stream()
          .map(io.zingg.duckdb.api.Column::name).toList());
      assertEquals("a", repeatedProjection.collect().get(0).get("group_name_1"));
      var cachedProjection = repeatedProjection.cache();
      assertEquals(repeatedProjection.columns(), cachedProjection.columns());
      assertEquals(cachedProjection.columns(), cachedProjection.schema().stream()
          .map(io.zingg.duckdb.api.Column::name).toList());
      assertEquals(repeatedProjection.collect(), cachedProjection.collect());
      var unionProjection = repeatedProjection.union(repeatedProjection, false, false);
      assertEquals(repeatedProjection.columns(), unionProjection.columns());
      assertEquals(6, unionProjection.count());
      var projectedJoin = repeatedProjection.joinProjected(
          job.sql("SELECT 'a' AS group_name, 'right-value' AS label"),
          "l.group_name = r.group_name", "r_");
      assertEquals(List.of("group_name", "group_name_1", "r_group_name", "r_label"),
          projectedJoin.columns());
      assertEquals(projectedJoin.columns(), projectedJoin.schema().stream()
          .map(io.zingg.duckdb.api.Column::name).toList());
      assertEquals("right-value", projectedJoin.collect().get(0).get("r_label"));
      assertThrows(io.zingg.duckdb.api.DuckException.class,
          () -> job.sql("SELECT 'a' AS r_id")
              .joinProjected(job.sql("SELECT 'a' AS id"), "l.r_id = r.id", "R_"));
    }
  }

  @Test
  void zeroColumnProjectionPreservesMultiplicityAndHidesPhysicalSentinel(@TempDir Path temp) {
    var config = new io.zingg.duckdb.api.RuntimeConfig(
        "jdbc:duckdb:" + temp.resolve("zero-column.duckdb"), 1, 0, null, 1);
    try (var runtime = new DuckRuntime(config); var job = runtime.openJob()) {
      var source = job.sql("SELECT * FROM (VALUES (1, 'x'), (1, 'x'), (2, 'y')) t(id, block)");
      var empty = source.selectZeroColumns();
      assertEquals(List.of(), empty.columns());
      assertEquals(List.of(), empty.schema());
      assertEquals(3, empty.count());
      assertEquals(3, empty.collect().size());
      assertEquals(List.of(), empty.collect().get(0).columns());
      assertEquals(List.of(), empty.collect().get(0).values());
      org.junit.jupiter.api.Assertions.assertThrows(io.zingg.duckdb.api.DuckException.class,
          () -> empty.writeCsv(temp.resolve("zero-columns.csv"), true));
      org.junit.jupiter.api.Assertions.assertFalse(java.nio.file.Files.exists(temp.resolve("zero-columns.csv")));
      org.junit.jupiter.api.Assertions.assertThrows(io.zingg.duckdb.api.DuckException.class,
          () -> empty.writeArrow(temp.resolve("zero-columns.arrow")));
      org.junit.jupiter.api.Assertions.assertFalse(java.nio.file.Files.exists(temp.resolve("zero-columns.arrow")));
      assertEquals(3, empty.filter(new DuckExpr("TRUE")).count());
      assertEquals(2, empty.limit(2).count());
      assertEquals(3, empty.sample(false, 1d).count());
      var replacementSample = empty.sample(true, 0.5d);
      assertEquals(List.of(), replacementSample.columns());
      org.junit.jupiter.api.Assertions.assertTrue(replacementSample.collect().stream()
          .allMatch(row -> row.columns().isEmpty() && row.values().isEmpty()));
      assertEquals(3, empty.cache().count());
      assertEquals(1, empty.distinct().count(), "all zero-column rows compare equal for distinct");
      assertEquals(3, source.drop("id", "block").count());
      var added = empty.withColumn("constant", 7);
      assertEquals(List.of("constant"), added.columns());
      assertEquals(3, added.count());
      assertEquals(7, ((Number) added.collect().get(0).get("constant")).intValue());
      var union = empty.union(empty, false, false);
      assertEquals(List.of(), union.columns());
      assertEquals(6, union.count());
      var byNameUnion = empty.union(source.select("block"), true, true);
      assertEquals(List.of("block"), byNameUnion.columns());
      assertEquals(6, byNameUnion.count());
      assertEquals(3L, byNameUnion.collect().stream().filter(row -> row.get("block") == null).count());
      var zeroJoin = empty.join(empty, "TRUE");
      assertEquals(List.of(), zeroJoin.columns());
      assertEquals(List.of(), zeroJoin.schema());
      assertEquals(9, zeroJoin.count());
      var zeroJoinRows = zeroJoin.collect();
      assertEquals(9, zeroJoinRows.size());
      org.junit.jupiter.api.Assertions.assertTrue(zeroJoinRows.stream()
          .allMatch(row -> row.columns().isEmpty() && row.values().isEmpty()));
      var projectedJoin = empty.join(source.select("block"), "TRUE");
      assertEquals(List.of("block"), projectedJoin.columns());
      assertEquals(List.of("block"), projectedJoin.schema().stream()
          .map(io.zingg.duckdb.api.Column::name).toList());
      assertEquals(9, projectedJoin.count());
      assertEquals(9, projectedJoin.collect().size());
      var zeroProjectedJoin = empty.joinProjected(source.select("block"), "TRUE", "r_");
      assertEquals(List.of("r_block"), zeroProjectedJoin.columns());
      assertEquals(9, zeroProjectedJoin.collect().size());
      assertEquals(1, empty.intersect(empty).count());
      assertEquals(0, empty.except(empty).count());
    }
  }

  @Test
  void withColumnEvaluatesPublicExpressionValuesInsteadOfSerializingThemAsLiterals(@TempDir Path temp) {
    var config = new io.zingg.duckdb.api.RuntimeConfig(
        "jdbc:duckdb:" + temp.resolve("with-column-expression.duckdb"), 1, 0, null, 1);
    try (var runtime = new DuckRuntime(config); var job = runtime.openJob()) {
      var frame = job.sql("SELECT 1 AS value UNION ALL SELECT 4");
      io.zingg.duckdb.api.Expression increment = () -> "\"value\" + 1";
      var rows = frame.withColumn("incremented", increment).sort("value ASC").collect();
      assertEquals(2, rows.size());
      assertEquals(2, ((Number) rows.get(0).get("incremented")).intValue());
      assertEquals(5, ((Number) rows.get(1).get("incremented")).intValue());
    }
  }

  @Test
  void cacheMaterializesAndRemainsComposable(@TempDir Path temp) {
    var config = new io.zingg.duckdb.api.RuntimeConfig(
        "jdbc:duckdb:" + temp.resolve("cache.duckdb"), 1, 0, null, 1);
    try (var runtime = new DuckRuntime(config); var job = runtime.openJob()) {
      var cached = job.sql("select * from range(3) t(value)").cache();
      assertEquals(3, cached.count());
      assertEquals(3, cached.select("value").count());
    }
  }

  @Test
  void collectEnforcesByteBudget(@TempDir Path temp) {
    var config = new io.zingg.duckdb.api.RuntimeConfig(
        "jdbc:duckdb:" + temp.resolve("collect-budget.duckdb"), 1, 0, null, 1);
    try (var runtime = new DuckRuntime(config);
         var job = runtime.openJob(new ResourceBudget(0, 0, 0, 0, 16, 0))) {
      org.junit.jupiter.api.Assertions.assertThrows(io.zingg.duckdb.api.DuckException.class,
          () -> job.sql("select 'a very long value' as value").collect());
    }
  }

  @Test
  void collectByteBudgetIsInclusiveUtf8AwareAndRejectsBeforeRetainingNextRow(@TempDir Path temp) {
    var config = new io.zingg.duckdb.api.RuntimeConfig(
        "jdbc:duckdb:" + temp.resolve("collect-byte-boundary.duckdb"), 1, 0, null, 1);
    try (var runtime = new DuckRuntime(config);
         var job = runtime.openJob(new ResourceBudget(0, 0, 0, 0, 11, 0))) {
      assertEquals(1, job.sql("select 'é'").collect().size(),
          "UTF-8 payload is two bytes plus one delimiter and eight bytes row overhead");
      var failure = org.junit.jupiter.api.Assertions.assertThrows(io.zingg.duckdb.api.DuckException.class,
          () -> job.sql("select 'éa'").collect());
      assertEquals("collect byte budget exceeded: 12 > 11", failure.getMessage());
      assertEquals(1, job.sql("select 'x'").collect().size(),
          "rejecting an over-budget row must cancel/close only that statement");
    }
  }

  @Test
  void collectByteBudgetCountsBinaryPayloadAndNullColumns(@TempDir Path temp) throws Exception {
    var config = new io.zingg.duckdb.api.RuntimeConfig(
        "jdbc:duckdb:" + temp.resolve("collect-binary-budget.duckdb"), 1, 0, null, 1);
    try (var runtime = new DuckRuntime(config);
         var job = runtime.openJob(new ResourceBudget(0, 0, 0, 0, 12, 0))) {
      var exact = job.sql("select from_hex('000102') as payload").collect();
      assertEquals(1, exact.size(), "three binary bytes, one cell delimiter, and row overhead");
      assertEquals(3, binaryBytes(exact.get(0).get("payload")).length);

      var failure = org.junit.jupiter.api.Assertions.assertThrows(io.zingg.duckdb.api.DuckException.class,
          () -> job.sql("select from_hex('00010203') as payload").collect());
      assertEquals("collect byte budget exceeded: 13 > 12", failure.getMessage());
      assertEquals(null, job.sql("select NULL::VARCHAR as absent").collect().get(0).get("absent"));
      assertEquals(1, job.sql("select 1 as value").collect().size(),
          "a binary-value budget rejection must leave the job usable");
    }
  }

  @Test
  void collectByteBudgetFailsClosedForComplexJdbcValues(@TempDir Path temp) {
    var config = new io.zingg.duckdb.api.RuntimeConfig(
        "jdbc:duckdb:" + temp.resolve("collect-complex-budget.duckdb"), 1, 0, null, 1);
    try (var runtime = new DuckRuntime(config);
         var job = runtime.openJob(new ResourceBudget(0, 0, 0, 0, 1024, 0))) {
      var failure = assertThrows(io.zingg.duckdb.api.DuckException.class,
          () -> job.sql("select [repeat('x', 4096)] as nested").collect());
      assertTrue(failure.getMessage().contains("cannot account for collected JDBC value"));
      assertTrue(failure.getCause().getMessage().contains("unsupported JDBC value type"));
      assertEquals(1, job.sql("select 1 as value").collect().size(),
          "fail-closed type handling must leave the job usable");
    }
  }

  @Test
  void zinggSplitCreatesStringArrayColumn(@TempDir Path temp) {
    var config = new io.zingg.duckdb.api.RuntimeConfig(
        "jdbc:duckdb:" + temp.resolve("split.duckdb"), 1, 0, null, 1);
    try (var runtime = new DuckRuntime(config); var job = runtime.openJob()) {
      var split = job.sql("select 'a|b' as value").split("value", "|", "parts");
      assertEquals(java.util.List.of("parts"), split.columns());
      assertEquals("[a, b]", String.valueOf(split.collect().get(0).get("parts")));
      var caseVariantSplit = job.sql("select 'a|b' as value").split("VALUE", "|", "Parts");
      assertEquals(List.of("Parts"), caseVariantSplit.columns());
      assertEquals("[a, b]", String.valueOf(caseVariantSplit.collect().get(0).get("Parts")));
      assertThrows(io.zingg.duckdb.api.DuckException.class,
          () -> job.sql("select 'a|b' as value, 'keep' as label")
              .split("value", "|", "LABEL"));
    }
  }

  @Test
  void arrowEgressUsesBuiltInDuckdbWriterOffline(@TempDir Path temp) throws Exception {
    var config = new io.zingg.duckdb.api.RuntimeConfig(
        "jdbc:duckdb:" + temp.resolve("arrow.duckdb"), 1, 0, null, 1);
    Path output = temp.resolve("rows.arrow");
    try (var runtime = new DuckRuntime(config); var job = runtime.openJob()) {
      job.sql("select 1 as value, 'offline' as label").writeArrow(output);
      var roundTrip = new DuckPipeReader(job).read(java.util.List.of(output), DuckPipeReader.UnionMode.MATCH_BY_NAME);
      assertEquals(1, roundTrip.count());
      assertEquals(java.util.List.of("value", "label", "z_source", "z_zid"), roundTrip.columns());
    }
    assertEquals(true, java.nio.file.Files.exists(output));
    assertEquals(true, java.nio.file.Files.size(output) > 0);
    try (var allocator = new org.apache.arrow.memory.RootAllocator();
         var input = java.nio.file.Files.newInputStream(output);
         var reader = new org.apache.arrow.vector.ipc.ArrowStreamReader(input, allocator)) {
      var root = reader.getVectorSchemaRoot();
      assertEquals(true, reader.loadNextBatch());
      assertEquals(1, root.getRowCount());
      assertEquals("offline", String.valueOf(root.getVector("label").getObject(0)));
    }
  }

  @Test
  void arrowRoundTripHandlesNullsAndMultipleBatches(@TempDir Path temp) throws Exception {
    var config = new io.zingg.duckdb.api.RuntimeConfig(
        "jdbc:duckdb:" + temp.resolve("arrow-batches.duckdb"), 1, 0, null, 1);
    Path output = temp.resolve("batches.arrow");
    try (var runtime = new DuckRuntime(config); var job = runtime.openJob()) {
      job.sql("select i as value, case when i % 2 = 0 then cast(null as varchar) else 'x' end as label, i % 2 = 0 as flag, i::double + 0.5 as ratio from range(1025) t(i)").writeArrow(output);
      var jdbcPath = ArrowFileSupport.read((DuckJob) job, output);
      var appenderPath = ArrowFileSupport.readWithAppender((DuckJob) job, output);
      assertEquals(jdbcPath.schema(), appenderPath.schema());
      assertEquals(jdbcPath.collect(), appenderPath.collect(), "Appender must preserve nulls and every batch");
      var roundTrip = new DuckPipeReader(job).read(java.util.List.of(output), DuckPipeReader.UnionMode.MATCH_BY_NAME);
      assertEquals(1025, roundTrip.count());
      assertEquals(null, roundTrip.collect().get(0).get("label"));
      assertEquals("x", roundTrip.collect().get(1).get("label"));
    }
  }

  @Test
  void emptyArrowEgressPreservesSchemaAndImportsZeroRowsThroughBothPaths(@TempDir Path temp)
      throws Exception {
    var config = new io.zingg.duckdb.api.RuntimeConfig(
        "jdbc:duckdb:" + temp.resolve("empty-arrow-egress.duckdb"), 1, 0, null, 1);
    Path output = temp.resolve("empty-result.arrow");
    try (var runtime = new DuckRuntime(config); var job = (DuckJob) runtime.openJob()) {
      var source = job.sql(
          "SELECT CAST(NULL AS VARCHAR) AS label, CAST(NULL AS BIGINT) AS identifier WHERE FALSE");
      var expectedSchema = source.schema();
      assertEquals(0, source.count());
      source.writeArrow(output);

      int emittedRows = 0;
      try (var allocator = new org.apache.arrow.memory.RootAllocator();
           var stream = java.nio.file.Files.newInputStream(output);
           var reader = new org.apache.arrow.vector.ipc.ArrowStreamReader(stream, allocator)) {
        assertEquals(List.of("label", "identifier"),
            reader.getVectorSchemaRoot().getSchema().getFields().stream()
                .map(org.apache.arrow.vector.types.pojo.Field::getName).toList());
        while (reader.loadNextBatch()) emittedRows += reader.getVectorSchemaRoot().getRowCount();
      }
      assertEquals(0, emittedRows, "empty Arrow egress must not invent a row");

      var jdbcFrame = ArrowFileSupport.read(job, output);
      var appenderFrame = ArrowFileSupport.readWithAppender(job, output);
      assertEquals(expectedSchema, jdbcFrame.schema());
      assertEquals(expectedSchema, appenderFrame.schema());
      assertEquals(List.of(), jdbcFrame.collect());
      assertEquals(List.of(), appenderFrame.collect());
    }
    assertTrue(java.nio.file.Files.isRegularFile(output));
    try (var files = java.nio.file.Files.list(temp)) {
      assertTrue(files.noneMatch(path -> path.getFileName().toString().endsWith(".partial")),
          "empty export/import must not leave staging files");
    }
  }

  @Test
  void arrowDictionaryNullabilityCombinesIndexAndValueFieldContracts(@TempDir Path temp)
      throws Exception {
    Path input = temp.resolve("dictionary-nullability.arrow");
    var indexType = new org.apache.arrow.vector.types.pojo.ArrowType.Int(8, true);
    var valueType = new org.apache.arrow.vector.types.pojo.ArrowType.Int(32, true);
    var nullableIndexEncoding = new org.apache.arrow.vector.types.pojo.DictionaryEncoding(91L, false, indexType);
    var requiredEncoding = new org.apache.arrow.vector.types.pojo.DictionaryEncoding(92L, false, indexType);
    var fields = List.of(
        new org.apache.arrow.vector.types.pojo.Field("nullable_index",
            new org.apache.arrow.vector.types.pojo.FieldType(true, indexType, nullableIndexEncoding, null), null),
        new org.apache.arrow.vector.types.pojo.Field("required_value",
            new org.apache.arrow.vector.types.pojo.FieldType(false, indexType, requiredEncoding, null), null));
    try (var allocator = new org.apache.arrow.memory.RootAllocator();
         var root = org.apache.arrow.vector.VectorSchemaRoot.create(
             new org.apache.arrow.vector.types.pojo.Schema(fields), allocator);
         var nonNullIndexValues = new org.apache.arrow.vector.IntVector("index-values",
             new org.apache.arrow.vector.types.pojo.FieldType(false, valueType, null, null), allocator);
         var requiredDictionaryValues = new org.apache.arrow.vector.IntVector("required-values",
             new org.apache.arrow.vector.types.pojo.FieldType(false, valueType, null, null), allocator);
         var channel = java.nio.channels.Channels.newChannel(java.nio.file.Files.newOutputStream(input))) {
      nonNullIndexValues.allocateNew();
      nonNullIndexValues.setSafe(0, 11); nonNullIndexValues.setValueCount(1);
      requiredDictionaryValues.allocateNew();
      requiredDictionaryValues.setSafe(0, 33); requiredDictionaryValues.setSafe(1, 44);
      requiredDictionaryValues.setValueCount(2);
      try (var dictionaries = new org.apache.arrow.vector.dictionary.DictionaryProvider.MapDictionaryProvider(
               new org.apache.arrow.vector.dictionary.Dictionary(nonNullIndexValues, nullableIndexEncoding),
               new org.apache.arrow.vector.dictionary.Dictionary(requiredDictionaryValues, requiredEncoding));
           var writer = new org.apache.arrow.vector.ipc.ArrowStreamWriter(root, dictionaries, channel)) {
        var nullableIndex = (org.apache.arrow.vector.TinyIntVector) root.getVector("nullable_index");
        nullableIndex.setSafe(0, (byte) 0); nullableIndex.setNull(1); nullableIndex.setValueCount(2);
        var required = (org.apache.arrow.vector.TinyIntVector) root.getVector("required_value");
        required.setSafe(0, (byte) 0); required.setSafe(1, (byte) 1); required.setValueCount(2);
        root.setRowCount(2);
        writer.start(); writer.writeBatch(); writer.end();
      }
    }

    var config = new io.zingg.duckdb.api.RuntimeConfig(
        "jdbc:duckdb:" + temp.resolve("dictionary-nullability.duckdb"), 1, 0, null, 1);
    try (var runtime = new DuckRuntime(config); var job = (DuckJob) runtime.openJob()) {
      for (boolean useAppender : new boolean[] {false, true}) {
        var frame = ArrowFileSupport.read(job, input, useAppender, importedRows -> {});
        assertEquals(java.util.Arrays.asList(11, null), frame.collect().stream()
            .map(row -> row.get("nullable_index")).toList());
        assertEquals(List.of(33, 44), frame.collect().stream()
            .map(row -> row.get("required_value")).toList());
      }
      assertEquals(2L, ((Number) job.sql(
          "select count(*) as n from information_schema.columns where table_schema='" + job.schemaName()
              + "' and table_name like 'zd_arrow_%' and column_name='nullable_index' and is_nullable='YES'")
          .collect().get(0).get("n")).longValue());
      assertEquals(2L, ((Number) job.sql(
          "select count(*) as n from information_schema.columns where table_schema='" + job.schemaName()
              + "' and table_name like 'zd_arrow_%' and column_name='required_value' and is_nullable='NO'")
          .collect().get(0).get("n")).longValue());
    }
  }

  @Test
  void arrowDictionaryEncodedValuesDecodeIdenticallyThroughJdbcAndAppender(@TempDir Path temp)
      throws Exception {
    Path input = temp.resolve("dictionary-encoded.arrow");
    try (var allocator = new org.apache.arrow.memory.RootAllocator()) {
      var indexType = new org.apache.arrow.vector.types.pojo.ArrowType.Int(8, true);
      var encoding = new org.apache.arrow.vector.types.pojo.DictionaryEncoding(7L, false, indexType);
      var categoryField = new org.apache.arrow.vector.types.pojo.Field("category",
          new org.apache.arrow.vector.types.pojo.FieldType(true, indexType, encoding, null), null);
      try (var root = org.apache.arrow.vector.VectorSchemaRoot.create(
              new org.apache.arrow.vector.types.pojo.Schema(List.of(categoryField)), allocator);
          var dictionaryVector = new org.apache.arrow.vector.VarCharVector("category-values", allocator);
          var channel = java.nio.channels.Channels.newChannel(java.nio.file.Files.newOutputStream(input))) {
        dictionaryVector.allocateNew();
        dictionaryVector.setSafe(0, "bronze".getBytes(java.nio.charset.StandardCharsets.UTF_8));
        dictionaryVector.setNull(1);
        dictionaryVector.setSafe(2, "gold".getBytes(java.nio.charset.StandardCharsets.UTF_8));
        dictionaryVector.setValueCount(3);
        try (var dictionaries = new org.apache.arrow.vector.dictionary.DictionaryProvider.MapDictionaryProvider(
                 new org.apache.arrow.vector.dictionary.Dictionary(dictionaryVector, encoding));
             var writer = new org.apache.arrow.vector.ipc.ArrowStreamWriter(root, dictionaries, channel)) {
          var indices = (org.apache.arrow.vector.TinyIntVector) root.getVector("category");
          indices.allocateNew();
          byte[] ids = {2, 0, 1, 2, 0, 1};
          for (int row = 0; row < ids.length; row++) indices.setSafe(row, ids[row]);
          indices.setNull(5);
          indices.setValueCount(ids.length);
          root.setRowCount(ids.length);
          writer.start();
          writer.writeBatch();
          writer.end();
        }
      }
    }

    var config = new io.zingg.duckdb.api.RuntimeConfig(
        "jdbc:duckdb:" + temp.resolve("dictionary-roundtrip.duckdb"), 1, 0, null, 1);
    try (var runtime = new DuckRuntime(config); var job = (DuckJob) runtime.openJob()) {
      var jdbc = ArrowFileSupport.read(job, input);
      var appender = ArrowFileSupport.readWithAppender(job, input);
      assertEquals(List.of("category"), jdbc.columns());
      assertEquals("VARCHAR", jdbc.schema().get(0).typeName());
      assertEquals(java.util.Arrays.asList("gold", "bronze", null, "gold", "bronze", null),
          jdbc.collect().stream().map(row -> row.get("category")).toList());
      assertEquals(jdbc.schema(), appender.schema());
      assertEquals(jdbc.collect(), appender.collect());
    }
  }

  @Test
  void invalidArrowDictionaryIndexFailsAndCleansStagingTable(@TempDir Path temp) throws Exception {
    Path input = temp.resolve("dictionary-invalid-index.arrow");
    var indexType = new org.apache.arrow.vector.types.pojo.ArrowType.Int(8, true);
    var encoding = new org.apache.arrow.vector.types.pojo.DictionaryEncoding(9L, false, indexType);
    var field = new org.apache.arrow.vector.types.pojo.Field("category",
        new org.apache.arrow.vector.types.pojo.FieldType(true, indexType, encoding, null), null);
    try (var allocator = new org.apache.arrow.memory.RootAllocator();
         var root = org.apache.arrow.vector.VectorSchemaRoot.create(
             new org.apache.arrow.vector.types.pojo.Schema(List.of(field)), allocator);
         var dictionaryVector = new org.apache.arrow.vector.VarCharVector("category-values", allocator);
         var channel = java.nio.channels.Channels.newChannel(java.nio.file.Files.newOutputStream(input))) {
      dictionaryVector.allocateNew();
      dictionaryVector.setSafe(0, "only-value".getBytes(java.nio.charset.StandardCharsets.UTF_8));
      dictionaryVector.setValueCount(1);
      try (var dictionaries = new org.apache.arrow.vector.dictionary.DictionaryProvider.MapDictionaryProvider(
               new org.apache.arrow.vector.dictionary.Dictionary(dictionaryVector, encoding));
           var writer = new org.apache.arrow.vector.ipc.ArrowStreamWriter(root, dictionaries, channel)) {
        ((org.apache.arrow.vector.TinyIntVector) root.getVector("category")).setSafe(0, (byte) 9);
        root.getVector("category").setValueCount(1);
        root.setRowCount(1);
        writer.start();
        writer.writeBatch();
        writer.end();
      }
    }

    var config = new io.zingg.duckdb.api.RuntimeConfig(
        "jdbc:duckdb:" + temp.resolve("dictionary-invalid.duckdb"), 1, 0, null, 1);
    try (var runtime = new DuckRuntime(config); var job = (DuckJob) runtime.openJob()) {
      int registeredBefore = job.materializedCount();
      for (boolean appender : List.of(false, true)) {
        var error = org.junit.jupiter.api.Assertions.assertThrows(io.zingg.duckdb.api.DuckException.class,
            () -> {
              if (appender) ArrowFileSupport.readWithAppender(job, input);
              else ArrowFileSupport.read(job, input);
            });
        assertEquals(true, error.getMessage().contains("dictionary index is out of range: 9"));
        assertEquals(registeredBefore, job.materializedCount());
        assertEquals(0L, job.sql("select count(*) as n from information_schema.tables where table_name like 'zd_arrow_%'")
            .collect().get(0).get("n"));
      }
    }
  }

  @Test
  void arrowIntegerAndBooleanDictionariesDecodeThroughJdbcAndAppender(@TempDir Path temp)
      throws Exception {
    Path input = temp.resolve("dictionary-integer-boolean.arrow");
    var indexType = new org.apache.arrow.vector.types.pojo.ArrowType.Int(8, true);
    var integerEncoding = new org.apache.arrow.vector.types.pojo.DictionaryEncoding(11L, false, indexType);
    var booleanEncoding = new org.apache.arrow.vector.types.pojo.DictionaryEncoding(12L, false, indexType);
    var integerField = new org.apache.arrow.vector.types.pojo.Field("rank",
        new org.apache.arrow.vector.types.pojo.FieldType(true, indexType, integerEncoding, null), null);
    var booleanField = new org.apache.arrow.vector.types.pojo.Field("enabled",
        new org.apache.arrow.vector.types.pojo.FieldType(true, indexType, booleanEncoding, null), null);
    try (var allocator = new org.apache.arrow.memory.RootAllocator();
         var root = org.apache.arrow.vector.VectorSchemaRoot.create(
             new org.apache.arrow.vector.types.pojo.Schema(List.of(integerField, booleanField)), allocator);
         var integerDictionary = new org.apache.arrow.vector.IntVector("rank-values", allocator);
         var booleanDictionary = new org.apache.arrow.vector.BitVector("enabled-values", allocator);
         var channel = java.nio.channels.Channels.newChannel(java.nio.file.Files.newOutputStream(input))) {
      integerDictionary.allocateNew();
      integerDictionary.setSafe(0, 40);
      integerDictionary.setSafe(1, 7);
      integerDictionary.setValueCount(2);
      booleanDictionary.allocateNew();
      booleanDictionary.setSafe(0, 1);
      booleanDictionary.setSafe(1, 0);
      booleanDictionary.setValueCount(2);
      try (var dictionaries = new org.apache.arrow.vector.dictionary.DictionaryProvider.MapDictionaryProvider(
               new org.apache.arrow.vector.dictionary.Dictionary(integerDictionary, integerEncoding),
               new org.apache.arrow.vector.dictionary.Dictionary(booleanDictionary, booleanEncoding));
           var writer = new org.apache.arrow.vector.ipc.ArrowStreamWriter(root, dictionaries, channel)) {
        var ranks = (org.apache.arrow.vector.TinyIntVector) root.getVector("rank");
        var enabled = (org.apache.arrow.vector.TinyIntVector) root.getVector("enabled");
        ranks.allocateNew();
        enabled.allocateNew();
        byte[] ids = {1, 0, 1};
        for (int row = 0; row < ids.length; row++) {
          ranks.setSafe(row, ids[row]);
          enabled.setSafe(row, ids[ids.length - row - 1]);
        }
        ranks.setValueCount(ids.length);
        enabled.setValueCount(ids.length);
        root.setRowCount(ids.length);
        writer.start();
        writer.writeBatch();
        writer.end();
      }
    }

    var config = new io.zingg.duckdb.api.RuntimeConfig(
        "jdbc:duckdb:" + temp.resolve("dictionary-integer-boolean.duckdb"), 1, 0, null, 1);
    try (var runtime = new DuckRuntime(config); var job = (DuckJob) runtime.openJob()) {
      var jdbc = ArrowFileSupport.read(job, input);
      var appender = ArrowFileSupport.readWithAppender(job, input);
      assertEquals(List.of("INTEGER", "BOOLEAN"), jdbc.schema().stream()
          .map(io.zingg.duckdb.api.Column::typeName).toList());
      assertEquals(List.of(7, 40, 7), jdbc.collect().stream()
          .map(row -> ((Number) row.get("rank")).intValue()).toList());
      assertEquals(List.of(false, true, false), jdbc.collect().stream()
          .map(row -> row.get("enabled")).toList());
      assertEquals(jdbc.schema(), appender.schema());
      assertEquals(jdbc.collect(), appender.collect());
    }
  }

  @Test
  void arrowDecimalAndDateDictionariesDecodeThroughJdbcAndAppender(@TempDir Path temp)
      throws Exception {
    Path input = temp.resolve("dictionary-decimal-date.arrow");
    var indexType = new org.apache.arrow.vector.types.pojo.ArrowType.Int(8, true);
    var decimalEncoding = new org.apache.arrow.vector.types.pojo.DictionaryEncoding(21L, false, indexType);
    var dateEncoding = new org.apache.arrow.vector.types.pojo.DictionaryEncoding(22L, false, indexType);
    var decimalField = new org.apache.arrow.vector.types.pojo.Field("amount",
        new org.apache.arrow.vector.types.pojo.FieldType(true, indexType, decimalEncoding, null), null);
    var dateField = new org.apache.arrow.vector.types.pojo.Field("created",
        new org.apache.arrow.vector.types.pojo.FieldType(true, indexType, dateEncoding, null), null);
    try (var allocator = new org.apache.arrow.memory.RootAllocator();
         var root = org.apache.arrow.vector.VectorSchemaRoot.create(
             new org.apache.arrow.vector.types.pojo.Schema(List.of(decimalField, dateField)), allocator);
         var decimalDictionary = new org.apache.arrow.vector.DecimalVector("amount-values",
             org.apache.arrow.vector.types.pojo.FieldType.nullable(
                 new org.apache.arrow.vector.types.pojo.ArrowType.Decimal(8, 2, 128)), allocator);
         var dateDictionary = new org.apache.arrow.vector.DateDayVector("created-values", allocator);
         var channel = java.nio.channels.Channels.newChannel(java.nio.file.Files.newOutputStream(input))) {
      decimalDictionary.allocateNew();
      decimalDictionary.setSafe(0, new java.math.BigDecimal("12.34"));
      decimalDictionary.setSafe(1, new java.math.BigDecimal("-0.50"));
      decimalDictionary.setValueCount(2);
      dateDictionary.allocateNew();
      dateDictionary.setSafe(0, 20_000);
      dateDictionary.setSafe(1, 20_001);
      dateDictionary.setValueCount(2);
      try (var dictionaries = new org.apache.arrow.vector.dictionary.DictionaryProvider.MapDictionaryProvider(
               new org.apache.arrow.vector.dictionary.Dictionary(decimalDictionary, decimalEncoding),
               new org.apache.arrow.vector.dictionary.Dictionary(dateDictionary, dateEncoding));
           var writer = new org.apache.arrow.vector.ipc.ArrowStreamWriter(root, dictionaries, channel)) {
        var amounts = (org.apache.arrow.vector.TinyIntVector) root.getVector("amount");
        var dates = (org.apache.arrow.vector.TinyIntVector) root.getVector("created");
        amounts.allocateNew();
        dates.allocateNew();
        amounts.setSafe(0, (byte) 1);
        amounts.setSafe(1, (byte) 0);
        dates.setSafe(0, (byte) 0);
        dates.setSafe(1, (byte) 1);
        amounts.setValueCount(2);
        dates.setValueCount(2);
        root.setRowCount(2);
        writer.start();
        writer.writeBatch();
        writer.end();
      }
    }

    var config = new io.zingg.duckdb.api.RuntimeConfig(
        "jdbc:duckdb:" + temp.resolve("dictionary-decimal-date.duckdb"), 1, 0, null, 1);
    try (var runtime = new DuckRuntime(config); var job = (DuckJob) runtime.openJob()) {
      var jdbc = ArrowFileSupport.read(job, input);
      var appender = ArrowFileSupport.readWithAppender(job, input);
      assertEquals(List.of("DECIMAL(8,2)", "DATE"), jdbc.schema().stream()
          .map(io.zingg.duckdb.api.Column::typeName).toList());
      assertEquals(List.of(new java.math.BigDecimal("-0.50"), new java.math.BigDecimal("12.34")),
          jdbc.collect().stream().map(row -> row.get("amount")).toList());
      assertEquals(List.of(java.time.LocalDate.ofEpochDay(20_000), java.time.LocalDate.ofEpochDay(20_001)),
          jdbc.collect().stream().map(row -> row.get("created")).toList());
      assertEquals(jdbc.schema(), appender.schema());
      assertEquals(jdbc.collect(), appender.collect());
    }
  }

  @Test
  void arrowTimezoneTimestampDictionaryPreservesInstantsThroughJdbcAndAppender(@TempDir Path temp)
      throws Exception {
    Path input = temp.resolve("dictionary-timestamp-tz.arrow");
    var indexType = new org.apache.arrow.vector.types.pojo.ArrowType.Int(8, true);
    var encoding = new org.apache.arrow.vector.types.pojo.DictionaryEncoding(31L, false, indexType);
    var timestampType = new org.apache.arrow.vector.types.pojo.ArrowType.Timestamp(
        org.apache.arrow.vector.types.TimeUnit.MICROSECOND, "America/Los_Angeles");
    var field = new org.apache.arrow.vector.types.pojo.Field("occurred_at",
        new org.apache.arrow.vector.types.pojo.FieldType(true, indexType, encoding, null), null);
    try (var allocator = new org.apache.arrow.memory.RootAllocator();
         var root = org.apache.arrow.vector.VectorSchemaRoot.create(
             new org.apache.arrow.vector.types.pojo.Schema(List.of(field)), allocator);
         var timestamps = new org.apache.arrow.vector.TimeStampMicroTZVector("timestamp-values",
             org.apache.arrow.vector.types.pojo.FieldType.nullable(timestampType), allocator);
         var channel = java.nio.channels.Channels.newChannel(java.nio.file.Files.newOutputStream(input))) {
      timestamps.allocateNew();
      timestamps.setSafe(0, 1_700_000_000_123_456L);
      timestamps.setSafe(1, -1L);
      timestamps.setValueCount(2);
      try (var dictionaries = new org.apache.arrow.vector.dictionary.DictionaryProvider.MapDictionaryProvider(
               new org.apache.arrow.vector.dictionary.Dictionary(timestamps, encoding));
           var writer = new org.apache.arrow.vector.ipc.ArrowStreamWriter(root, dictionaries, channel)) {
        var indices = (org.apache.arrow.vector.TinyIntVector) root.getVector("occurred_at");
        indices.allocateNew();
        indices.setSafe(0, (byte) 0);
        indices.setSafe(1, (byte) 1);
        indices.setValueCount(2);
        root.setRowCount(2);
        writer.start();
        writer.writeBatch();
        writer.end();
      }
    }

    var config = new io.zingg.duckdb.api.RuntimeConfig(
        "jdbc:duckdb:" + temp.resolve("dictionary-timestamp-tz.duckdb"), 1, 0, null, 1);
    try (var runtime = new DuckRuntime(config); var job = (DuckJob) runtime.openJob()) {
      var jdbc = ArrowFileSupport.read((DuckJob) job, input);
      var appender = ArrowFileSupport.readWithAppender((DuckJob) job, input);
      assertEquals("TIMESTAMP WITH TIME ZONE", jdbc.schema().get(0).typeName());
      var rows = jdbc.collect();
      assertEquals(java.time.Instant.parse("2023-11-14T22:13:20.123456Z"),
          ((java.time.OffsetDateTime) rows.get(0).get("occurred_at")).toInstant());
      assertEquals(java.time.Instant.parse("1969-12-31T23:59:59.999999Z"),
          ((java.time.OffsetDateTime) rows.get(1).get("occurred_at")).toInstant());
      assertEquals(jdbc.schema(), appender.schema());
      assertEquals(rows, appender.collect());
    }
  }

  @Test
  void arrowHalfPrecisionDictionaryIsExpandedBeforeJdbcAndAppenderImport(@TempDir Path temp)
      throws Exception {
    Path input = temp.resolve("dictionary-float16.arrow");
    var indexType = new org.apache.arrow.vector.types.pojo.ArrowType.Int(8, true);
    var encoding = new org.apache.arrow.vector.types.pojo.DictionaryEncoding(41L, false, indexType);
    var halfType = new org.apache.arrow.vector.types.pojo.ArrowType.FloatingPoint(
        org.apache.arrow.vector.types.FloatingPointPrecision.HALF);
    var field = new org.apache.arrow.vector.types.pojo.Field("measurement",
        new org.apache.arrow.vector.types.pojo.FieldType(true, indexType, encoding, null), null);
    try (var allocator = new org.apache.arrow.memory.RootAllocator();
         var root = org.apache.arrow.vector.VectorSchemaRoot.create(
             new org.apache.arrow.vector.types.pojo.Schema(List.of(field)), allocator);
         var values = new org.apache.arrow.vector.Float2Vector("measurement-values",
             org.apache.arrow.vector.types.pojo.FieldType.nullable(halfType), allocator);
         var channel = java.nio.channels.Channels.newChannel(java.nio.file.Files.newOutputStream(input))) {
      values.allocateNew();
      values.setSafe(0, (short) 0x3e00); // 1.5 in IEEE-754 binary16.
      values.setSafe(1, (short) 0xc080); // -2.25 in IEEE-754 binary16.
      values.setValueCount(2);
      try (var dictionaries = new org.apache.arrow.vector.dictionary.DictionaryProvider.MapDictionaryProvider(
               new org.apache.arrow.vector.dictionary.Dictionary(values, encoding));
           var writer = new org.apache.arrow.vector.ipc.ArrowStreamWriter(root, dictionaries, channel)) {
        var indices = (org.apache.arrow.vector.TinyIntVector) root.getVector("measurement");
        indices.allocateNew();
        indices.setSafe(0, (byte) 1);
        indices.setSafe(1, (byte) 0);
        indices.setValueCount(2);
        root.setRowCount(2);
        writer.start();
        writer.writeBatch();
        writer.end();
      }
    }

    var config = new io.zingg.duckdb.api.RuntimeConfig(
        "jdbc:duckdb:" + temp.resolve("dictionary-float16.duckdb"), 1, 0, null, 1);
    try (var runtime = new DuckRuntime(config); var job = (DuckJob) runtime.openJob()) {
      var jdbc = ArrowFileSupport.read((DuckJob) job, input);
      var appender = ArrowFileSupport.readWithAppender((DuckJob) job, input);
      assertEquals("FLOAT", jdbc.schema().get(0).typeName());
      assertEquals(List.of(-2.25f, 1.5f), jdbc.collect().stream()
          .map(row -> row.get("measurement")).toList());
      assertEquals(jdbc.schema(), appender.schema());
      assertEquals(jdbc.collect(), appender.collect());
    }
  }

  @Test
  void arrowLargeUtf8PreservesLongUnicodeAndNullValuesThroughJdbcAndAppender(@TempDir Path temp)
      throws Exception {
    var field = new org.apache.arrow.vector.types.pojo.Field("description",
        org.apache.arrow.vector.types.pojo.FieldType.nullable(
            new org.apache.arrow.vector.types.pojo.ArrowType.LargeUtf8()), null);
    String longUnicode = "duck🦆-Zingg-".repeat(12_000);
    List<String> expected = java.util.Arrays.asList(longUnicode, "Zingg—鸭—данные—🦆", null);
    for (boolean fileFormat : new boolean[] {false, true}) {
      Path input = temp.resolve("large-utf8-" + (fileFormat ? "file" : "stream") + ".arrow");
      try (var allocator = new org.apache.arrow.memory.RootAllocator();
           var root = org.apache.arrow.vector.VectorSchemaRoot.create(
               new org.apache.arrow.vector.types.pojo.Schema(List.of(field)), allocator);
           var channel = java.nio.channels.Channels.newChannel(java.nio.file.Files.newOutputStream(input))) {
        var vector = (org.apache.arrow.vector.LargeVarCharVector) root.getVector("description");
        vector.allocateNew();
        vector.setSafe(0, expected.get(0).getBytes(java.nio.charset.StandardCharsets.UTF_8));
        vector.setSafe(1, expected.get(1).getBytes(java.nio.charset.StandardCharsets.UTF_8));
        vector.setNull(2);
        vector.setValueCount(expected.size());
        root.setRowCount(expected.size());
        writeArrowBatch(root, channel, fileFormat);
      }
      var config = new io.zingg.duckdb.api.RuntimeConfig(
          "jdbc:duckdb:" + temp.resolve("large-utf8-" + fileFormat + ".duckdb"), 1, 0, null, 1);
      try (var runtime = new DuckRuntime(config); var job = (DuckJob) runtime.openJob()) {
        var jdbc = ArrowFileSupport.read(job, input);
        var appender = ArrowFileSupport.readWithAppender(job, input);
        assertEquals("VARCHAR", jdbc.schema().get(0).typeName());
        assertEquals(expected, jdbc.collect().stream().map(row -> (String) row.get("description")).toList());
        assertEquals(jdbc.schema(), appender.schema());
        assertEquals(jdbc.collect(), appender.collect());
      }
    }
  }

  @Test
  void arrowLargeBinaryPreservesLongEmptyAndNullValuesThroughJdbcAndAppender(@TempDir Path temp)
      throws Exception {
    var field = new org.apache.arrow.vector.types.pojo.Field("payload",
        org.apache.arrow.vector.types.pojo.FieldType.nullable(
            new org.apache.arrow.vector.types.pojo.ArrowType.LargeBinary()), null);
    byte[] largePayload = new byte[160_000];
    for (int i = 0; i < largePayload.length; i++) largePayload[i] = (byte) (i * 31 + 7);
    byte[][] expected = {largePayload, new byte[0], null};
    for (boolean fileFormat : new boolean[] {false, true}) {
      Path input = temp.resolve("large-binary-" + (fileFormat ? "file" : "stream") + ".arrow");
      try (var allocator = new org.apache.arrow.memory.RootAllocator();
           var root = org.apache.arrow.vector.VectorSchemaRoot.create(
               new org.apache.arrow.vector.types.pojo.Schema(List.of(field)), allocator);
           var channel = java.nio.channels.Channels.newChannel(java.nio.file.Files.newOutputStream(input))) {
        var vector = (org.apache.arrow.vector.LargeVarBinaryVector) root.getVector("payload");
        vector.allocateNew();
        vector.setSafe(0, largePayload);
        vector.setSafe(1, new byte[0]);
        vector.setNull(2);
        vector.setValueCount(expected.length);
        root.setRowCount(expected.length);
        writeArrowBatch(root, channel, fileFormat);
      }
      var config = new io.zingg.duckdb.api.RuntimeConfig(
          "jdbc:duckdb:" + temp.resolve("large-binary-" + fileFormat + ".duckdb"), 1, 0, null, 1);
      try (var runtime = new DuckRuntime(config); var job = (DuckJob) runtime.openJob()) {
        var jdbc = ArrowFileSupport.read(job, input);
        var appender = ArrowFileSupport.readWithAppender(job, input);
        assertEquals("BLOB", jdbc.schema().get(0).typeName());
        var jdbcRows = jdbc.collect();
        assertEquals(expected.length, jdbcRows.size());
        org.junit.jupiter.api.Assertions.assertArrayEquals(expected[0], binaryBytes(jdbcRows.get(0).get("payload")));
        org.junit.jupiter.api.Assertions.assertArrayEquals(expected[1], binaryBytes(jdbcRows.get(1).get("payload")));
        assertEquals(null, jdbcRows.get(2).get("payload"));
        assertEquals(jdbc.schema(), appender.schema());
        var appenderRows = appender.collect();
        org.junit.jupiter.api.Assertions.assertArrayEquals(expected[0], binaryBytes(appenderRows.get(0).get("payload")));
        org.junit.jupiter.api.Assertions.assertArrayEquals(expected[1], binaryBytes(appenderRows.get(1).get("payload")));
        assertEquals(null, appenderRows.get(2).get("payload"));
      }
    }
  }

  @Test
  void arrowBinaryDictionaryPreservesExactBytesThroughJdbcAndAppender(@TempDir Path temp)
      throws Exception {
    Path input = temp.resolve("dictionary-binary.arrow");
    var indexType = new org.apache.arrow.vector.types.pojo.ArrowType.Int(8, true);
    var encoding = new org.apache.arrow.vector.types.pojo.DictionaryEncoding(51L, false, indexType);
    var field = new org.apache.arrow.vector.types.pojo.Field("payload",
        new org.apache.arrow.vector.types.pojo.FieldType(true, indexType, encoding, null), null);
    byte[][] expected = {new byte[] {0, (byte) 0xff, 0x41}, new byte[0], new byte[] {(byte) 0x80, 0x00}};
    try (var allocator = new org.apache.arrow.memory.RootAllocator();
         var root = org.apache.arrow.vector.VectorSchemaRoot.create(
             new org.apache.arrow.vector.types.pojo.Schema(List.of(field)), allocator);
         var values = new org.apache.arrow.vector.VarBinaryVector("payload-values", allocator);
         var channel = java.nio.channels.Channels.newChannel(java.nio.file.Files.newOutputStream(input))) {
      values.allocateNew();
      for (int i = 0; i < expected.length; i++) values.setSafe(i, expected[i]);
      values.setValueCount(expected.length);
      try (var dictionaries = new org.apache.arrow.vector.dictionary.DictionaryProvider.MapDictionaryProvider(
               new org.apache.arrow.vector.dictionary.Dictionary(values, encoding));
           var writer = new org.apache.arrow.vector.ipc.ArrowStreamWriter(root, dictionaries, channel)) {
        var indices = (org.apache.arrow.vector.TinyIntVector) root.getVector("payload");
        indices.allocateNew();
        indices.setSafe(0, (byte) 2);
        indices.setSafe(1, (byte) 1);
        indices.setSafe(2, (byte) 0);
        indices.setValueCount(expected.length);
        root.setRowCount(expected.length);
        writer.start();
        writer.writeBatch();
        writer.end();
      }
    }

    var config = new io.zingg.duckdb.api.RuntimeConfig(
        "jdbc:duckdb:" + temp.resolve("dictionary-binary.duckdb"), 1, 0, null, 1);
    try (var runtime = new DuckRuntime(config); var job = (DuckJob) runtime.openJob()) {
      var jdbc = ArrowFileSupport.read((DuckJob) job, input);
      var appender = ArrowFileSupport.readWithAppender((DuckJob) job, input);
      assertEquals("BLOB", jdbc.schema().get(0).typeName());
      var rows = jdbc.collect();
      for (int row = 0; row < rows.size(); row++)
        org.junit.jupiter.api.Assertions.assertArrayEquals(expected[2 - row], binaryBytes(rows.get(row).get("payload")));
      assertEquals(jdbc.schema(), appender.schema());
      var appenderRows = appender.collect();
      for (int row = 0; row < appenderRows.size(); row++)
        org.junit.jupiter.api.Assertions.assertArrayEquals(binaryBytes(rows.get(row).get("payload")),
            binaryBytes(appenderRows.get(row).get("payload")));
    }
  }

  @Test
  void malformedArrowInputFailsWithoutPublishingAWorkTable(@TempDir Path temp) throws Exception {
    var config = new io.zingg.duckdb.api.RuntimeConfig(
        "jdbc:duckdb:" + temp.resolve("arrow-invalid.duckdb"), 1, 0, null, 1);
    Path input = temp.resolve("invalid.arrow");
    java.nio.file.Files.write(input, new byte[] {0x01, 0x02, 0x03, 0x04});
    try (var runtime = new DuckRuntime(config); var job = (DuckJob) runtime.openJob()) {
      var reader = new DuckPipeReader(job, null, new ResourceBudget(0, 0, 100, 0, 0, 0));
      org.junit.jupiter.api.Assertions.assertThrows(io.zingg.duckdb.api.DuckException.class,
          () -> reader.read(java.util.List.of(input), DuckPipeReader.UnionMode.MATCH_BY_NAME));
      assertNoInputSnapshotDirectories(job);
    }
  }

  @Test
  void truncatedArrowFileFooterFailsBeforeCreatingWorkTable(@TempDir Path temp) throws Exception {
    Path input = temp.resolve("truncated-footer.arrow");
    try (var allocator = new org.apache.arrow.memory.RootAllocator();
         var root = org.apache.arrow.vector.VectorSchemaRoot.create(
             new org.apache.arrow.vector.types.pojo.Schema(List.of(
                 new org.apache.arrow.vector.types.pojo.Field("value",
                     org.apache.arrow.vector.types.pojo.FieldType.nullable(
                         new org.apache.arrow.vector.types.pojo.ArrowType.Int(32, true)), null))), allocator);
         var channel = java.nio.channels.Channels.newChannel(java.nio.file.Files.newOutputStream(input));
         var writer = new org.apache.arrow.vector.ipc.ArrowFileWriter(root, null, channel)) {
      var value = (org.apache.arrow.vector.IntVector) root.getVector("value");
      value.setSafe(0, 42);
      root.setRowCount(1);
      writer.start();
      writer.writeBatch();
      writer.end();
    }
    long validLength = java.nio.file.Files.size(input);
    assertTrue(validLength > 16, "fixture must contain an Arrow file body and footer");
    try (var channel = java.nio.channels.FileChannel.open(input, java.nio.file.StandardOpenOption.WRITE)) {
      channel.truncate(validLength - 8);
    }

    var config = new io.zingg.duckdb.api.RuntimeConfig(
        "jdbc:duckdb:" + temp.resolve("truncated-footer.duckdb"), 1, 0, null, 1);
    try (var runtime = new DuckRuntime(config); var job = (DuckJob) runtime.openJob()) {
      int registeredBefore = job.materializedCount();
      for (boolean useAppender : new boolean[] {false, true}) {
        assertThrows(io.zingg.duckdb.api.DuckException.class,
            () -> ArrowFileSupport.read(job, input, useAppender, importedRows -> {}));
        assertEquals(registeredBefore, job.materializedCount(),
            "invalid file footer must fail before registering a work table");
        assertEquals(0L, ((Number) job.sql(
            "select count(*) as n from information_schema.tables where table_schema='" + job.schemaName()
                + "' and table_name like 'zd_arrow_%'").collect().get(0).get("n")).longValue(),
            "invalid file footer must not leave a staging table");
      }
    }
  }

  @Test
  void unsupportedArrowTimeAndNestedComplexTypesFailBeforeCreatingWorkTables(@TempDir Path temp)
      throws Exception {
    var nested = new org.apache.arrow.vector.types.pojo.Field("items",
        org.apache.arrow.vector.types.pojo.FieldType.nullable(new org.apache.arrow.vector.types.pojo.ArrowType.List()),
        List.of(new org.apache.arrow.vector.types.pojo.Field("item",
            org.apache.arrow.vector.types.pojo.FieldType.nullable(
                new org.apache.arrow.vector.types.pojo.ArrowType.Int(32, true)), null)));
    var struct = new org.apache.arrow.vector.types.pojo.Field("record",
        org.apache.arrow.vector.types.pojo.FieldType.nullable(
            new org.apache.arrow.vector.types.pojo.ArrowType.Struct()),
        List.of(new org.apache.arrow.vector.types.pojo.Field("item",
            org.apache.arrow.vector.types.pojo.FieldType.nullable(
                new org.apache.arrow.vector.types.pojo.ArrowType.Int(32, true)), null)));
    var mapEntries = new org.apache.arrow.vector.types.pojo.Field("entries",
        new org.apache.arrow.vector.types.pojo.FieldType(false,
            new org.apache.arrow.vector.types.pojo.ArrowType.Struct(), null, null),
        List.of(new org.apache.arrow.vector.types.pojo.Field("key",
                new org.apache.arrow.vector.types.pojo.FieldType(false,
                    new org.apache.arrow.vector.types.pojo.ArrowType.Int(32, true), null, null), null),
            new org.apache.arrow.vector.types.pojo.Field("value",
                org.apache.arrow.vector.types.pojo.FieldType.nullable(
                    new org.apache.arrow.vector.types.pojo.ArrowType.Int(32, true)), null)));
    var map = new org.apache.arrow.vector.types.pojo.Field("mapping",
        org.apache.arrow.vector.types.pojo.FieldType.nullable(
            new org.apache.arrow.vector.types.pojo.ArrowType.Map(false)), List.of(mapEntries));
    var unsupportedFields = List.of(
        new org.apache.arrow.vector.types.pojo.Field("time",
            org.apache.arrow.vector.types.pojo.FieldType.nullable(
                new org.apache.arrow.vector.types.pojo.ArrowType.Time(
                    org.apache.arrow.vector.types.TimeUnit.MILLISECOND, 32)), null),
        nested, struct, map);
    var config = new io.zingg.duckdb.api.RuntimeConfig(
        "jdbc:duckdb:" + temp.resolve("unsupported-arrow-types.duckdb"), 1, 0, null, 1);
    try (var runtime = new DuckRuntime(config); var job = (DuckJob) runtime.openJob()) {
      int registeredBefore = job.materializedCount();
      for (var field : unsupportedFields) {
        Path input = temp.resolve(field.getName() + ".arrow");
        try (var allocator = new org.apache.arrow.memory.RootAllocator();
             var root = org.apache.arrow.vector.VectorSchemaRoot.create(
                 new org.apache.arrow.vector.types.pojo.Schema(List.of(field)), allocator);
             var channel = java.nio.channels.Channels.newChannel(java.nio.file.Files.newOutputStream(input));
             var writer = new org.apache.arrow.vector.ipc.ArrowStreamWriter(root, null, channel)) {
          writer.start();
          writer.end();
        }
        for (boolean useAppender : new boolean[] {false, true}) {
          var failure = org.junit.jupiter.api.Assertions.assertThrows(
              io.zingg.duckdb.api.DuckException.class,
              () -> ArrowFileSupport.read(job, input, useAppender, importedRows -> {}));
          assertTrue(failure.getMessage().startsWith("unsupported Arrow field type:"),
              failure.getMessage());
          assertEquals(registeredBefore, job.materializedCount(),
              "unsupported schema must fail before registering a work table");
          try (var statement = job.connection().createStatement();
               var rows = statement.executeQuery("SELECT count(*) FROM information_schema.tables WHERE table_schema='"
                   + job.schemaName() + "'")) {
            org.junit.jupiter.api.Assertions.assertTrue(rows.next());
            assertEquals(0, rows.getInt(1), "unsupported schema must not leave a partial table");
          }
        }
      }
    }
  }

  @Test
  void allArrowTimeUnitsRejectBeforeStagingAcrossImportRoutesAndFramings(@TempDir Path temp)
      throws Exception {
    var timeFields = List.of(
        new org.apache.arrow.vector.types.pojo.Field("time_second",
            org.apache.arrow.vector.types.pojo.FieldType.nullable(new org.apache.arrow.vector.types.pojo.ArrowType.Time(
                org.apache.arrow.vector.types.TimeUnit.SECOND, 32)), null),
        new org.apache.arrow.vector.types.pojo.Field("time_millisecond",
            org.apache.arrow.vector.types.pojo.FieldType.nullable(new org.apache.arrow.vector.types.pojo.ArrowType.Time(
                org.apache.arrow.vector.types.TimeUnit.MILLISECOND, 32)), null),
        new org.apache.arrow.vector.types.pojo.Field("time_microsecond",
            org.apache.arrow.vector.types.pojo.FieldType.nullable(new org.apache.arrow.vector.types.pojo.ArrowType.Time(
                org.apache.arrow.vector.types.TimeUnit.MICROSECOND, 64)), null),
        new org.apache.arrow.vector.types.pojo.Field("time_nanosecond",
            org.apache.arrow.vector.types.pojo.FieldType.nullable(new org.apache.arrow.vector.types.pojo.ArrowType.Time(
                org.apache.arrow.vector.types.TimeUnit.NANOSECOND, 64)), null));
    var config = new io.zingg.duckdb.api.RuntimeConfig(
        "jdbc:duckdb:" + temp.resolve("unsupported-arrow-time-units.duckdb"), 1, 0, null, 1);
    try (var runtime = new DuckRuntime(config); var job = (DuckJob) runtime.openJob()) {
      int registeredBefore = job.materializedCount();
      for (var field : timeFields) {
        for (boolean fileFormat : new boolean[] {false, true}) {
          Path input = temp.resolve(field.getName() + "-" + fileFormat + ".arrow");
          writeEmptyArrow(input, field, fileFormat);
          for (boolean useAppender : new boolean[] {false, true}) {
            var failure = assertThrows(io.zingg.duckdb.api.DuckException.class,
                () -> ArrowFileSupport.read(job, input, useAppender, importedRows -> {}),
                "TIME must be rejected for unit="
                    + ((org.apache.arrow.vector.types.pojo.ArrowType.Time) field.getType()).getUnit()
                    + ", fileFormat=" + fileFormat + ", appender=" + useAppender);
            assertTrue(failure.getMessage().startsWith("unsupported Arrow field type:"), failure.getMessage());
            assertEquals(registeredBefore, job.materializedCount());
            assertEquals(0L, ((Number) job.sql(
                "select count(*) as n from information_schema.tables where table_schema='" + job.schemaName()
                    + "' and table_name like 'zd_arrow_%'").collect().get(0).get("n")).longValue());
          }
        }
      }
    }
  }

  @Test
  void duplicateArrowFieldNamesFailBeforeCreatingWorkTables(@TempDir Path temp) throws Exception {
    var intType = new org.apache.arrow.vector.types.pojo.ArrowType.Int(32, true);
    var duplicateSchemas = List.of(List.of("duplicate", "duplicate"), List.of("Name", "name"));
    var config = new io.zingg.duckdb.api.RuntimeConfig(
        "jdbc:duckdb:" + temp.resolve("duplicate-arrow-fields.duckdb"), 1, 0, null, 1);
    try (var runtime = new DuckRuntime(config); var job = (DuckJob) runtime.openJob()) {
      int registeredBefore = job.materializedCount();
      for (int schemaIndex = 0; schemaIndex < duplicateSchemas.size(); schemaIndex++) {
        for (boolean fileFormat : new boolean[] {false, true}) {
          var names = duplicateSchemas.get(schemaIndex);
          var fields = names.stream().map(name -> new org.apache.arrow.vector.types.pojo.Field(name,
              org.apache.arrow.vector.types.pojo.FieldType.nullable(intType), null)).toList();
          Path input = temp.resolve("duplicate-fields-" + schemaIndex + "-" + fileFormat + ".arrow");
          try (var allocator = new org.apache.arrow.memory.RootAllocator();
               var root = org.apache.arrow.vector.VectorSchemaRoot.create(
                   new org.apache.arrow.vector.types.pojo.Schema(fields), allocator);
               var channel = java.nio.channels.Channels.newChannel(java.nio.file.Files.newOutputStream(input))) {
            ((org.apache.arrow.vector.IntVector) root.getVector(0)).setSafe(0, 1);
            ((org.apache.arrow.vector.IntVector) root.getVector(1)).setSafe(0, 2);
            root.setRowCount(1);
            writeArrowBatch(root, channel, fileFormat);
          }
          for (boolean useAppender : new boolean[] {false, true}) {
            var failure = assertThrows(io.zingg.duckdb.api.DuckException.class,
                () -> ArrowFileSupport.read(job, input, useAppender, importedRows -> {}));
            assertEquals("duplicate Arrow field name (case-insensitive): " + names.get(1),
                failure.getMessage());
            assertEquals(registeredBefore, job.materializedCount());
            assertEquals(0L, ((Number) job.sql(
                "select count(*) as n from information_schema.tables where table_schema='" + job.schemaName()
                    + "' and table_name like 'zd_arrow_%'").collect().get(0).get("n")).longValue());
          }
        }
      }
    }
  }

  @Test
  void arrowIngressPreservesDeclaredNullabilityAndRejectsRequiredNulls(@TempDir Path temp)
      throws Exception {
    var intType = new org.apache.arrow.vector.types.pojo.ArrowType.Int(32, true);
    var requiredField = new org.apache.arrow.vector.types.pojo.Field("required_value",
        new org.apache.arrow.vector.types.pojo.FieldType(false, intType, null, null), null);
    var optionalField = new org.apache.arrow.vector.types.pojo.Field("optional_value",
        org.apache.arrow.vector.types.pojo.FieldType.nullable(intType), null);
    var config = new io.zingg.duckdb.api.RuntimeConfig(
        "jdbc:duckdb:" + temp.resolve("arrow-nullability.duckdb"), 1, 0, null, 1);
    try (var runtime = new DuckRuntime(config); var job = (DuckJob) runtime.openJob()) {
      for (boolean fileFormat : new boolean[] {false, true}) {
        Path validInput = temp.resolve("nullability-valid-" + fileFormat + ".arrow");
        try (var allocator = new org.apache.arrow.memory.RootAllocator();
             var root = org.apache.arrow.vector.VectorSchemaRoot.create(
                 new org.apache.arrow.vector.types.pojo.Schema(List.of(requiredField, optionalField)), allocator);
             var channel = java.nio.channels.Channels.newChannel(java.nio.file.Files.newOutputStream(validInput))) {
          var required = (org.apache.arrow.vector.IntVector) root.getVector("required_value");
          required.setSafe(0, 10); required.setSafe(1, 20); required.setValueCount(2);
          var optional = (org.apache.arrow.vector.IntVector) root.getVector("optional_value");
          optional.setNull(0); optional.setSafe(1, 30); optional.setValueCount(2);
          root.setRowCount(2);
          writeArrowBatch(root, channel, fileFormat);
        }
        for (boolean useAppender : new boolean[] {false, true}) {
          var frame = ArrowFileSupport.read(job, validInput, useAppender, importedRows -> {});
          assertEquals(List.of(true, true), frame.schema().stream()
              .map(io.zingg.duckdb.api.Column::nullable).toList(),
              "DuckDB JDBC result metadata reports projected columns as nullable/unknown");
          assertEquals(List.of(10, 20), frame.collect().stream()
              .map(row -> row.get("required_value")).toList());
          assertEquals(java.util.Arrays.asList(null, 30), frame.collect().stream()
              .map(row -> row.get("optional_value")).toList());
        }
        long requiredNotNullColumns = ((Number) job.sql(
            "select count(*) as n from information_schema.columns where table_schema='" + job.schemaName()
                + "' and table_name like 'zd_arrow_%' and column_name='required_value' and is_nullable='NO'")
            .collect().get(0).get("n")).longValue();
        long optionalNullableColumns = ((Number) job.sql(
            "select count(*) as n from information_schema.columns where table_schema='" + job.schemaName()
                + "' and table_name like 'zd_arrow_%' and column_name='optional_value' and is_nullable='YES'")
            .collect().get(0).get("n")).longValue();
        long expectedValidImportTables = fileFormat ? 4L : 2L;
        assertEquals(expectedValidImportTables, requiredNotNullColumns,
            "both import paths must retain Arrow's non-nullable column constraint");
        assertEquals(expectedValidImportTables, optionalNullableColumns,
            "both import paths must retain Arrow's nullable column declaration");

        Path invalidInput = temp.resolve("nullability-invalid-" + fileFormat + ".arrow");
        try (var allocator = new org.apache.arrow.memory.RootAllocator();
             var root = org.apache.arrow.vector.VectorSchemaRoot.create(
                 new org.apache.arrow.vector.types.pojo.Schema(List.of(requiredField)), allocator);
             var channel = java.nio.channels.Channels.newChannel(java.nio.file.Files.newOutputStream(invalidInput))) {
          var required = (org.apache.arrow.vector.IntVector) root.getVector("required_value");
          required.setNull(0); required.setValueCount(1);
          root.setRowCount(1);
          writeArrowBatch(root, channel, fileFormat);
        }
        int registrationsBeforeInvalidImport = job.materializedCount();
        long workTablesBeforeInvalidImport = ((Number) job.sql(
            "select count(*) as n from information_schema.tables where table_name like 'zd_arrow_%'")
            .collect().get(0).get("n")).longValue();
        for (boolean useAppender : new boolean[] {false, true}) {
          var failure = org.junit.jupiter.api.Assertions.assertThrows(
              io.zingg.duckdb.api.DuckException.class,
              () -> ArrowFileSupport.read(job, invalidInput, useAppender, importedRows -> {}),
              "required null must fail for fileFormat=" + fileFormat + ", appender=" + useAppender);
          assertEquals("null value in non-nullable Arrow field: required_value", failure.getMessage());
          assertEquals(registrationsBeforeInvalidImport, job.materializedCount());
          assertEquals(workTablesBeforeInvalidImport,
              ((Number) job.sql("select count(*) as n from information_schema.tables where table_name like 'zd_arrow_%'")
                  .collect().get(0).get("n")).longValue());
        }
      }
    }
  }

  @Test
  void emptyArrowStreamAndFilePreserveSchemaAndImportZeroRows(@TempDir Path temp)
      throws Exception {
    var config = new io.zingg.duckdb.api.RuntimeConfig(
        "jdbc:duckdb:" + temp.resolve("empty-arrow.duckdb"), 1, 0, null, 1);
    try (var runtime = new DuckRuntime(config); var job = (DuckJob) runtime.openJob()) {
      for (boolean fileFormat : new boolean[] {false, true}) {
        for (boolean includeEmptyBatch : new boolean[] {false, true}) {
          Path input = temp.resolve((fileFormat ? "empty-file" : "empty-stream")
              + (includeEmptyBatch ? "-batch" : "-schema") + ".arrow");
          writeEmptyArrow(input, fileFormat, includeEmptyBatch);
          for (boolean useAppender : new boolean[] {false, true}) {
            var imported = useAppender
                ? ArrowFileSupport.readWithAppender(job, input)
                : ArrowFileSupport.read(job, input);
            assertEquals(List.of("value"), imported.columns());
            assertEquals(List.of("value"), imported.schema().stream()
                .map(io.zingg.duckdb.api.Column::name).toList());
            assertEquals(0, imported.count(),
                "zero-row Arrow input must import successfully with zero rows");
            assertEquals(0, imported.collect().size());
          }
        }
      }
    }
  }

  @Test
  void truncatedLaterArrowBatchRollsBackBothImportPaths(@TempDir Path temp) throws Exception {
    byte[] complete;
    try (var allocator = new org.apache.arrow.memory.RootAllocator();
         var root = org.apache.arrow.vector.VectorSchemaRoot.create(
             new org.apache.arrow.vector.types.pojo.Schema(List.of(
                 new org.apache.arrow.vector.types.pojo.Field("value",
                     org.apache.arrow.vector.types.pojo.FieldType.nullable(
                         new org.apache.arrow.vector.types.pojo.ArrowType.Int(32, true)), null))), allocator);
         var output = new java.io.ByteArrayOutputStream();
         var channel = java.nio.channels.Channels.newChannel(output);
         var writer = new org.apache.arrow.vector.ipc.ArrowStreamWriter(root, null, channel)) {
      var vector = (org.apache.arrow.vector.IntVector) root.getVector("value");
      writer.start();
      vector.setSafe(0, 10);
      root.setRowCount(1);
      writer.writeBatch();
      vector.setSafe(0, 20);
      root.setRowCount(1);
      writer.writeBatch();
      writer.end();
      complete = output.toByteArray();
    }

    int secondBatchOffset;
    try (var allocator = new org.apache.arrow.memory.RootAllocator();
         var source = new java.io.ByteArrayInputStream(complete);
         var reader = new org.apache.arrow.vector.ipc.ArrowStreamReader(source, allocator)) {
      assertEquals(true, reader.loadNextBatch());
      assertEquals(1, reader.getVectorSchemaRoot().getRowCount());
      secondBatchOffset = complete.length - source.available();
    }
    // Keep the second message's framing header but truncate before its FlatBuffer metadata.
    // The first batch is valid and can be committed; decoding the next batch must fail.
    assertEquals((byte) 0xff, complete[secondBatchOffset]);
    Path input = temp.resolve("truncated-second-batch.arrow");
    java.nio.file.Files.write(input, java.util.Arrays.copyOf(complete, secondBatchOffset + 8));

    var config = new io.zingg.duckdb.api.RuntimeConfig(
        "jdbc:duckdb:" + temp.resolve("truncated-batch.duckdb"), 1, 0, null, 1);
    try (var runtime = new DuckRuntime(config); var job = (DuckJob) runtime.openJob()) {
      int registeredBefore = job.materializedCount();
      for (boolean useAppender : new boolean[] {false, true}) {
        boolean[] firstBatchCommitted = {false};
        var failure = org.junit.jupiter.api.Assertions.assertThrows(
            io.zingg.duckdb.api.DuckException.class,
            () -> ArrowFileSupport.read(job, input, useAppender, importedRows -> {
              if (importedRows == 1) firstBatchCommitted[0] = true;
            }));
        assertEquals("Arrow IPC ingestion failed: " + input, failure.getMessage());
        org.junit.jupiter.api.Assertions.assertNotNull(failure.getCause());
        org.junit.jupiter.api.Assertions.assertTrue(firstBatchCommitted[0],
            "the first batch must be committed before the malformed later batch is decoded");
        assertEquals(registeredBefore, job.materializedCount(),
            "decoder failure must release materialization registration");
        try (var statement = job.connection().createStatement();
             var rows = statement.executeQuery("select count(*) from information_schema.tables where table_schema='"
                 + job.schemaName() + "'")) {
          org.junit.jupiter.api.Assertions.assertTrue(rows.next());
          assertEquals(0, rows.getInt(1), "decoder failure must drop the partially populated table");
        }
      }
    }
  }

  @Test
  void decimalInsertFailureAfterCommittedArrowFileBatchRollsBackBothImportPaths(@TempDir Path temp)
      throws Exception {
    Path input = temp.resolve("decimal-overflow-file.arrow");
    try (var allocator = new org.apache.arrow.memory.RootAllocator();
         var root = org.apache.arrow.vector.VectorSchemaRoot.create(
             new org.apache.arrow.vector.types.pojo.Schema(List.of(
                 new org.apache.arrow.vector.types.pojo.Field("amount",
                     org.apache.arrow.vector.types.pojo.FieldType.nullable(
                         new org.apache.arrow.vector.types.pojo.ArrowType.Decimal(5, 2, 128)), null))), allocator);
         var channel = java.nio.channels.Channels.newChannel(java.nio.file.Files.newOutputStream(input));
         var writer = new org.apache.arrow.vector.ipc.ArrowFileWriter(root, null, channel)) {
      var vector = (org.apache.arrow.vector.DecimalVector) root.getVector("amount");
      writer.start();
      vector.setSafe(0, new java.math.BigDecimal("12.34"));
      root.setRowCount(1);
      writer.writeBatch();
      vector.setSafe(0, new java.math.BigDecimal("999.99"));
      byte[] bigEndian = new java.math.BigInteger("100000").toByteArray();
      byte[] littleEndian = new byte[16];
      for (int i = 0; i < bigEndian.length; i++) littleEndian[i] = bigEndian[bigEndian.length - 1 - i];
      vector.getDataBuffer().setBytes(0, littleEndian);
      root.setRowCount(1);
      writer.writeBatch();
      writer.end();
    }

    try (var allocator = new org.apache.arrow.memory.RootAllocator();
         var channel = java.nio.channels.FileChannel.open(input, java.nio.file.StandardOpenOption.READ);
         var reader = new org.apache.arrow.vector.ipc.ArrowFileReader(channel, allocator)) {
      reader.initialize();
      assertEquals(2, reader.getRecordBlocks().size(),
          "the valid file footer must reference both record batches before import begins");
    }

    var config = new io.zingg.duckdb.api.RuntimeConfig(
        "jdbc:duckdb:" + temp.resolve("decimal-overflow-file.duckdb"), 1, 0, null, 1);
    try (var runtime = new DuckRuntime(config); var job = (DuckJob) runtime.openJob()) {
      int registeredBefore = job.materializedCount();
      for (boolean useAppender : new boolean[] {false, true}) {
        boolean[] firstBatchCommitted = {false};
        var failure = org.junit.jupiter.api.Assertions.assertThrows(
            io.zingg.duckdb.api.DuckException.class,
            () -> ArrowFileSupport.read(job, input, useAppender, importedRows -> {
              if (importedRows == 1) firstBatchCommitted[0] = true;
            }));
        assertEquals("Arrow IPC ingestion failed: " + input, failure.getMessage());
        org.junit.jupiter.api.Assertions.assertInstanceOf(java.sql.SQLException.class, failure.getCause(),
            "the error must occur on DuckDB's insert path after Arrow file decoding");
        assertTrue(firstBatchCommitted[0], "the first record batch must be imported before the bad decimal");
        assertEquals(registeredBefore, job.materializedCount(), "failed import must release its registration");
        assertEquals(0L, ((Number) job.sql(
            "select count(*) as n from information_schema.tables where table_schema='" + job.schemaName()
                + "' and table_name like 'zd_arrow_%'").collect().get(0).get("n")).longValue(),
            "failed import must drop its partially populated table");
      }
    }
  }

  @Test
  void decimalInsertFailureAfterCommittedArrowBatchRollsBackBothImportPaths(@TempDir Path temp)
      throws Exception {
    Path input = temp.resolve("decimal-overflow.arrow");
    try (var allocator = new org.apache.arrow.memory.RootAllocator();
         var root = org.apache.arrow.vector.VectorSchemaRoot.create(
             new org.apache.arrow.vector.types.pojo.Schema(List.of(
                 new org.apache.arrow.vector.types.pojo.Field("amount",
                     org.apache.arrow.vector.types.pojo.FieldType.nullable(
                         new org.apache.arrow.vector.types.pojo.ArrowType.Decimal(5, 2, 128)), null))), allocator);
         var channel = java.nio.channels.Channels.newChannel(java.nio.file.Files.newOutputStream(input));
         var writer = new org.apache.arrow.vector.ipc.ArrowStreamWriter(root, null, channel)) {
      var vector = (org.apache.arrow.vector.DecimalVector) root.getVector("amount");
      writer.start();
      vector.setSafe(0, new java.math.BigDecimal("12.34"));
      root.setRowCount(1);
      writer.writeBatch();
      // DecimalVector's public setter enforces the declared precision, so write the
      // signed 128-bit unscaled payload directly to construct a well-framed IPC file
      // containing a value outside DECIMAL(5,2)'s legal range.
      vector.setSafe(0, new java.math.BigDecimal("999.99"));
      byte[] bigEndian = new java.math.BigInteger("100000").toByteArray();
      byte[] littleEndian = new byte[16];
      for (int i = 0; i < bigEndian.length; i++) littleEndian[i] = bigEndian[bigEndian.length - 1 - i];
      vector.getDataBuffer().setBytes(0, littleEndian);
      root.setRowCount(1);
      writer.writeBatch();
      writer.end();
    }

    var config = new io.zingg.duckdb.api.RuntimeConfig(
        "jdbc:duckdb:" + temp.resolve("decimal-overflow.duckdb"), 1, 0, null, 1);
    try (var runtime = new DuckRuntime(config); var job = (DuckJob) runtime.openJob()) {
      int registeredBefore = job.materializedCount();
      for (boolean useAppender : new boolean[] {false, true}) {
        boolean[] firstBatchCommitted = {false};
        var failure = org.junit.jupiter.api.Assertions.assertThrows(
            io.zingg.duckdb.api.DuckException.class,
            () -> ArrowFileSupport.read(job, input, useAppender, importedRows -> {
              if (importedRows == 1) firstBatchCommitted[0] = true;
            }));
        assertEquals("Arrow IPC ingestion failed: " + input, failure.getMessage());
        org.junit.jupiter.api.Assertions.assertInstanceOf(java.sql.SQLException.class, failure.getCause(),
            "the failure must originate from DuckDB's JDBC/Appender write path, not Arrow decoding");
        org.junit.jupiter.api.Assertions.assertTrue(firstBatchCommitted[0],
            "the first valid batch must commit before the out-of-range decimal is inserted");
        assertEquals(registeredBefore, job.materializedCount(),
            "write failure must release materialization registration");
        try (var statement = job.connection().createStatement();
             var rows = statement.executeQuery("select count(*) from information_schema.tables where table_schema='"
                 + job.schemaName() + "'")) {
          org.junit.jupiter.api.Assertions.assertTrue(rows.next());
          assertEquals(0, rows.getInt(1), "write failure must drop the partially populated table");
        }
      }
    }
  }

  @Test
  void forciblyKilledArrowImportLeavesNoOrphanTablesAfterDatabaseRestart(@TempDir Path temp)
      throws Exception {
    Path input = temp.resolve("kill-input.arrow");
    try (var allocator = new org.apache.arrow.memory.RootAllocator();
         var root = org.apache.arrow.vector.VectorSchemaRoot.create(
             new org.apache.arrow.vector.types.pojo.Schema(List.of(
                 new org.apache.arrow.vector.types.pojo.Field("value",
                     org.apache.arrow.vector.types.pojo.FieldType.nullable(
                         new org.apache.arrow.vector.types.pojo.ArrowType.Int(32, true)), null))), allocator);
         var channel = java.nio.channels.Channels.newChannel(java.nio.file.Files.newOutputStream(input));
         var writer = new org.apache.arrow.vector.ipc.ArrowStreamWriter(root, null, channel)) {
      var vector = (org.apache.arrow.vector.IntVector) root.getVector("value");
      writer.start();
      vector.setSafe(0, 10);
      root.setRowCount(1);
      writer.writeBatch();
      vector.setSafe(0, 20);
      root.setRowCount(1);
      writer.writeBatch();
      writer.end();
    }

    Path database = temp.resolve("killed-import.duckdb");
    Path ready = temp.resolve("first-batch-committed.txt");
    Path childLog = temp.resolve("arrow-kill-child.log");
    String javaCommand = Path.of(System.getProperty("java.home"), "bin",
        System.getProperty("os.name").toLowerCase(java.util.Locale.ROOT).contains("win") ? "java.exe" : "java")
        .toString();
    var child = new ProcessBuilder(javaCommand,
        "--add-opens=java.base/java.nio=ALL-UNNAMED",
        "--add-opens=java.base/sun.nio.ch=ALL-UNNAMED",
        "-cp", System.getProperty("java.class.path"),
        ArrowIngressKillProbe.class.getName(), database.toString(), input.toString(), ready.toString())
        .directory(temp.toFile()).redirectErrorStream(true).redirectOutput(childLog.toFile()).start();
    try {
      long deadline = System.nanoTime() + java.util.concurrent.TimeUnit.SECONDS.toNanos(60);
      while (!java.nio.file.Files.exists(ready) && child.isAlive() && System.nanoTime() < deadline)
        Thread.sleep(10);
      org.junit.jupiter.api.Assertions.assertTrue(java.nio.file.Files.exists(ready),
          "child must commit the first Arrow batch and block before reading the next one; child output: "
              + java.nio.file.Files.readString(childLog));
      org.junit.jupiter.api.Assertions.assertTrue(child.destroyForcibly().waitFor(10,
          java.util.concurrent.TimeUnit.SECONDS), "forced worker termination must complete");
    } finally {
      if (child.isAlive()) {
        child.destroyForcibly();
        child.waitFor(10, java.util.concurrent.TimeUnit.SECONDS);
      }
    }

    try (var runtime = new DuckRuntime(new io.zingg.duckdb.api.RuntimeConfig(
             "jdbc:duckdb:" + database, 1, 0, null, 1));
         var job = (DuckJob) runtime.openJob();
         var statement = job.connection().createStatement()) {
      try (var rows = statement.executeQuery("select count(*) from information_schema.tables where table_name like 'zd_arrow_%'")) {
        org.junit.jupiter.api.Assertions.assertTrue(rows.next());
        assertEquals(0, rows.getInt(1), "restart must not expose a partially imported Arrow table");
      }
      try (var rows = statement.executeQuery("select count(*) from information_schema.schemata "
          + "where substr(schema_name, 1, 11) = '_zingg_run_' and schema_name <> '" + job.schemaName() + "'")) {
        org.junit.jupiter.api.Assertions.assertTrue(rows.next());
        assertEquals(0, rows.getInt(1), "restart must remove the killed job's orphan run schema");
      }
    }
  }

  @Test
  void arrowIngressRejectsSymbolicLinkWithoutReadingExternalTarget(@TempDir Path temp) throws Exception {
    var config = new io.zingg.duckdb.api.RuntimeConfig(
        "jdbc:duckdb:" + temp.resolve("arrow-symlink.duckdb"), 1, 0, null, 1);
    Path external = temp.resolve("external.arrow");
    Path linked = temp.resolve("linked.arrow");
    java.nio.file.Files.write(external, new byte[] {0x01, 0x02, 0x03, 0x04});
    try {
      java.nio.file.Files.createSymbolicLink(linked, external.getFileName());
    } catch (UnsupportedOperationException | java.io.IOException | SecurityException unavailable) {
      org.junit.jupiter.api.Assumptions.abort("symbolic links are unavailable: " + unavailable);
    }
    try (var runtime = new DuckRuntime(config); var job = runtime.openJob()) {
      var failure = org.junit.jupiter.api.Assertions.assertThrows(io.zingg.duckdb.api.DuckException.class,
          () -> ArrowFileSupport.read((DuckJob) job, linked));
      org.junit.jupiter.api.Assertions.assertNotNull(failure.getCause());
      assertEquals(java.util.List.of(), job.sql("SELECT table_name FROM information_schema.tables "
          + "WHERE table_schema = " + DuckExpr.literal(((DuckJob) job).schemaName()).sql()
          + " AND table_name LIKE 'zd_arrow_%'").collect());
    }
    org.junit.jupiter.api.Assertions.assertArrayEquals(new byte[] {0x01, 0x02, 0x03, 0x04},
        java.nio.file.Files.readAllBytes(external));
  }

  @Test
  void arrowRoundTripHandlesDateTimestampAndBinary(@TempDir Path temp) throws Exception {
    var config = new io.zingg.duckdb.api.RuntimeConfig(
        "jdbc:duckdb:" + temp.resolve("arrow-types.duckdb"), 1, 0, null, 1);
    Path output = temp.resolve("types.arrow");
    try (var runtime = new DuckRuntime(config); var job = runtime.openJob()) {
      job.sql("select DATE '2020-01-02' as day, TIMESTAMP '2020-01-02 03:04:05' as moment, CAST('123.4567' AS DECIMAL(18,4)) as amount, from_hex('CAFE') as payload").writeArrow(output);
      var jdbcPath = ArrowFileSupport.read((DuckJob) job, output);
      var appenderPath = ArrowFileSupport.readWithAppender((DuckJob) job, output);
      assertEquals(jdbcPath.schema(), appenderPath.schema());
      assertEquals(jdbcPath.collect(), appenderPath.collect(), "Appender must preserve date, decimal and binary values");
      var roundTrip = new DuckPipeReader(job).read(java.util.List.of(output), DuckPipeReader.UnionMode.MATCH_BY_NAME);
      assertEquals(1, roundTrip.count());
      assertEquals(java.time.LocalDate.of(2020, 1, 2), roundTrip.collect().get(0).get("day"));
      assertEquals(0, ((java.math.BigDecimal)roundTrip.collect().get(0).get("amount")).compareTo(new java.math.BigDecimal("123.4567")));
      assertEquals(true, roundTrip.schema().stream().anyMatch(c -> c.name().equals("amount") && c.typeName().contains("18,4")));
    }
  }

  @Test
  void arrowDate32AndDate64ScalarsMatchAcrossImportRoutesAndFramings(@TempDir Path temp)
      throws Exception {
    var date32 = new org.apache.arrow.vector.types.pojo.ArrowType.Date(
        org.apache.arrow.vector.types.DateUnit.DAY);
    var date64 = new org.apache.arrow.vector.types.pojo.ArrowType.Date(
        org.apache.arrow.vector.types.DateUnit.MILLISECOND);
    var fields = List.of(
        new org.apache.arrow.vector.types.pojo.Field("date32",
            org.apache.arrow.vector.types.pojo.FieldType.nullable(date32), null),
        new org.apache.arrow.vector.types.pojo.Field("date64",
            org.apache.arrow.vector.types.pojo.FieldType.nullable(date64), null));
    var expected32 = List.of(java.time.LocalDate.of(1969, 12, 31), java.time.LocalDate.of(2020, 1, 2));
    var expected64 = List.of(java.time.LocalDate.of(1969, 12, 31), java.time.LocalDate.of(2020, 1, 2));
    var config = new io.zingg.duckdb.api.RuntimeConfig(
        "jdbc:duckdb:" + temp.resolve("arrow-date-scalars.duckdb"), 1, 0, null, 1);
    try (var runtime = new DuckRuntime(config); var job = (DuckJob) runtime.openJob()) {
      for (boolean fileFormat : new boolean[] {false, true}) {
        Path input = temp.resolve("date-scalars-" + fileFormat + ".arrow");
        try (var allocator = new org.apache.arrow.memory.RootAllocator();
             var root = org.apache.arrow.vector.VectorSchemaRoot.create(
                 new org.apache.arrow.vector.types.pojo.Schema(fields), allocator);
             var channel = java.nio.channels.Channels.newChannel(java.nio.file.Files.newOutputStream(input))) {
          var days = (org.apache.arrow.vector.DateDayVector) root.getVector("date32");
          days.setSafe(0, -1);
          days.setSafe(1, (int) java.time.LocalDate.of(2020, 1, 2).toEpochDay());
          var millis = (org.apache.arrow.vector.DateMilliVector) root.getVector("date64");
          millis.setSafe(0, -86_400_000L);
          millis.setSafe(1, java.time.LocalDate.of(2020, 1, 2).toEpochDay() * 86_400_000L);
          root.setRowCount(2);
          writeArrowBatch(root, channel, fileFormat);
        }
        var jdbc = ArrowFileSupport.read((DuckJob) job, input);
        var appender = ArrowFileSupport.readWithAppender((DuckJob) job, input);
        assertEquals(jdbc.schema(), appender.schema());
        assertEquals(List.of("DATE", "DATE"), jdbc.schema().stream().map(io.zingg.duckdb.api.Column::typeName).toList());
        assertEquals(jdbc.collect(), appender.collect());
        assertEquals(expected32, jdbc.collect().stream().map(row -> row.get("date32")).toList());
        assertEquals(expected64, jdbc.collect().stream().map(row -> row.get("date64")).toList());
      }
    }
  }

  @Test
  void arrowTimestampUnitsAndTimezoneArePreservedOnIngress(@TempDir Path temp) throws Exception {
    Path input = temp.resolve("timestamp-units.arrow");
    var fields = java.util.List.of(
        arrowTimestampField("seconds", org.apache.arrow.vector.types.TimeUnit.SECOND, null),
        arrowTimestampField("millis", org.apache.arrow.vector.types.TimeUnit.MILLISECOND, null),
        arrowTimestampField("micros", org.apache.arrow.vector.types.TimeUnit.MICROSECOND, null),
        arrowTimestampField("nanos", org.apache.arrow.vector.types.TimeUnit.NANOSECOND, null),
        arrowTimestampField("zoned", org.apache.arrow.vector.types.TimeUnit.MICROSECOND, "America/Los_Angeles"));
    try (var allocator = new org.apache.arrow.memory.RootAllocator();
         var root = org.apache.arrow.vector.VectorSchemaRoot.create(new org.apache.arrow.vector.types.pojo.Schema(fields), allocator);
         var channel = java.nio.channels.Channels.newChannel(java.nio.file.Files.newOutputStream(input));
         var writer = new org.apache.arrow.vector.ipc.ArrowStreamWriter(root, null, channel)) {
      ((org.apache.arrow.vector.TimeStampSecVector) root.getVector("seconds")).setSafe(0, 1_700_000_000L);
      ((org.apache.arrow.vector.TimeStampMilliVector) root.getVector("millis")).setSafe(0, 1_700_000_000_123L);
      ((org.apache.arrow.vector.TimeStampMicroVector) root.getVector("micros")).setSafe(0, 1_700_000_000_123_456L);
      ((org.apache.arrow.vector.TimeStampNanoVector) root.getVector("nanos")).setSafe(0, 1_700_000_000_123_456_789L);
      ((org.apache.arrow.vector.TimeStampMicroTZVector) root.getVector("zoned")).setSafe(0, 1_700_000_000_123_456L);
      ((org.apache.arrow.vector.TimeStampSecVector) root.getVector("seconds")).setSafe(1, -1L);
      ((org.apache.arrow.vector.TimeStampMilliVector) root.getVector("millis")).setSafe(1, -1L);
      ((org.apache.arrow.vector.TimeStampMicroVector) root.getVector("micros")).setSafe(1, -1L);
      ((org.apache.arrow.vector.TimeStampNanoVector) root.getVector("nanos")).setSafe(1, -1L);
      ((org.apache.arrow.vector.TimeStampMicroTZVector) root.getVector("zoned")).setSafe(1, -1L);
      root.setRowCount(2);
      writer.start();
      writer.writeBatch();
      writer.end();
    }
    var config = new io.zingg.duckdb.api.RuntimeConfig(
        "jdbc:duckdb:" + temp.resolve("timestamp-units.duckdb"), 1, 0, null, 1);
    try (var runtime = new DuckRuntime(config); var job = runtime.openJob()) {
      var jdbcPath = ArrowFileSupport.read((DuckJob) job, input);
      var appenderPath = ArrowFileSupport.readWithAppender((DuckJob) job, input);
      assertEquals(jdbcPath.schema(), appenderPath.schema());
      var rows = jdbcPath.collect();
      assertEquals(rows, appenderPath.collect(), "Appender path must preserve timestamps and zones");
      var row = rows.get(0);
      assertEquals(java.sql.Timestamp.valueOf(java.time.LocalDateTime.of(2023, 11, 14, 22, 13, 20)), row.get("seconds"));
      assertEquals(java.sql.Timestamp.valueOf(java.time.LocalDateTime.of(2023, 11, 14, 22, 13, 20, 123_000_000)), row.get("millis"));
      assertEquals(java.sql.Timestamp.valueOf(java.time.LocalDateTime.of(2023, 11, 14, 22, 13, 20, 123_456_000)), row.get("micros"));
      assertEquals(java.sql.Timestamp.valueOf(java.time.LocalDateTime.of(2023, 11, 14, 22, 13, 20, 123_456_000)), row.get("nanos"));
      assertEquals(java.time.Instant.parse("2023-11-14T22:13:20.123456Z"), ((java.time.OffsetDateTime) row.get("zoned")).toInstant());
      var beforeEpoch = rows.get(1);
      assertEquals(java.sql.Timestamp.valueOf(java.time.LocalDateTime.of(1969, 12, 31, 23, 59, 59)), beforeEpoch.get("seconds"));
      assertEquals(java.sql.Timestamp.valueOf(java.time.LocalDateTime.of(1969, 12, 31, 23, 59, 59, 999_000_000)), beforeEpoch.get("millis"));
      assertEquals(java.sql.Timestamp.valueOf(java.time.LocalDateTime.of(1969, 12, 31, 23, 59, 59, 999_999_000)), beforeEpoch.get("micros"));
      assertEquals(java.sql.Timestamp.valueOf(java.time.LocalDateTime.of(1969, 12, 31, 23, 59, 59, 999_999_000)), beforeEpoch.get("nanos"));
      assertEquals(java.time.Instant.parse("1969-12-31T23:59:59.999999Z"), ((java.time.OffsetDateTime) beforeEpoch.get("zoned")).toInstant());
    }
  }

  @Test
  void arrowTimezoneTimestampScalarMatchesAcrossFramingsAndImportRoutes(@TempDir Path temp)
      throws Exception {
    var field = arrowTimestampField("occurred_at", org.apache.arrow.vector.types.TimeUnit.MICROSECOND,
        "America/Los_Angeles");
    var config = new io.zingg.duckdb.api.RuntimeConfig(
        "jdbc:duckdb:" + temp.resolve("timestamp-zoned-framings.duckdb"), 1, 0, null, 1);
    try (var runtime = new DuckRuntime(config); var job = (DuckJob) runtime.openJob()) {
      for (boolean fileFormat : new boolean[] {false, true}) {
        Path input = temp.resolve("timestamp-zoned-" + fileFormat + ".arrow");
        try (var allocator = new org.apache.arrow.memory.RootAllocator();
             var root = org.apache.arrow.vector.VectorSchemaRoot.create(
                 new org.apache.arrow.vector.types.pojo.Schema(List.of(field)), allocator);
             var channel = java.nio.channels.Channels.newChannel(java.nio.file.Files.newOutputStream(input))) {
          var timestamps = (org.apache.arrow.vector.TimeStampMicroTZVector) root.getVector("occurred_at");
          timestamps.setSafe(0, 1_700_000_000_123_456L);
          timestamps.setSafe(1, -1L);
          root.setRowCount(2);
          writeArrowBatch(root, channel, fileFormat);
        }
        var jdbc = ArrowFileSupport.read((DuckJob) job, input);
        var appender = ArrowFileSupport.readWithAppender((DuckJob) job, input);
        assertEquals("TIMESTAMP WITH TIME ZONE", jdbc.schema().get(0).typeName());
        assertEquals(jdbc.schema(), appender.schema());
        var rows = jdbc.collect();
        assertEquals(rows, appender.collect());
        assertEquals(java.time.Instant.parse("2023-11-14T22:13:20.123456Z"),
            ((java.time.OffsetDateTime) rows.get(0).get("occurred_at")).toInstant());
        assertEquals(java.time.Instant.parse("1969-12-31T23:59:59.999999Z"),
            ((java.time.OffsetDateTime) rows.get(1).get("occurred_at")).toInstant());
      }
    }
  }

  @Test
  void arrowTimestampOutOfRangeExtremesFailCleanlyAcrossFramingsAndImporters(@TempDir Path temp)
      throws Exception {
    var config = new io.zingg.duckdb.api.RuntimeConfig(
        "jdbc:duckdb:" + temp.resolve("timestamp-extremes.duckdb"), 1, 0, null, 1);
    try (var runtime = new DuckRuntime(config); var job = (DuckJob) runtime.openJob()) {
      int registeredBefore = job.materializedCount();
      for (var unit : List.of(org.apache.arrow.vector.types.TimeUnit.SECOND,
          org.apache.arrow.vector.types.TimeUnit.MILLISECOND)) {
        var field = arrowTimestampField("occurred_at", unit, null);
        for (long extreme : new long[] {Long.MIN_VALUE, Long.MAX_VALUE}) {
          for (boolean fileFormat : new boolean[] {false, true}) {
            Path input = temp.resolve("timestamp-" + unit + "-" + extreme + "-" + fileFormat + ".arrow");
            try (var allocator = new org.apache.arrow.memory.RootAllocator();
                 var root = org.apache.arrow.vector.VectorSchemaRoot.create(
                     new org.apache.arrow.vector.types.pojo.Schema(List.of(field)), allocator);
                 var channel = java.nio.channels.Channels.newChannel(java.nio.file.Files.newOutputStream(input))) {
              if (unit == org.apache.arrow.vector.types.TimeUnit.SECOND)
                ((org.apache.arrow.vector.TimeStampSecVector) root.getVector("occurred_at")).setSafe(0, extreme);
              else
                ((org.apache.arrow.vector.TimeStampMilliVector) root.getVector("occurred_at")).setSafe(0, extreme);
              root.setRowCount(1);
              writeArrowBatch(root, channel, fileFormat);
            }
            for (boolean useAppender : new boolean[] {false, true}) {
              var failure = org.junit.jupiter.api.Assertions.assertThrows(
                  io.zingg.duckdb.api.DuckException.class,
                  () -> ArrowFileSupport.read(job, input, useAppender, importedRows -> {}));
              assertEquals("Arrow IPC ingestion failed: " + input, failure.getMessage());
              assertEquals(registeredBefore, job.materializedCount());
              assertEquals(0L, job.sql("select count(*) as n from information_schema.tables where table_name like 'zd_arrow_%'")
                  .collect().get(0).get("n"), "out-of-range timestamp must not leave a staging table");
            }
          }
        }
      }
    }
  }

  @Test
  void arrowMicroAndNanoTimestampInt64ExtremesPreserveDuckdbMicrosecondPrecision(@TempDir Path temp)
      throws Exception {
    var units = List.of(org.apache.arrow.vector.types.TimeUnit.MICROSECOND,
        org.apache.arrow.vector.types.TimeUnit.NANOSECOND);
    long[] extremes = {Long.MIN_VALUE, Long.MAX_VALUE};
    var fields = units.stream().map(unit -> arrowTimestampField(unit.name().toLowerCase(), unit, null)).toList();
    var expectedByUnit = new java.util.LinkedHashMap<String, List<java.sql.Timestamp>>();
    for (var unit : units) {
      var expected = new java.util.ArrayList<java.sql.Timestamp>();
      for (long value : extremes) {
        java.time.Instant instant = unit == org.apache.arrow.vector.types.TimeUnit.MICROSECOND
            ? java.time.Instant.ofEpochSecond(Math.floorDiv(value, 1_000_000L),
                Math.floorMod(value, 1_000_000L) * 1_000L)
            : java.time.Instant.ofEpochSecond(Math.floorDiv(value, 1_000_000_000L),
                Math.floorMod(value, 1_000_000_000L));
        var local = java.time.LocalDateTime.ofInstant(
            instant.truncatedTo(java.time.temporal.ChronoUnit.MICROS), java.time.ZoneOffset.UTC);
        expected.add(java.sql.Timestamp.valueOf(local));
      }
      expectedByUnit.put(unit.name().toLowerCase(), expected);
    }

    for (boolean fileFormat : new boolean[] {false, true}) {
      Path input = temp.resolve("timestamp-micro-nano-" + fileFormat + ".arrow");
      try (var allocator = new org.apache.arrow.memory.RootAllocator();
           var root = org.apache.arrow.vector.VectorSchemaRoot.create(
               new org.apache.arrow.vector.types.pojo.Schema(fields), allocator);
           var channel = java.nio.channels.Channels.newChannel(java.nio.file.Files.newOutputStream(input))) {
        for (var unit : units) {
          String column = unit.name().toLowerCase();
          if (unit == org.apache.arrow.vector.types.TimeUnit.MICROSECOND) {
            var vector = (org.apache.arrow.vector.TimeStampMicroVector) root.getVector(column);
            vector.setSafe(0, extremes[0]); vector.setSafe(1, extremes[1]); vector.setValueCount(extremes.length);
          } else {
            var vector = (org.apache.arrow.vector.TimeStampNanoVector) root.getVector(column);
            vector.setSafe(0, extremes[0]); vector.setSafe(1, extremes[1]); vector.setValueCount(extremes.length);
          }
        }
        root.setRowCount(extremes.length);
        writeArrowBatch(root, channel, fileFormat);
      }
      var config = new io.zingg.duckdb.api.RuntimeConfig(
          "jdbc:duckdb:" + temp.resolve("timestamp-micro-nano-" + fileFormat + ".duckdb"), 1, 0, null, 1);
      try (var runtime = new DuckRuntime(config); var job = (DuckJob) runtime.openJob()) {
        var jdbc = ArrowFileSupport.read(job, input);
        var appender = ArrowFileSupport.readWithAppender(job, input);
        assertEquals(jdbc.schema(), appender.schema());
        for (var unit : units) {
          String column = unit.name().toLowerCase();
          assertEquals(expectedByUnit.get(column), jdbc.collect().stream()
              .map(row -> row.get(column)).toList(), "timestamp extremes must truncate to DuckDB microseconds");
        }
        assertEquals(jdbc.collect(), appender.collect());
      }
    }
  }

  @Test
  void arrowDecimalDictionaryPreservesMaximumPrecisionAndScaleThroughJdbcAndAppender(@TempDir Path temp)
      throws Exception {
    Path input = temp.resolve("dictionary-decimal-limits.arrow");
    var indexType = new org.apache.arrow.vector.types.pojo.ArrowType.Int(8, true);
    var scaleEncoding = new org.apache.arrow.vector.types.pojo.DictionaryEncoding(71L, false, indexType);
    var precisionEncoding = new org.apache.arrow.vector.types.pojo.DictionaryEncoding(72L, false, indexType);
    var scaleField = new org.apache.arrow.vector.types.pojo.Field("scale_limit",
        new org.apache.arrow.vector.types.pojo.FieldType(true, indexType, scaleEncoding, null), null);
    var precisionField = new org.apache.arrow.vector.types.pojo.Field("precision_limit",
        new org.apache.arrow.vector.types.pojo.FieldType(true, indexType, precisionEncoding, null), null);
    var minimumUnscaled = java.math.BigInteger.TEN.pow(38).subtract(java.math.BigInteger.ONE).negate();
    var maximumUnscaled = minimumUnscaled.negate();
    var expectedScale = List.of(new java.math.BigDecimal("0." + "0".repeat(37) + "1"),
        new java.math.BigDecimal("-0." + "0".repeat(37) + "1"));
    var expectedPrecision = List.of(new java.math.BigDecimal(maximumUnscaled),
        new java.math.BigDecimal(minimumUnscaled));
    try (var allocator = new org.apache.arrow.memory.RootAllocator();
         var root = org.apache.arrow.vector.VectorSchemaRoot.create(
             new org.apache.arrow.vector.types.pojo.Schema(List.of(scaleField, precisionField)), allocator);
         var scaleValues = new org.apache.arrow.vector.DecimalVector("scale-values",
             org.apache.arrow.vector.types.pojo.FieldType.nullable(
                 new org.apache.arrow.vector.types.pojo.ArrowType.Decimal(38, 38, 128)), allocator);
         var precisionValues = new org.apache.arrow.vector.DecimalVector("precision-values",
             org.apache.arrow.vector.types.pojo.FieldType.nullable(
                 new org.apache.arrow.vector.types.pojo.ArrowType.Decimal(38, 0, 128)), allocator);
         var channel = java.nio.channels.Channels.newChannel(java.nio.file.Files.newOutputStream(input))) {
      scaleValues.allocateNew();
      scaleValues.setSafe(0, expectedScale.get(0));
      scaleValues.setSafe(1, expectedScale.get(1));
      scaleValues.setValueCount(2);
      precisionValues.allocateNew();
      precisionValues.setSafe(0, new java.math.BigDecimal(maximumUnscaled));
      precisionValues.setSafe(1, new java.math.BigDecimal(minimumUnscaled));
      precisionValues.setValueCount(2);
      try (var dictionaries = new org.apache.arrow.vector.dictionary.DictionaryProvider.MapDictionaryProvider(
               new org.apache.arrow.vector.dictionary.Dictionary(scaleValues, scaleEncoding),
               new org.apache.arrow.vector.dictionary.Dictionary(precisionValues, precisionEncoding));
           var writer = new org.apache.arrow.vector.ipc.ArrowStreamWriter(root, dictionaries, channel)) {
        var scaleIndices = (org.apache.arrow.vector.TinyIntVector) root.getVector("scale_limit");
        var precisionIndices = (org.apache.arrow.vector.TinyIntVector) root.getVector("precision_limit");
        scaleIndices.setSafe(0, (byte) 0); scaleIndices.setSafe(1, (byte) 1); scaleIndices.setValueCount(2);
        precisionIndices.setSafe(0, (byte) 0); precisionIndices.setSafe(1, (byte) 1); precisionIndices.setValueCount(2);
        root.setRowCount(2);
        writer.start(); writer.writeBatch(); writer.end();
      }
    }

    var config = new io.zingg.duckdb.api.RuntimeConfig(
        "jdbc:duckdb:" + temp.resolve("dictionary-decimal-limits.duckdb"), 1, 0, null, 1);
    try (var runtime = new DuckRuntime(config); var job = (DuckJob) runtime.openJob()) {
      var jdbc = ArrowFileSupport.read(job, input);
      var appender = ArrowFileSupport.readWithAppender(job, input);
      assertEquals(List.of("DECIMAL(38,38)", "DECIMAL(38,0)"),
          jdbc.schema().stream().map(io.zingg.duckdb.api.Column::typeName).toList());
      assertEquals(expectedScale, jdbc.collect().stream().map(row -> row.get("scale_limit")).toList());
      assertEquals(expectedPrecision, jdbc.collect().stream().map(row -> row.get("precision_limit")).toList());
      assertEquals(jdbc.schema(), appender.schema());
      assertEquals(jdbc.collect(), appender.collect());
    }
  }

  @Test
  void arrowUnsignedIntegerDictionariesPreserveMaximumValuesThroughJdbcAndAppender(@TempDir Path temp)
      throws Exception {
    Path input = temp.resolve("dictionary-unsigned.arrow");
    var indexType = new org.apache.arrow.vector.types.pojo.ArrowType.Int(8, true);
    var encodings = java.util.stream.IntStream.range(0, 4)
        .mapToObj(i -> new org.apache.arrow.vector.types.pojo.DictionaryEncoding(61L + i, false, indexType))
        .toList();
    var fields = List.of(
        new org.apache.arrow.vector.types.pojo.Field("u8", new org.apache.arrow.vector.types.pojo.FieldType(true, indexType, encodings.get(0), null), null),
        new org.apache.arrow.vector.types.pojo.Field("u16", new org.apache.arrow.vector.types.pojo.FieldType(true, indexType, encodings.get(1), null), null),
        new org.apache.arrow.vector.types.pojo.Field("u32", new org.apache.arrow.vector.types.pojo.FieldType(true, indexType, encodings.get(2), null), null),
        new org.apache.arrow.vector.types.pojo.Field("u64", new org.apache.arrow.vector.types.pojo.FieldType(true, indexType, encodings.get(3), null), null));
    try (var allocator = new org.apache.arrow.memory.RootAllocator();
         var root = org.apache.arrow.vector.VectorSchemaRoot.create(new org.apache.arrow.vector.types.pojo.Schema(fields), allocator);
         var u8 = new org.apache.arrow.vector.UInt1Vector("u8-values", allocator);
         var u16 = new org.apache.arrow.vector.UInt2Vector("u16-values", allocator);
         var u32 = new org.apache.arrow.vector.UInt4Vector("u32-values", allocator);
         var u64 = new org.apache.arrow.vector.UInt8Vector("u64-values", allocator);
         var channel = java.nio.channels.Channels.newChannel(java.nio.file.Files.newOutputStream(input))) {
      u8.allocateNew(); u8.setSafe(0, 255); u8.setValueCount(1);
      u16.allocateNew(); u16.setSafe(0, 65_535); u16.setValueCount(1);
      u32.allocateNew(); u32.setSafe(0, -1); u32.setValueCount(1);
      u64.allocateNew(); u64.setSafe(0, -1L); u64.setValueCount(1);
      try (var dictionaries = new org.apache.arrow.vector.dictionary.DictionaryProvider.MapDictionaryProvider(
               new org.apache.arrow.vector.dictionary.Dictionary(u8, encodings.get(0)),
               new org.apache.arrow.vector.dictionary.Dictionary(u16, encodings.get(1)),
               new org.apache.arrow.vector.dictionary.Dictionary(u32, encodings.get(2)),
               new org.apache.arrow.vector.dictionary.Dictionary(u64, encodings.get(3)));
           var writer = new org.apache.arrow.vector.ipc.ArrowStreamWriter(root, dictionaries, channel)) {
        for (int column = 0; column < fields.size(); column++) {
          var indices = (org.apache.arrow.vector.TinyIntVector) root.getVector(column);
          indices.setSafe(0, (byte) 0);
          indices.setValueCount(1);
        }
        root.setRowCount(1);
        writer.start(); writer.writeBatch(); writer.end();
      }
    }

    var config = new io.zingg.duckdb.api.RuntimeConfig(
        "jdbc:duckdb:" + temp.resolve("dictionary-unsigned.duckdb"), 1, 0, null, 1);
    try (var runtime = new DuckRuntime(config); var job = (DuckJob) runtime.openJob()) {
      var jdbc = ArrowFileSupport.read(job, input);
      var appender = ArrowFileSupport.readWithAppender(job, input);
      assertEquals("BIGINT", jdbc.schema().stream().filter(column -> column.name().equals("u8")).findFirst().orElseThrow().typeName());
      assertEquals("BIGINT", jdbc.schema().stream().filter(column -> column.name().equals("u16")).findFirst().orElseThrow().typeName());
      assertEquals("BIGINT", jdbc.schema().stream().filter(column -> column.name().equals("u32")).findFirst().orElseThrow().typeName());
      assertEquals("DECIMAL(20,0)", jdbc.schema().stream().filter(column -> column.name().equals("u64")).findFirst().orElseThrow().typeName());
      assertEquals(jdbc.schema(), appender.schema());
      var row = jdbc.collect().get(0);
      assertEquals(255L, ((Number) row.get("u8")).longValue());
      assertEquals(65_535L, ((Number) row.get("u16")).longValue());
      assertEquals(4_294_967_295L, ((Number) row.get("u32")).longValue());
      assertEquals(new java.math.BigDecimal("18446744073709551615"), row.get("u64"));
      assertEquals(jdbc.collect(), appender.collect());
    }
  }

  @Test
  void arrowUnsigned64MaximumIsPreservedExactly(@TempDir Path temp) throws Exception {
    Path input = temp.resolve("uint64.arrow");
    var fields = java.util.List.of(arrowIntegerField("u8", 8, false), arrowIntegerField("u16", 16, false),
        arrowIntegerField("u32", 32, false), arrowIntegerField("u64", 64, false));
    try (var allocator = new org.apache.arrow.memory.RootAllocator();
         var root = org.apache.arrow.vector.VectorSchemaRoot.create(new org.apache.arrow.vector.types.pojo.Schema(fields), allocator);
         var channel = java.nio.channels.Channels.newChannel(java.nio.file.Files.newOutputStream(input));
         var writer = new org.apache.arrow.vector.ipc.ArrowStreamWriter(root, null, channel)) {
      ((org.apache.arrow.vector.UInt1Vector) root.getVector("u8")).setSafe(0, 255);
      ((org.apache.arrow.vector.UInt2Vector) root.getVector("u16")).setSafe(0, 65_535);
      ((org.apache.arrow.vector.UInt4Vector) root.getVector("u32")).setSafe(0, -1);
      ((org.apache.arrow.vector.UInt8Vector) root.getVector("u64")).setSafe(0, -1L);
      root.setRowCount(1);
      writer.start();
      writer.writeBatch();
      writer.end();
    }
    var config = new io.zingg.duckdb.api.RuntimeConfig(
        "jdbc:duckdb:" + temp.resolve("uint64.duckdb"), 1, 0, null, 1);
    try (var runtime = new DuckRuntime(config); var job = runtime.openJob()) {
      var frame = ArrowFileSupport.read((DuckJob) job, input);
      assertEquals("BIGINT", frame.schema().stream().filter(c -> c.name().equals("u8")).findFirst().orElseThrow().typeName());
      assertEquals("BIGINT", frame.schema().stream().filter(c -> c.name().equals("u16")).findFirst().orElseThrow().typeName());
      assertEquals("BIGINT", frame.schema().stream().filter(c -> c.name().equals("u32")).findFirst().orElseThrow().typeName());
      assertEquals("DECIMAL(20,0)", frame.schema().stream().filter(c -> c.name().equals("u64")).findFirst().orElseThrow().typeName());
      var row = frame.collect().get(0);
      assertEquals(255L, ((Number) row.get("u8")).longValue());
      assertEquals(65_535L, ((Number) row.get("u16")).longValue());
      assertEquals(4_294_967_295L, ((Number) row.get("u32")).longValue());
      assertEquals(new java.math.BigDecimal("18446744073709551615"), row.get("u64"));
      var appenderFrame = ArrowFileSupport.readWithAppender((DuckJob) job, input);
      assertEquals(frame.schema(), appenderFrame.schema());
      assertEquals(frame.collect(), appenderFrame.collect());
    }
  }

  @Test
  void arrowSignedIntegerWidthsMatchBetweenJdbcAndAppender(@TempDir Path temp) throws Exception {
    Path input = temp.resolve("signed-widths.arrow");
    var fields = java.util.List.of(arrowIntegerField("i8", 8, true), arrowIntegerField("i16", 16, true),
        arrowIntegerField("i32", 32, true), arrowIntegerField("i64", 64, true));
    try (var allocator = new org.apache.arrow.memory.RootAllocator();
         var root = org.apache.arrow.vector.VectorSchemaRoot.create(new org.apache.arrow.vector.types.pojo.Schema(fields), allocator);
         var channel = java.nio.channels.Channels.newChannel(java.nio.file.Files.newOutputStream(input));
         var writer = new org.apache.arrow.vector.ipc.ArrowStreamWriter(root, null, channel)) {
      ((org.apache.arrow.vector.TinyIntVector) root.getVector("i8")).setSafe(0, Byte.MIN_VALUE);
      ((org.apache.arrow.vector.SmallIntVector) root.getVector("i16")).setSafe(0, Short.MIN_VALUE);
      ((org.apache.arrow.vector.IntVector) root.getVector("i32")).setSafe(0, Integer.MIN_VALUE);
      ((org.apache.arrow.vector.BigIntVector) root.getVector("i64")).setSafe(0, Long.MIN_VALUE);
      ((org.apache.arrow.vector.TinyIntVector) root.getVector("i8")).setSafe(1, Byte.MAX_VALUE);
      ((org.apache.arrow.vector.SmallIntVector) root.getVector("i16")).setSafe(1, Short.MAX_VALUE);
      ((org.apache.arrow.vector.IntVector) root.getVector("i32")).setSafe(1, Integer.MAX_VALUE);
      ((org.apache.arrow.vector.BigIntVector) root.getVector("i64")).setSafe(1, Long.MAX_VALUE);
      root.setRowCount(2);
      writer.start();
      writer.writeBatch();
      writer.end();
    }
    var config = new io.zingg.duckdb.api.RuntimeConfig(
        "jdbc:duckdb:" + temp.resolve("signed-widths.duckdb"), 1, 0, null, 1);
    try (var runtime = new DuckRuntime(config); var job = runtime.openJob()) {
      var jdbcFrame = ArrowFileSupport.read((DuckJob) job, input);
      var appenderFrame = ArrowFileSupport.readWithAppender((DuckJob) job, input);
      assertEquals(java.util.List.of("TINYINT", "SMALLINT", "INTEGER", "BIGINT"),
          jdbcFrame.schema().stream().map(io.zingg.duckdb.api.Column::typeName).toList());
      assertEquals(jdbcFrame.schema(), appenderFrame.schema());
      assertEquals(jdbcFrame.collect(), appenderFrame.collect());
      assertEquals(Byte.MIN_VALUE, jdbcFrame.collect().get(0).get("i8"));
      assertEquals(Short.MIN_VALUE, jdbcFrame.collect().get(0).get("i16"));
      assertEquals(Integer.MIN_VALUE, jdbcFrame.collect().get(0).get("i32"));
      assertEquals(Long.MIN_VALUE, jdbcFrame.collect().get(0).get("i64"));
    }
  }

  @Test
  void arrowHalfSingleAndDoublePrecisionSurviveIngressAppenderAndEgress(@TempDir Path temp) throws Exception {
    Path input = temp.resolve("floating-widths.arrow");
    Path output = temp.resolve("floating-widths-roundtrip.arrow");
    var singleType = new org.apache.arrow.vector.types.pojo.ArrowType.FloatingPoint(
        org.apache.arrow.vector.types.FloatingPointPrecision.SINGLE);
    var halfType = new org.apache.arrow.vector.types.pojo.ArrowType.FloatingPoint(
        org.apache.arrow.vector.types.FloatingPointPrecision.HALF);
    var doubleType = new org.apache.arrow.vector.types.pojo.ArrowType.FloatingPoint(
        org.apache.arrow.vector.types.FloatingPointPrecision.DOUBLE);
    var fields = List.of(
        new org.apache.arrow.vector.types.pojo.Field("half_value",
            org.apache.arrow.vector.types.pojo.FieldType.nullable(halfType), null),
        new org.apache.arrow.vector.types.pojo.Field("single_value",
            org.apache.arrow.vector.types.pojo.FieldType.nullable(singleType), null),
        new org.apache.arrow.vector.types.pojo.Field("double_value",
            org.apache.arrow.vector.types.pojo.FieldType.nullable(doubleType), null));
    try (var allocator = new org.apache.arrow.memory.RootAllocator();
         var root = org.apache.arrow.vector.VectorSchemaRoot.create(
             new org.apache.arrow.vector.types.pojo.Schema(fields), allocator);
         var channel = java.nio.channels.Channels.newChannel(java.nio.file.Files.newOutputStream(input));
         var writer = new org.apache.arrow.vector.ipc.ArrowStreamWriter(root, null, channel)) {
      var half = (org.apache.arrow.vector.Float2Vector) root.getVector("half_value");
      var single = (org.apache.arrow.vector.Float4Vector) root.getVector("single_value");
      var wide = (org.apache.arrow.vector.Float8Vector) root.getVector("double_value");
      half.setSafe(0, (short) 0x3e00); // 1.5
      half.setSafe(1, (short) 0x0001); // Smallest positive binary16 subnormal: 2^-24.
      half.setSafe(2, (short) 0x03ff); // Largest positive subnormal.
      half.setSafe(3, (short) 0x0400); // Smallest positive normal.
      half.setSafe(4, (short) 0x7bff); // Largest finite value.
      half.setSafe(5, (short) 0x8000); // Negative zero.
      half.setSafe(6, (short) 0x7c00); // Positive infinity.
      half.setSafe(7, (short) 0x7e00); // NaN.
      half.setNull(8);
      single.setSafe(0, -123.25f);
      wide.setSafe(0, -123.25d);
      single.setNull(1);
      wide.setSafe(1, 1.0d / 10.0d);
      single.setSafe(2, 0.1f);
      wide.setNull(2);
      root.setRowCount(9);
      writer.start();
      writer.writeBatch();
      writer.end();
    }

    var config = new io.zingg.duckdb.api.RuntimeConfig(
        "jdbc:duckdb:" + temp.resolve("floating-widths.duckdb"), 1, 0, null, 1);
    try (var runtime = new DuckRuntime(config); var job = runtime.openJob()) {
      var jdbcFrame = ArrowFileSupport.read((DuckJob) job, input);
      var appenderFrame = ArrowFileSupport.readWithAppender((DuckJob) job, input);
      assertEquals(List.of("FLOAT", "FLOAT", "DOUBLE"),
          jdbcFrame.schema().stream().map(io.zingg.duckdb.api.Column::typeName).toList());
      assertEquals(jdbcFrame.schema(), appenderFrame.schema());
      assertEquals(jdbcFrame.collect(), appenderFrame.collect());
      assertEquals(-123.25f, jdbcFrame.collect().get(0).get("single_value"));
      assertEquals(1.5f, jdbcFrame.collect().get(0).get("half_value"));
      assertEquals(Math.scalb(1.0f, -24), jdbcFrame.collect().get(1).get("half_value"));
      assertEquals(1023 * Math.scalb(1.0f, -24), jdbcFrame.collect().get(2).get("half_value"));
      assertEquals(Math.scalb(1.0f, -14), jdbcFrame.collect().get(3).get("half_value"));
      assertEquals(65504.0f, jdbcFrame.collect().get(4).get("half_value"));
      assertEquals(Float.floatToRawIntBits(-0.0f), Float.floatToRawIntBits(
          (Float) jdbcFrame.collect().get(5).get("half_value")));
      assertEquals(Float.POSITIVE_INFINITY, jdbcFrame.collect().get(6).get("half_value"));
      org.junit.jupiter.api.Assertions.assertTrue(Float.isNaN(
          (Float) jdbcFrame.collect().get(7).get("half_value")));
      assertEquals(null, jdbcFrame.collect().get(8).get("half_value"));
      assertEquals(1.0d / 10.0d, jdbcFrame.collect().get(1).get("double_value"));

      jdbcFrame.writeArrow(output);
      try (var allocator = new org.apache.arrow.memory.RootAllocator();
           var reader = new org.apache.arrow.vector.ipc.ArrowStreamReader(
               java.nio.file.Files.newInputStream(output), allocator)) {
        org.junit.jupiter.api.Assertions.assertTrue(reader.loadNextBatch());
        var schema = reader.getVectorSchemaRoot().getSchema();
        assertEquals(org.apache.arrow.vector.types.FloatingPointPrecision.SINGLE,
            ((org.apache.arrow.vector.types.pojo.ArrowType.FloatingPoint)
                schema.findField("half_value").getType()).getPrecision(),
            "DuckDB FLOAT egress intentionally widens Arrow HALF to SINGLE");
        assertEquals(org.apache.arrow.vector.types.FloatingPointPrecision.SINGLE,
            ((org.apache.arrow.vector.types.pojo.ArrowType.FloatingPoint)
                schema.findField("single_value").getType()).getPrecision());
        assertEquals(org.apache.arrow.vector.types.FloatingPointPrecision.DOUBLE,
            ((org.apache.arrow.vector.types.pojo.ArrowType.FloatingPoint)
                schema.findField("double_value").getType()).getPrecision());
      }
      var roundTrip = ArrowFileSupport.read((DuckJob) job, output);
      assertEquals(jdbcFrame.schema(), roundTrip.schema());
      assertEquals(jdbcFrame.collect(), roundTrip.collect());
    }
  }

  private static org.apache.arrow.vector.types.pojo.Field arrowIntegerField(String name, int width, boolean signed) {
    return new org.apache.arrow.vector.types.pojo.Field(name,
        org.apache.arrow.vector.types.pojo.FieldType.nullable(
            new org.apache.arrow.vector.types.pojo.ArrowType.Int(width, signed)), null);
  }

  private static org.apache.arrow.vector.types.pojo.Field arrowTimestampField(
      String name, org.apache.arrow.vector.types.TimeUnit unit, String timezone) {
    return new org.apache.arrow.vector.types.pojo.Field(name,
        org.apache.arrow.vector.types.pojo.FieldType.nullable(
            new org.apache.arrow.vector.types.pojo.ArrowType.Timestamp(unit, timezone)), null);
  }

  private static byte[] binaryBytes(Object value) throws java.sql.SQLException {
    if (value instanceof byte[] bytes) return bytes;
    if (value instanceof java.sql.Blob blob) return blob.getBytes(1, Math.toIntExact(blob.length()));
    throw new AssertionError("expected binary JDBC value, got " + value.getClass().getName());
  }

  private static void assertNoInputSnapshotDirectories(DuckJob job) throws java.io.IOException {
    Path tempDirectory=job.tempDirectory();
    if(!java.nio.file.Files.exists(tempDirectory)) return;
    try(var entries=java.nio.file.Files.list(tempDirectory)){
      assertTrue(entries.noneMatch(path->path.getFileName().toString().startsWith(".zingg-input-")),
          "bounded input snapshot staging must be removed after import completes or fails");
    }
  }

  private static void writeEmptyArrow(Path path, boolean fileFormat, boolean includeEmptyBatch)
      throws Exception {
    try (var allocator = new org.apache.arrow.memory.RootAllocator();
         var root = org.apache.arrow.vector.VectorSchemaRoot.create(
             new org.apache.arrow.vector.types.pojo.Schema(List.of(
                 new org.apache.arrow.vector.types.pojo.Field("value",
                     org.apache.arrow.vector.types.pojo.FieldType.nullable(
                         new org.apache.arrow.vector.types.pojo.ArrowType.Int(32, true)), null))), allocator);
         var channel = java.nio.channels.Channels.newChannel(java.nio.file.Files.newOutputStream(path))) {
      if (fileFormat) {
        try (var writer = new org.apache.arrow.vector.ipc.ArrowFileWriter(root, null, channel)) {
          writer.start();
          if (includeEmptyBatch) writer.writeBatch();
          writer.end();
        }
      } else {
        try (var writer = new org.apache.arrow.vector.ipc.ArrowStreamWriter(root, null, channel)) {
          writer.start();
          if (includeEmptyBatch) writer.writeBatch();
          writer.end();
        }
      }
    }
  }

  private static void writeEmptyArrow(Path path, org.apache.arrow.vector.types.pojo.Field field,
      boolean fileFormat) throws Exception {
    try (var allocator = new org.apache.arrow.memory.RootAllocator();
         var root = org.apache.arrow.vector.VectorSchemaRoot.create(
             new org.apache.arrow.vector.types.pojo.Schema(List.of(field)), allocator);
         var channel = java.nio.channels.Channels.newChannel(java.nio.file.Files.newOutputStream(path))) {
      if (fileFormat) {
        try (var writer = new org.apache.arrow.vector.ipc.ArrowFileWriter(root, null, channel)) {
          writer.start();
          writer.end();
        }
      } else {
        try (var writer = new org.apache.arrow.vector.ipc.ArrowStreamWriter(root, null, channel)) {
          writer.start();
          writer.end();
        }
      }
    }
  }

  private static void writeArrowBatch(org.apache.arrow.vector.VectorSchemaRoot root,
      java.nio.channels.WritableByteChannel channel, boolean fileFormat) throws Exception {
    if (fileFormat) {
      try (var writer = new org.apache.arrow.vector.ipc.ArrowFileWriter(root, null, channel)) {
        writer.start(); writer.writeBatch(); writer.end();
      }
    } else {
      try (var writer = new org.apache.arrow.vector.ipc.ArrowStreamWriter(root, null, channel)) {
        writer.start(); writer.writeBatch(); writer.end();
      }
    }
  }

  @Test
  void arrowEgressRejectsUnsupportedNestedTypesInsteadOfCoercing(@TempDir Path temp) {
    var config = new io.zingg.duckdb.api.RuntimeConfig(
        "jdbc:duckdb:" + temp.resolve("arrow-nested.duckdb"), 1, 0, null, 1);
    Path output = temp.resolve("nested.arrow");
    try (var runtime = new DuckRuntime(config); var job = runtime.openJob()) {
      var error = org.junit.jupiter.api.Assertions.assertThrows(io.zingg.duckdb.api.DuckException.class,
          () -> job.sql("select [1, 2, 3] as values").writeArrow(output));
      org.junit.jupiter.api.Assertions.assertTrue(error.getMessage().contains("unsupported JDBC type"));
      org.junit.jupiter.api.Assertions.assertFalse(java.nio.file.Files.exists(output));
    }
  }

  @Test
  void arrowFileFramingIsAcceptedOnIngress(@TempDir Path temp) throws Exception {
    var config = new io.zingg.duckdb.api.RuntimeConfig(
        "jdbc:duckdb:" + temp.resolve("arrow-file.duckdb"), 1, 0, null, 1);
    Path input = temp.resolve("external.arrow");
    try (var allocator = new org.apache.arrow.memory.RootAllocator();
         var root = org.apache.arrow.vector.VectorSchemaRoot.create(
             new org.apache.arrow.vector.types.pojo.Schema(java.util.List.of(
                 new org.apache.arrow.vector.types.pojo.Field("value",
                     org.apache.arrow.vector.types.pojo.FieldType.nullable(
                         new org.apache.arrow.vector.types.pojo.ArrowType.Int(32, true)), null))), allocator);
         var channel = java.nio.channels.Channels.newChannel(java.nio.file.Files.newOutputStream(input));
         var writer = new org.apache.arrow.vector.ipc.ArrowFileWriter(root, null, channel)) {
      var vector = (org.apache.arrow.vector.IntVector) root.getVector("value");
      vector.setSafe(0, 42);
      root.setRowCount(1);
      writer.start();
      writer.writeBatch();
      writer.end();
    }
    try (var runtime = new DuckRuntime(config); var job = runtime.openJob()) {
      var frame = new DuckPipeReader(job).read(java.util.List.of(input), DuckPipeReader.UnionMode.MATCH_BY_NAME);
      assertEquals(1, frame.count());
      assertEquals(42, ((Number)frame.collect().get(0).get("value")).intValue());
    }
  }

  @Test
  void arrowIngressBudgetFailureDropsPartialJobTableImmediately(@TempDir Path temp) throws Exception {
    Path input = temp.resolve("two-rows.arrow");
    var sourceConfig = new io.zingg.duckdb.api.RuntimeConfig(
        "jdbc:duckdb:" + temp.resolve("arrow-source.duckdb"), 1, 0, null, 1);
    try (var runtime = new DuckRuntime(sourceConfig); var job = runtime.openJob()) {
      job.sql("select * from range(2) t(value)").writeArrow(input);
    }

    var targetConfig = new io.zingg.duckdb.api.RuntimeConfig(
        "jdbc:duckdb:" + temp.resolve("arrow-target.duckdb"), 1, 0, null, 1);
    try (var runtime = new DuckRuntime(targetConfig);
         var job = (DuckJob) runtime.openJob(new ResourceBudget(0, 1, 0, 0, 0, 0))) {
      var reader = new DuckPipeReader(job);
      int registeredBefore = job.materializedCount();
      var failure = org.junit.jupiter.api.Assertions.assertThrows(
          io.zingg.duckdb.api.DuckException.class,
          () -> reader.read(java.util.List.of(input), DuckPipeReader.UnionMode.MATCH_BY_NAME));
      assertEquals("row budget exceeded: 2 > 1", failure.getMessage());
      assertEquals(registeredBefore, job.materializedCount(),
          "failed JDBC ingress must release its materialization registration immediately");
      var appenderFailure = org.junit.jupiter.api.Assertions.assertThrows(
          io.zingg.duckdb.api.DuckException.class, () -> ArrowFileSupport.readWithAppender(job, input));
      assertEquals("row budget exceeded: 2 > 1", appenderFailure.getMessage());
      assertEquals(registeredBefore, job.materializedCount(),
          "failed Appender ingress must release its materialization registration immediately");
      try (var statement = job.connection().createStatement();
           var rows = statement.executeQuery("select count(*) from information_schema.tables where table_schema='" + job.schemaName() + "'")) {
        org.junit.jupiter.api.Assertions.assertTrue(rows.next());
        assertEquals(0, rows.getInt(1), "failed Arrow ingress must not leave a registered table");
      }
    }
  }

  @Test
  void arrowInputByteBudgetIsAggregateAndPreflightedBeforeAnyImport(@TempDir Path temp) throws Exception {
    Path first = temp.resolve("first.arrow");
    Path second = temp.resolve("second.arrow");
    writeEmptyArrow(first, false, false);
    writeEmptyArrow(second, false, false);
    long firstBytes = java.nio.file.Files.size(first);
    long secondBytes = java.nio.file.Files.size(second);
    assertEquals(firstBytes, secondBytes);
    long totalBytes = Math.addExact(firstBytes, secondBytes);

    var config = new io.zingg.duckdb.api.RuntimeConfig(
        "jdbc:duckdb:" + temp.resolve("arrow-input-byte-budget.duckdb"), 1, 0, null, 1);
    try (var runtime = new DuckRuntime(config); var job = (DuckJob) runtime.openJob()) {
      int registeredBefore = job.materializedCount();
      long limit = totalBytes - 1;
      var failure = org.junit.jupiter.api.Assertions.assertThrows(io.zingg.duckdb.api.DuckException.class,
          () -> new DuckPipeReader(job, null, new ResourceBudget(0, 0, limit, 0, 0, 0))
              .read(List.of(first, second), DuckPipeReader.UnionMode.MATCH_BY_NAME, false));
      assertEquals("input byte budget exceeded: " + totalBytes + " > " + limit, failure.getMessage());
      assertEquals(registeredBefore, job.materializedCount(),
          "aggregate byte-budget rejection must occur before importing the first file");
      assertEquals(0L, job.sql("select count(*) as n from information_schema.tables where table_name like 'zd_arrow_%'")
          .collect().get(0).get("n"));

      var exactlyAtLimit = new DuckPipeReader(job, null,
          new ResourceBudget(0, 0, totalBytes, 0, 0, 0))
          .read(List.of(first, second), DuckPipeReader.UnionMode.MATCH_BY_NAME, false);
      assertEquals(0, exactlyAtLimit.count(), "the exact aggregate byte limit is inclusive");
      assertNoInputSnapshotDirectories(job);
    }
  }

  @Test
  void inputFileMutationAfterBytePreflightIsRejectedBeforeImport(@TempDir Path temp) throws Exception {
    Path input = temp.resolve("mutable.csv");
    java.nio.file.Files.writeString(input, "id,value\n1,a\n");
    var config = new io.zingg.duckdb.api.RuntimeConfig(
        "jdbc:duckdb:" + temp.resolve("input-mutation.duckdb"), 1, 0, null, 1);
    try (var runtime = new DuckRuntime(config); var job = (DuckJob) runtime.openJob()) {
      int registeredBefore = job.materializedCount();
      long allowed = java.nio.file.Files.size(input) + 4;
      var budget = new ResourceBudget(0, 0, allowed, 0, 0, 0);
      var reader = new DuckPipeReader(job, null, budget, () -> {
        try {
          java.nio.file.Files.writeString(input, "id,value\n1,a\n2,this-content-grew-after-preflight\n");
        } catch (java.io.IOException e) {
          throw new java.io.UncheckedIOException(e);
        }
      });
      var failure = org.junit.jupiter.api.Assertions.assertThrows(io.zingg.duckdb.api.DuckException.class,
          () -> reader.read(List.of(input), DuckPipeReader.UnionMode.MATCH_BY_NAME, false));
      assertEquals("input changed after byte-budget preflight: " + input.toAbsolutePath().normalize(),
          failure.getMessage());
      assertEquals(registeredBefore, job.materializedCount());
      assertEquals(0L, job.sql("select count(*) as n from information_schema.tables where table_name like 'zd_arrow_%'")
          .collect().get(0).get("n"));
    }
  }

  @Test
  void arrowIngressLaterBatchBudgetFailureRollsBackBothImportPaths(@TempDir Path temp) throws Exception {
    Path input = temp.resolve("later-batch-over-budget.arrow");
    var indexType = new org.apache.arrow.vector.types.pojo.ArrowType.Int(8, true);
    var encoding = new org.apache.arrow.vector.types.pojo.DictionaryEncoding(61L, false, indexType);
    try (var allocator = new org.apache.arrow.memory.RootAllocator();
         var root = org.apache.arrow.vector.VectorSchemaRoot.create(
             new org.apache.arrow.vector.types.pojo.Schema(java.util.List.of(
                 new org.apache.arrow.vector.types.pojo.Field("value",
                     new org.apache.arrow.vector.types.pojo.FieldType(true, indexType, encoding, null), null))), allocator);
         var dictionaryVector = new org.apache.arrow.vector.IntVector("value-dictionary", allocator);
         var channel = java.nio.channels.Channels.newChannel(java.nio.file.Files.newOutputStream(input));
         var dictionaries = new org.apache.arrow.vector.dictionary.DictionaryProvider.MapDictionaryProvider(
             new org.apache.arrow.vector.dictionary.Dictionary(dictionaryVector, encoding));
         var writer = new org.apache.arrow.vector.ipc.ArrowStreamWriter(root, dictionaries, channel)) {
      dictionaryVector.allocateNew();
      dictionaryVector.setSafe(0, 10);
      dictionaryVector.setSafe(1, 20);
      dictionaryVector.setValueCount(2);
      var vector = (org.apache.arrow.vector.TinyIntVector) root.getVector("value");
      vector.allocateNew();
      writer.start();
      vector.setSafe(0, 0);
      root.setRowCount(1);
      writer.writeBatch();
      vector.setSafe(0, 1);
      root.setRowCount(1);
      writer.writeBatch();
      writer.end();
    }

    var config = new io.zingg.duckdb.api.RuntimeConfig(
        "jdbc:duckdb:" + temp.resolve("later-batch-budget.duckdb"), 1, 0, null, 1);
    try (var runtime = new DuckRuntime(config);
         var job = (DuckJob) runtime.openJob(new ResourceBudget(0, 1, 0, 0, 0, 0))) {
      int registeredBefore = job.materializedCount();
      for (boolean useAppender : new boolean[] {false, true}) {
        var failure = org.junit.jupiter.api.Assertions.assertThrows(
            io.zingg.duckdb.api.DuckException.class,
            () -> {
              if (useAppender) ArrowFileSupport.readWithAppender(job, input);
              else ArrowFileSupport.read(job, input);
            });
        assertEquals("row budget exceeded: 2 > 1", failure.getMessage());
        assertEquals(registeredBefore, job.materializedCount(),
            "later-batch failure must remove the job's materialization registration");
        try (var statement = job.connection().createStatement();
             var rows = statement.executeQuery("select count(*) from information_schema.tables where table_schema='" + job.schemaName() + "'")) {
          org.junit.jupiter.api.Assertions.assertTrue(rows.next());
          assertEquals(0, rows.getInt(1), "later-batch failure must drop the partially populated table immediately");
        }
      }
    }
  }

  @Test
  void arrowIngressUnexpectedFailureAfterCommittedBatchRollsBackBothImportPaths(@TempDir Path temp)
      throws Exception {
    Path input = temp.resolve("observer-failure.arrow");
    try (var allocator = new org.apache.arrow.memory.RootAllocator();
         var root = org.apache.arrow.vector.VectorSchemaRoot.create(
             new org.apache.arrow.vector.types.pojo.Schema(java.util.List.of(
                 new org.apache.arrow.vector.types.pojo.Field("value",
                     org.apache.arrow.vector.types.pojo.FieldType.nullable(
                         new org.apache.arrow.vector.types.pojo.ArrowType.Int(32, true)), null))), allocator);
         var channel = java.nio.channels.Channels.newChannel(java.nio.file.Files.newOutputStream(input));
         var writer = new org.apache.arrow.vector.ipc.ArrowStreamWriter(root, null, channel)) {
      var vector = (org.apache.arrow.vector.IntVector) root.getVector("value");
      writer.start();
      vector.setSafe(0, 10);
      root.setRowCount(1);
      writer.writeBatch();
      vector.setSafe(0, 20);
      root.setRowCount(1);
      writer.writeBatch();
      writer.end();
    }

    var config = new io.zingg.duckdb.api.RuntimeConfig(
        "jdbc:duckdb:" + temp.resolve("observer-failure.duckdb"), 1, 0, null, 1);
    try (var runtime = new DuckRuntime(config); var job = (DuckJob) runtime.openJob()) {
      int registeredBefore = job.materializedCount();
      for (boolean useAppender : new boolean[] {false, true}) {
        var failure = org.junit.jupiter.api.Assertions.assertThrows(
            io.zingg.duckdb.api.DuckException.class,
            () -> ArrowFileSupport.read(job, input, useAppender, importedRows -> {
              if (importedRows == 1) throw new java.io.IOException("injected post-batch failure");
            }));
        assertEquals("Arrow IPC ingestion failed: " + input, failure.getMessage());
        org.junit.jupiter.api.Assertions.assertInstanceOf(java.io.IOException.class, failure.getCause());
        assertEquals("injected post-batch failure", failure.getCause().getMessage());
        assertEquals(registeredBefore, job.materializedCount(),
            "post-batch failure must release materialization registration");
        try (var statement = job.connection().createStatement();
             var rows = statement.executeQuery("select count(*) from information_schema.tables where table_schema='" + job.schemaName() + "'")) {
          org.junit.jupiter.api.Assertions.assertTrue(rows.next());
          assertEquals(0, rows.getInt(1), "post-batch failure must remove the committed partial table");
        }
      }
    }
  }

  @Test
  void arrowIngressInterruptionPreservesInterruptAndRollsBackBothImportPaths(@TempDir Path temp)
      throws Exception {
    Path input = temp.resolve("interrupted.arrow");
    try (var allocator = new org.apache.arrow.memory.RootAllocator();
         var root = org.apache.arrow.vector.VectorSchemaRoot.create(
             new org.apache.arrow.vector.types.pojo.Schema(java.util.List.of(
                 new org.apache.arrow.vector.types.pojo.Field("value",
                     org.apache.arrow.vector.types.pojo.FieldType.nullable(
                         new org.apache.arrow.vector.types.pojo.ArrowType.Int(32, true)), null))), allocator);
         var channel = java.nio.channels.Channels.newChannel(java.nio.file.Files.newOutputStream(input));
         var writer = new org.apache.arrow.vector.ipc.ArrowStreamWriter(root, null, channel)) {
      var vector = (org.apache.arrow.vector.IntVector) root.getVector("value");
      writer.start();
      vector.setSafe(0, 10);
      root.setRowCount(1);
      writer.writeBatch();
      vector.setSafe(0, 20);
      root.setRowCount(1);
      writer.writeBatch();
      writer.end();
    }

    var config = new io.zingg.duckdb.api.RuntimeConfig(
        "jdbc:duckdb:" + temp.resolve("interrupted.duckdb"), 1, 0, null, 1);
    try (var runtime = new DuckRuntime(config); var job = (DuckJob) runtime.openJob()) {
      int registeredBefore = job.materializedCount();
      for (boolean useAppender : new boolean[] {false, true}) {
        try {
          var failure = org.junit.jupiter.api.Assertions.assertThrows(
              io.zingg.duckdb.api.DuckException.class,
              () -> ArrowFileSupport.read(job, input, useAppender, importedRows -> {
                if (importedRows == 1) throw new InterruptedException("injected Arrow cancellation");
              }));
          assertEquals("Arrow IPC ingestion failed: " + input, failure.getMessage());
          org.junit.jupiter.api.Assertions.assertInstanceOf(InterruptedException.class, failure.getCause());
          org.junit.jupiter.api.Assertions.assertTrue(Thread.currentThread().isInterrupted(),
              "interrupted status must survive Arrow cleanup and exception wrapping");
          assertEquals(registeredBefore, job.materializedCount(),
              "interrupted ingestion must release its materialization registration");
          try (var statement = job.connection().createStatement();
               var rows = statement.executeQuery("select count(*) from information_schema.tables where table_schema='"
                   + job.schemaName() + "'")) {
            org.junit.jupiter.api.Assertions.assertTrue(rows.next());
            assertEquals(0, rows.getInt(1), "interrupted ingestion must drop the partially populated table");
          }
        } finally {
          Thread.interrupted();
        }
      }
    }
  }

  @Test
  void requestCancellationDropsPartialArrowMaterialization(@TempDir Path temp) throws Exception {
    Path input = temp.resolve("request-cancel.arrow");
    try (var allocator = new org.apache.arrow.memory.RootAllocator();
         var root = org.apache.arrow.vector.VectorSchemaRoot.create(
             new org.apache.arrow.vector.types.pojo.Schema(List.of(
                 new org.apache.arrow.vector.types.pojo.Field("value",
                     org.apache.arrow.vector.types.pojo.FieldType.nullable(
                         new org.apache.arrow.vector.types.pojo.ArrowType.Int(32, true)), null))), allocator);
         var channel = java.nio.channels.Channels.newChannel(java.nio.file.Files.newOutputStream(input));
         var writer = new org.apache.arrow.vector.ipc.ArrowStreamWriter(root, null, channel)) {
      var vector = (org.apache.arrow.vector.IntVector) root.getVector("value");
      writer.start();
      vector.setSafe(0, 10);
      root.setRowCount(1);
      writer.writeBatch();
      vector.setSafe(0, 20);
      root.setRowCount(1);
      writer.writeBatch();
      writer.end();
    }

    var config = new io.zingg.duckdb.api.RuntimeConfig(
        "jdbc:duckdb:" + temp.resolve("request-cancel-arrow.duckdb"), 1, 0, null, 1);
    try (var runtime = new DuckRuntime(config)) {
      for (boolean useAppender : new boolean[] {false, true}) {
        var cancellation = new DuckCancellation();
        try (var ignored = DuckCancellation.install(cancellation);
             var job = (DuckJob) runtime.openJob()) {
          int registeredBefore = job.materializedCount();
          var failure = org.junit.jupiter.api.Assertions.assertThrows(
              io.zingg.duckdb.api.DuckException.class,
              () -> ArrowFileSupport.read(job, input, useAppender, importedRows -> {
                if (importedRows == 1) cancellation.cancel();
              }));
          assertEquals("operation cancelled", failure.getMessage());
          assertEquals(registeredBefore, job.materializedCount());
          try (var statement = job.connection().createStatement();
               var rows = statement.executeQuery("SELECT count(*) FROM information_schema.tables WHERE table_schema='"
                   + job.schemaName() + "'")) {
            org.junit.jupiter.api.Assertions.assertTrue(rows.next());
            assertEquals(0, rows.getInt(1),
                "request cancellation must drop committed partial Arrow rows before returning");
          }
        }
      }
    }
  }

  @Test
  void predicatePartitionHasExplicitNameAndAlias(@TempDir Path temp) {
    var config = new io.zingg.duckdb.api.RuntimeConfig(
        "jdbc:duckdb:" + temp.resolve("partition.duckdb"), 1, 0, null, 1);
    try (var runtime = new DuckRuntime(config); var job = runtime.openJob()) {
      var frame = job.sql("select * from range(4) t(value)");
      var named = frame.partitionByPredicate(new io.zingg.duckdb.engine.DuckExpr("value % 2 = 0"));
      var alias = frame.split(new io.zingg.duckdb.engine.DuckExpr("value % 2 = 0"));
      assertEquals(2, named.matching().count());
      assertEquals(2, named.remaining().count());
      assertEquals(named.matching().count(), alias.matching().count());
      assertEquals(named.remaining().count(), alias.remaining().count());
    }
  }
}
