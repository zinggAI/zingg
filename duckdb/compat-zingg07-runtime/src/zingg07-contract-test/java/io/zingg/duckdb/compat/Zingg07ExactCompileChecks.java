package io.zingg.duckdb.compat;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNull;
import io.zingg.duckdb.api.DuckException;
import io.zingg.duckdb.api.Frame;
import io.zingg.duckdb.api.Row;
import io.zingg.duckdb.api.RuntimeConfig;
import io.zingg.duckdb.engine.DuckRuntime;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import zingg.common.client.ZFrame;
import zingg.common.client.util.DSUtil;
import zingg.common.client.util.PipeUtilBase;

/** Compiled only by the opt-in exact-contract profile. */
class Zingg07ExactCompileChecks {
  @Test
  void exactV07SymbolsCompileAgainstPinnedCapsule() {
    assertNotNull(ZFrame.class);
    assertNotNull(DSUtil.class);
    assertNotNull(PipeUtilBase.class);
  }

  @Test
  void everyPinnedAbstractOverloadHasADeclaredDisposition() {
    var methods = Arrays.stream(ZFrame.class.getDeclaredMethods())
        .filter(method -> Modifier.isAbstract(method.getModifiers())).toList();
    assertEquals(93, methods.size());
    for (var method : methods) assertNotNull(ZFrameDuckAdapter.disposition(method), method.toGenericString());
    assertEquals(0, methods.stream().filter(method -> ZFrameDuckAdapter.disposition(method).startsWith("UNSUPPORTED:")).count());
    assertEquals(0, methods.stream().filter(method -> ZFrameDuckAdapter.disposition(method).startsWith("CONDITIONAL:")).count());
    assertEquals(93, methods.stream().filter(method -> ZFrameDuckAdapter.disposition(method).startsWith("IMPLEMENTED:")).count());
    assertTrue(methods.stream().filter(method -> method.getName().equals("as"))
        .allMatch(method -> ZFrameDuckAdapter.disposition(method).startsWith("IMPLEMENTED:")));
  }

  @Test
  void adapterMapsDuckFrameOperationsIncludingReplacementSampling() throws Exception {
    var config = new RuntimeConfig("jdbc:duckdb:", 1, 0, null, 1);
    try (var runtime = new DuckRuntime(config); var job = runtime.openJob()) {
      Frame source = job.sql("SELECT * FROM (VALUES (1, 'a'), (2, 'a'), (3, 'b')) t(id, block)");
      ZFrame<Frame, Row, DuckZColumn> frame = ZFrameDuckAdapter.wrap(source);
      assertEquals(3, frame.count());
      assertTrue(frame.showSchema().startsWith("StructType("));
      assertEquals("IntegerType", frame.fields()[0].getDataType());
      assertEquals("StringType", frame.fields()[1].getDataType());
      assertEquals(1, frame.filter(frame.equalTo("id", 1)).count());
      assertEquals(1, frame.filter(frame.equalTo("id", 1.0d)).count());
      assertEquals(2, frame.filter(frame.equalTo("block", "a")).count());
      Row accessRow = frame.head();
      assertEquals(1, frame.getAsInt(accessRow, "id"));
      assertEquals("a", frame.getAsString(accessRow, "block"));
      assertThrows(ClassCastException.class, () -> frame.getAsLong(accessRow, "id"));
      ZFrame<Frame, Row, DuckZColumn> typedAccess = ZFrameDuckAdapter.wrap(job.sql(
          "SELECT 1 AS int_value, CAST(2 AS BIGINT) AS long_value, CAST(3 AS DOUBLE) AS double_value, "
              + "CAST(NULL AS INTEGER) AS null_value"));
      Row typedAccessRow = typedAccess.head();
      assertEquals(1, typedAccess.getAsInt(typedAccessRow, "int_value"));
      assertEquals(2L, typedAccess.getAsLong(typedAccessRow, "long_value"));
      assertEquals(3.0d, typedAccess.getAsDouble(typedAccessRow, "double_value"));
      assertThrows(NullPointerException.class, () -> typedAccess.getAsInt(typedAccessRow, "null_value"));
      assertThrows(java.util.NoSuchElementException.class, () -> ZFrameDuckAdapter.wrap(
          job.sql("SELECT 1 AS value WHERE FALSE")).head());
      assertEquals(1, frame.filter(frame.notEqual("block", "a")).count());
      assertEquals(2, frame.filter(frame.gt("id", 1.0d)).count());
      assertEquals(0, frame.filter(frame.gt(frame.col("id"), frame.col("id"))).count());
      assertEquals(2, frame.filter(frame.equalTo("id", 1)).union(
          ZFrameDuckAdapter.wrap(job.sql("SELECT * FROM (VALUES (2, 'a')) t(id, block)"))).count());
      assertEquals(3, frame.withColumn("copy", frame.col("id")).select("copy").count());
      var aliasedProjection = frame.select(List.of(frame.col("id").as("renamed_id")));
      assertEquals(List.of("renamed_id"), Arrays.asList(aliasedProjection.columns()));
      assertEquals(3, aliasedProjection.count());
      assertEquals(2, frame.dropDuplicates(new String[] {"block"}).count());
      ZFrame<Frame, Row, DuckZColumn> right = ZFrameDuckAdapter.wrap(
          job.sql("SELECT 1 AS id, 'matched' AS detail"));
      var leftJoin = frame.join(right, "id", false, ZFrame.LEFT_JOIN);
      assertEquals(3, leftJoin.count());
      assertEquals(List.of("id", "block", "id", "detail"), Arrays.asList(leftJoin.columns()));
      var usingJoin = frame.joinOnCol(right, "id");
      assertEquals(1, usingJoin.count());
      assertEquals(List.of("id", "block", "detail"), Arrays.asList(usingJoin.columns()));
      var expressionJoin = frame.join(right,
          frame.equalTo(frame.col("id"), right.col("id").as("z_id")), ZFrame.INNER_JOIN);
      assertEquals(1, expressionJoin.count());
      var expressionJoinOnCol = frame.joinOnCol(right,
          frame.equalTo(frame.col("id"), right.col("id")));
      assertEquals(1, expressionJoinOnCol.count());
      assertEquals(2, frame.filterInCond("id", ZFrameDuckAdapter.wrap(
          job.sql("SELECT 1 AS wanted UNION ALL SELECT 3 AS wanted")), "wanted").count());
      assertEquals(2, frame.groupByCount("block", "z_count").count());
      ZFrame<Frame, Row, DuckZColumn> nullableGroups = ZFrameDuckAdapter.wrap(
          job.sql("SELECT * FROM (VALUES (NULL::VARCHAR), ('x'), ('x')) t(group_key)"));
      var groupedRows = nullableGroups.groupByCount("group_key", "row_count").collectAsList();
      assertEquals(2, groupedRows.size());
      var nullKeyGroup = groupedRows.stream().filter(row -> row.get("group_key") == null).findFirst().orElseThrow();
      assertEquals(1L, ((Number) nullKeyGroup.get("row_count")).longValue());
      var populatedGroup = groupedRows.stream().filter(row -> "x".equals(row.get("group_key"))).findFirst().orElseThrow();
      assertEquals(2L, ((Number) populatedGroup.get("row_count")).longValue());
      ZFrame<Frame, Row, DuckZColumn> nullableCompositeGroups = ZFrameDuckAdapter.wrap(
          job.sql("SELECT * FROM (VALUES (NULL::VARCHAR, 'x'), (NULL::VARCHAR, 'y'), ('p', 'x')) t(primary_group, secondary_group)"));
      var compositeRows = nullableCompositeGroups.groupByCount(
          "primary_group", "secondary_group", "non_null_primary_count").collectAsList();
      assertEquals(3, compositeRows.size());
      var nullCompositeGroup = compositeRows.stream().filter(row -> row.get("primary_group") == null).findFirst().orElseThrow();
      assertEquals(0L, ((Number) nullCompositeGroup.get("non_null_primary_count")).longValue());
      var nonNullCompositeGroup = compositeRows.stream().filter(row -> "p".equals(row.get("primary_group"))).findFirst().orElseThrow();
      assertEquals(1L, ((Number) nonNullCompositeGroup.get("non_null_primary_count")).longValue());
      assertEquals(1, frame.intersect(ZFrameDuckAdapter.wrap(
          job.sql("SELECT * FROM (VALUES (1, 'a')) t(id, block)"))).count());
      assertEquals(List.of("id", "block"), Arrays.asList(frame.columns()));
      assertEquals(1, frame.fieldIndex("block"));
      var withColumns = frame.withColumns(new String[] {"id_repeat", "block_repeat"}, new DuckZColumn[] {
          frame.col("id"), frame.col("block")});
      assertEquals(List.of("id", "block", "id_repeat", "block_repeat"), Arrays.asList(withColumns.columns()));
      assertEquals(3, withColumns.count());
      ZFrame<Frame, Row, DuckZColumn> renamed = frame.toDF(new String[] {"record_id", "group_name"});
      assertEquals(List.of("record_id", "group_name"), Arrays.asList(renamed.columns()));
      assertEquals(0, renamed.fieldIndex("record_id"));
      assertEquals(1, renamed.fieldIndex("group_name"));
      assertEquals(List.of("group_name"), Arrays.asList(renamed.select("group_name").columns()));
      assertEquals("a", renamed.select("group_name").getAsString(renamed.select("group_name").head(), "group_name"));
      assertEquals("a", renamed.getAsString(renamed.head(), "group_name"));
      var renamedColumnProjection = renamed.select(List.of(renamed.col("group_name")));
      assertEquals(List.of("group_name"), Arrays.asList(renamedColumnProjection.columns()));
      assertEquals("a", renamedColumnProjection.getAsString(renamedColumnProjection.head(), "group_name"));
      var aliased = renamed.as("records");
      assertEquals(Arrays.asList(renamed.columns()), Arrays.asList(aliased.columns()));
      assertEquals(renamed.collectAsList(), aliased.collectAsList());
      var aliasLeft = frame.as("left_records");
      var aliasRight = frame.as("right_records");
      var aliasSelfJoin = aliasLeft.join(aliasRight,
          aliasLeft.equalTo(aliasLeft.col("id"), aliasRight.col("id")), ZFrame.INNER_JOIN);
      assertEquals(3, aliasSelfJoin.count());
      assertEquals(List.of("record_id", "group_name"), Arrays.asList(renamed.withColumnRenamed("absent", "unused").columns()));
      ZFrame<Frame, Row, DuckZColumn> duplicateNames = frame.toDF(new String[] {"same", "same"});
      assertEquals(1, duplicateNames.fieldIndex("same"));
      assertThrows(IllegalArgumentException.class, () -> duplicateNames.select("same"));
      assertEquals(3, frame.sample(false, 1.0).count());
      assertEquals(3, frame.sample(false, 1.0f).count());
      assertEquals(0, frame.sample(false, 0.0f).count());
      assertEquals(List.of("parts"), Arrays.asList(ZFrameDuckAdapter.wrap(
          job.sql("SELECT 'alpha|beta' AS words")).split("words", "\\|", "parts").columns()));
      assertEquals(List.of("item"), Arrays.asList(ZFrameDuckAdapter.wrap(
          job.sql("SELECT [1, 2, 3] AS items")).explode("items", "item").columns()));
      frame.show(false);
      assertEquals(0, frame.sample(true, 0.0).count());
      assertEquals(0, frame.sample(true, 0.0f).count());
      var replacementDouble = frame.sample(true, 0.5);
      var replacementFloat = frame.sample(true, 0.5f);
      assertEquals(List.of("id", "block"), Arrays.asList(replacementDouble.columns()));
      assertEquals(List.of("id", "block"), Arrays.asList(replacementFloat.columns()));
      assertTrue(replacementDouble.count() >= 0);
      assertTrue(replacementFloat.count() >= 0);
      assertTrue(frame.sample(true, 1.0).count() >= 0);
      assertTrue(frame.sample(true, 1.0f).count() >= 0);
      assertThrows(DuckException.class, () -> frame.sample(true, 1.01d).count());
      assertEquals(0, ZFrameDuckAdapter.wrap(job.sql("SELECT 1 AS id, 'empty' AS block WHERE FALSE"))
          .sample(true, 0.25).count());

      ZFrame<Frame, Row, DuckZColumn> nullable = ZFrameDuckAdapter.wrap(
          job.sql("SELECT NULL::VARCHAR AS left_value, 'x' AS right_value"));
      var nullConcat = nullable.select(nullable.concat(nullable.col("left_value"), nullable.col("right_value")));
      assertEquals(1, nullConcat.count());
      assertNull(nullConcat.head().get(nullConcat.columns()[0]));
    }
  }
}
