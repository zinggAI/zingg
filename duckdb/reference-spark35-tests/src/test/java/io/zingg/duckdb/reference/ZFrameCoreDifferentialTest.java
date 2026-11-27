package io.zingg.duckdb.reference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.zingg.duckdb.api.Frame;
import io.zingg.duckdb.api.DuckException;
import io.zingg.duckdb.api.Row;
import io.zingg.duckdb.api.RuntimeConfig;
import io.zingg.duckdb.compat.DuckZColumn;
import io.zingg.duckdb.compat.ZFrameDuckAdapter;
import io.zingg.duckdb.engine.DuckRuntime;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.apache.spark.sql.Dataset;
import org.apache.spark.SparkException;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.functions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import scala.collection.JavaConverters;
import zingg.common.client.ZFrame;

/** Executable Spark 3.5.5 oracle for selected ZFrame relational overloads. */
class ZFrameCoreDifferentialTest {
  @Test
  void limitMatchesSpark(@TempDir Path temp) throws Exception {
    SparkSession spark = newSparkSession("zframe-limit");
    try (var runtime = new DuckRuntime(new RuntimeConfig(
        "jdbc:duckdb:" + temp.resolve("limit.duckdb"), 1, 0, null, 1));
         var job = runtime.openJob()) {
      Dataset<org.apache.spark.sql.Row> sparkRows = spark.sql(
          "SELECT * FROM (VALUES (3, 'duplicate'), (1, 'first'), (2, 'duplicate')) "
              + "AS records(id, value) ORDER BY id");
      ZFrame<Frame, Row, DuckZColumn> duckRows = ZFrameDuckAdapter.wrap(job.sql(
          "SELECT * FROM (VALUES (3, 'duplicate'), (1, 'first'), (2, 'duplicate')) "
              + "AS records(id, value) ORDER BY id"));
      for (int limit : List.of(0, 2, 10)) {
        assertEquals(indexSparkRows(sparkRows.limit(limit).collectAsList()),
            indexDuckRows(duckRows.limit(limit).collectAsList()), "limit(" + limit + ") Spark parity");
      }
    } finally {
      spark.stop();
    }
  }

  @Test
  void unionByNameAllowMissingMatchesSpark(@TempDir Path temp) throws Exception {
    SparkSession spark = newSparkSession("zframe-union-by-name-allow-missing");
    try (var runtime = new DuckRuntime(new RuntimeConfig(
        "jdbc:duckdb:" + temp.resolve("union-by-name-allow-missing.duckdb"), 1, 0, null, 1));
         var job = runtime.openJob()) {
      Dataset<org.apache.spark.sql.Row> sparkLeft = spark.sql(
          "SELECT * FROM (VALUES (1, 'left'), (2, CAST(NULL AS STRING)), (2, CAST(NULL AS STRING))) "
              + "AS records(id, value)");
      Dataset<org.apache.spark.sql.Row> sparkRight = spark.sql(
          "SELECT * FROM (VALUES ('right', 3), ('duplicate', 3)) AS records(value, id)");
      ZFrame<Frame, Row, DuckZColumn> duckLeft = ZFrameDuckAdapter.wrap(job.sql(
          "SELECT * FROM (VALUES (1, 'left'), (2, CAST(NULL AS VARCHAR)), (2, CAST(NULL AS VARCHAR))) "
              + "AS records(id, value)"));
      ZFrame<Frame, Row, DuckZColumn> duckRight = ZFrameDuckAdapter.wrap(job.sql(
          "SELECT * FROM (VALUES ('right', 3), ('duplicate', 3)) AS records(value, id)"));

      Dataset<org.apache.spark.sql.Row> sparkResult = sparkLeft.unionByName(sparkRight, true);
      assertEquals(List.of(sparkResult.columns()),
          List.of(duckLeft.unionByName(duckRight, true).columns()),
          "unionByName(..., true) output column order and missing-column expansion");
      assertEquals(indexSparkRows(sparkResult.collectAsList()),
          indexDuckRows(duckLeft.unionByName(duckRight, true).collectAsList()),
          "unionByName(..., true) complete rows, NULL padding, and duplicate multiplicities");
    } finally {
      spark.stop();
    }
  }

  @Test
  void dropDuplicatesArrayMatchesSpark(@TempDir Path temp) throws Exception {
    SparkSession spark = newSparkSession("zframe-drop-duplicates-array");
    try (var runtime = new DuckRuntime(new RuntimeConfig(
        "jdbc:duckdb:" + temp.resolve("drop-duplicates-array.duckdb"), 1, 0, null, 1));
         var job = runtime.openJob()) {
      Dataset<org.apache.spark.sql.Row> sparkRows = spark.sql(
          "SELECT * FROM (VALUES (CAST(NULL AS STRING), 'same'), (CAST(NULL AS STRING), 'same'), "
              + "('x', 'first'), ('x', 'second'), ('y', CAST(NULL AS STRING))) "
              + "AS records(group_key, value_key)");
      ZFrame<Frame, Row, DuckZColumn> duckRows = ZFrameDuckAdapter.wrap(job.sql(
          "SELECT * FROM (VALUES (CAST(NULL AS VARCHAR), 'same'), (CAST(NULL AS VARCHAR), 'same'), "
              + "('x', 'first'), ('x', 'second'), ('y', CAST(NULL AS VARCHAR))) "
              + "AS records(group_key, value_key)"));

      Dataset<org.apache.spark.sql.Row> sparkByKey = sparkRows.dropDuplicates(new String[] {"group_key"});
      assertEquals(indexSingleColumn(sparkByKey.collectAsList(), "group_key"),
          indexSingleColumnDuck(duckRows.dropDuplicates(new String[] {"group_key"}).collectAsList(), "group_key"),
          "dropDuplicates(String[]) deduplicates NULL and non-NULL subset keys");
      Dataset<org.apache.spark.sql.Row> sparkEmptySubset = sparkRows.dropDuplicates(new String[0]);
      assertEquals(sparkEmptySubset.count(), duckRows.dropDuplicates(new String[0]).count(),
          "dropDuplicates(empty String[]) emits the same at-most-one row cardinality as Spark");

      Dataset<org.apache.spark.sql.Row> sparkEmpty = spark.sql(
          "SELECT CAST(NULL AS STRING) AS group_key, CAST(NULL AS STRING) AS value_key WHERE FALSE");
      ZFrame<Frame, Row, DuckZColumn> duckEmpty = ZFrameDuckAdapter.wrap(job.sql(
          "SELECT CAST(NULL AS VARCHAR) AS group_key, CAST(NULL AS VARCHAR) AS value_key WHERE FALSE"));
      assertEquals(sparkEmpty.dropDuplicates(new String[0]).count(),
          duckEmpty.dropDuplicates(new String[0]).count(),
          "dropDuplicates(empty String[]) keeps empty input empty");
    } finally {
      spark.stop();
    }
  }

  @Test
  void dropDuplicatesVarargsMatchesSpark(@TempDir Path temp) throws Exception {
    SparkSession spark = newSparkSession("zframe-drop-duplicates-varargs");
    try (var runtime = new DuckRuntime(new RuntimeConfig(
        "jdbc:duckdb:" + temp.resolve("drop-duplicates-varargs.duckdb"), 1, 0, null, 1));
         var job = runtime.openJob()) {
      Dataset<org.apache.spark.sql.Row> sparkRows = spark.sql(
          "SELECT * FROM (VALUES ('x', 'same'), ('x', 'same'), ('x', 'other'), "
              + "('y', 'same'), (CAST(NULL AS STRING), 'same'), (CAST(NULL AS STRING), 'same')) "
              + "AS records(group_key, value_key)");
      ZFrame<Frame, Row, DuckZColumn> duckRows = ZFrameDuckAdapter.wrap(job.sql(
          "SELECT * FROM (VALUES ('x', 'same'), ('x', 'same'), ('x', 'other'), "
              + "('y', 'same'), (CAST(NULL AS VARCHAR), 'same'), (CAST(NULL AS VARCHAR), 'same')) "
              + "AS records(group_key, value_key)"));

      Dataset<org.apache.spark.sql.Row> sparkOneKey =
          sparkRows.dropDuplicates("group_key", new String[0]);
      assertEquals(indexSingleColumn(sparkOneKey.collectAsList(), "group_key"),
          indexSingleColumnDuck(duckRows.dropDuplicates("group_key", new String[0]).collectAsList(), "group_key"),
          "dropDuplicates(firstKey, empty varargs) deduplicates only the first key");

      Dataset<org.apache.spark.sql.Row> sparkTwoKeys =
          sparkRows.dropDuplicates("group_key", "value_key");
      assertEquals(indexSparkRows(sparkTwoKeys.collectAsList()),
          indexDuckRows(duckRows.dropDuplicates("group_key", "value_key").collectAsList()),
          "dropDuplicates(firstKey, additionalKeys...) combines all keys and preserves NULL grouping");
    } finally {
      spark.stop();
    }
  }

  @Test
  void withColumnsMatchesSpark(@TempDir Path temp) throws Exception {
    SparkSession spark = newSparkSession("zframe-with-columns");
    try (var runtime = new DuckRuntime(new RuntimeConfig(
        "jdbc:duckdb:" + temp.resolve("with-columns.duckdb"), 1, 0, null, 1));
         var job = runtime.openJob()) {
      Dataset<org.apache.spark.sql.Row> sparkRows = spark.sql(
          "SELECT * FROM (VALUES (1, 'left-a', 'right-a'), (2, 'left-b', 'right-b'), "
              + "(3, CAST(NULL AS STRING), 'right-c'), (3, CAST(NULL AS STRING), 'right-c')) "
              + "AS records(id, left_value, right_value)");
      ZFrame<Frame, Row, DuckZColumn> duckRows = ZFrameDuckAdapter.wrap(job.sql(
          "SELECT * FROM (VALUES (1, 'left-a', 'right-a'), (2, 'left-b', 'right-b'), "
              + "(3, CAST(NULL AS VARCHAR), 'right-c'), (3, CAST(NULL AS VARCHAR), 'right-c')) "
              + "AS records(id, left_value, right_value)"));

      List<String> names = List.of("left_value", "right_value", "derived_value");
      List<org.apache.spark.sql.Column> sparkValues = List.of(
          functions.col("right_value"), functions.col("left_value"),
          functions.concat(functions.col("left_value"), functions.col("right_value")));
      List<DuckZColumn> duckValues = List.of(
          duckRows.col("right_value"), duckRows.col("left_value"),
          duckRows.concat(duckRows.col("left_value"), duckRows.col("right_value")));
      Dataset<org.apache.spark.sql.Row> sparkResult = sparkRows.withColumns(
          JavaConverters.asScalaIteratorConverter(names.iterator()).asScala().toSeq(),
          JavaConverters.asScalaIteratorConverter(sparkValues.iterator()).asScala().toSeq());
      assertEquals(List.of(sparkResult.columns()), List.of(duckRows.withColumns(
          names.toArray(String[]::new), duckValues.toArray(DuckZColumn[]::new)).columns()),
          "withColumns simultaneous replacements and append order");
      assertEquals(indexSparkRows(sparkResult.collectAsList()),
          indexDuckRows(duckRows.withColumns(names.toArray(String[]::new),
              duckValues.toArray(DuckZColumn[]::new)).collectAsList()),
          "withColumns reads all expressions from the original row and preserves duplicate multiplicity");

      Dataset<org.apache.spark.sql.Row> sparkIdentity = sparkRows.withColumns(
          JavaConverters.asScalaIteratorConverter(List.<String>of().iterator()).asScala().toSeq(),
          JavaConverters.asScalaIteratorConverter(List.<org.apache.spark.sql.Column>of().iterator()).asScala().toSeq());
      assertEquals(indexSparkRows(sparkIdentity.collectAsList()),
          indexDuckRows(duckRows.withColumns(new String[0], new DuckZColumn[0]).collectAsList()),
          "withColumns with empty parallel arrays is an identity projection");
    } finally {
      spark.stop();
    }
  }

  @Test
  void withColumnLiteralMatchesSpark(@TempDir Path temp) throws Exception {
    SparkSession spark = newSparkSession("zframe-with-column-literal");
    try (var runtime = new DuckRuntime(new RuntimeConfig(
        "jdbc:duckdb:" + temp.resolve("with-column-literal.duckdb"), 1, 0, null, 1));
         var job = runtime.openJob()) {
      Dataset<org.apache.spark.sql.Row> sparkRows = spark.sql(
          "SELECT * FROM (VALUES (1, 'alpha'), (2, CAST(NULL AS STRING)), (2, CAST(NULL AS STRING))) "
              + "AS records(id, label)");
      ZFrame<Frame, Row, DuckZColumn> duckRows = ZFrameDuckAdapter.wrap(job.sql(
          "SELECT * FROM (VALUES (1, 'alpha'), (2, CAST(NULL AS VARCHAR)), (2, CAST(NULL AS VARCHAR))) "
              + "AS records(id, label)"));

      Dataset<org.apache.spark.sql.Row> sparkAdded = sparkRows.withColumn("constant", functions.lit(7));
      assertEquals(List.of(sparkAdded.columns()),
          List.of(duckRows.withColumn("constant", 7).columns()),
          "withColumn literal appends new output column");
      assertEquals(indexSparkRows(sparkAdded.collectAsList()),
          indexDuckRows(duckRows.withColumn("constant", 7).collectAsList()),
          "withColumn literal value and duplicate multiplicity");

      Dataset<org.apache.spark.sql.Row> sparkReplace = sparkRows.withColumn("label", functions.lit("replacement"));
      assertEquals(List.of(sparkReplace.columns()),
          List.of(duckRows.withColumn("label", "replacement").columns()),
          "withColumn literal replacement retains the original column position");
      assertEquals(indexSparkRows(sparkReplace.collectAsList()),
          indexDuckRows(duckRows.withColumn("label", "replacement").collectAsList()),
          "withColumn replacement overwrites all original values including NULL");

      Dataset<org.apache.spark.sql.Row> sparkNull = sparkRows.withColumn("nullable_added", functions.lit(null));
      assertEquals(indexSparkRows(sparkNull.collectAsList()),
          indexDuckRows(duckRows.withColumn("nullable_added", null).collectAsList()),
          "withColumn NULL literal preserves row multiplicity and NULL values");
    } finally {
      spark.stop();
    }
  }

  @Test
  void equalToStringMatchesSpark(@TempDir Path temp) throws Exception {
    SparkSession spark = newSparkSession("zframe-equal-to-string");
    try (var runtime = new DuckRuntime(new RuntimeConfig(
        "jdbc:duckdb:" + temp.resolve("equal-to-string.duckdb"), 1, 0, null, 1));
         var job = runtime.openJob()) {
      Dataset<org.apache.spark.sql.Row> sparkRows = spark.sql(
          "SELECT * FROM (VALUES ('alpha'), ('alpha'), (''), ('O\\'Reilly'), (CAST(NULL AS STRING))) "
              + "AS records(text_value)");
      ZFrame<Frame, Row, DuckZColumn> duckRows = ZFrameDuckAdapter.wrap(job.sql(
          "SELECT * FROM (VALUES ('alpha'), ('alpha'), (''), ('O''Reilly'), (CAST(NULL AS VARCHAR))) "
              + "AS records(text_value)"));

      Dataset<org.apache.spark.sql.Row> sparkPredicate = sparkRows.withColumn(
          "matches", functions.col("text_value").equalTo("O'Reilly"));
      assertEquals(List.of(sparkPredicate.columns()), List.of(duckRows.withColumn(
          "matches", duckRows.equalTo("text_value", "O'Reilly")).columns()),
          "equalTo(String, String) projected result schema");
      assertEquals(indexSparkRows(sparkPredicate.collectAsList()),
          indexDuckRows(duckRows.withColumn("matches", duckRows.equalTo("text_value", "O'Reilly"))
              .collectAsList()),
          "equalTo(String, String) escaping, NULL propagation, and full multiplicity");

      Dataset<org.apache.spark.sql.Row> sparkMatches = sparkRows.filter(
          functions.col("text_value").equalTo("alpha"));
      assertEquals(indexSparkRows(sparkMatches.collectAsList()),
          indexDuckRows(duckRows.filter(duckRows.equalTo("text_value", "alpha")).collectAsList()),
          "equalTo(String, String) filtering matches exact string values only");
    } finally {
      spark.stop();
    }
  }

  @Test
  void equalToIntMatchesSpark(@TempDir Path temp) throws Exception {
    SparkSession spark = newSparkSession("zframe-equal-to-int");
    try (var runtime = new DuckRuntime(new RuntimeConfig(
        "jdbc:duckdb:" + temp.resolve("equal-to-int.duckdb"), 1, 0, null, 1));
         var job = runtime.openJob()) {
      Dataset<org.apache.spark.sql.Row> sparkRows = spark.sql(
          "SELECT * FROM (VALUES (CAST(-2147483648 AS INT)), (10), (10), "
              + "(CAST(2147483647 AS INT)), (CAST(NULL AS INT))) AS records(number_value)");
      ZFrame<Frame, Row, DuckZColumn> duckRows = ZFrameDuckAdapter.wrap(job.sql(
          "SELECT * FROM (VALUES (CAST(-2147483648 AS INTEGER)), (10), (10), "
              + "(CAST(2147483647 AS INTEGER)), (CAST(NULL AS INTEGER))) AS records(number_value)"));

      Dataset<org.apache.spark.sql.Row> sparkProjected = sparkRows.withColumn(
          "matches_min", functions.col("number_value").equalTo(Integer.MIN_VALUE));
      assertEquals(indexSparkRows(sparkProjected.collectAsList()),
          indexDuckRows(duckRows.withColumn("matches_min",
              duckRows.equalTo("number_value", Integer.MIN_VALUE)).collectAsList()),
          "equalTo(String, int) projection checks minimum-int equality and NULL propagation");

      for (int value : new int[] {Integer.MIN_VALUE, 10, Integer.MAX_VALUE}) {
        Dataset<org.apache.spark.sql.Row> sparkMatches = sparkRows.filter(
            functions.col("number_value").equalTo(value));
        assertEquals(indexSparkRows(sparkMatches.collectAsList()),
            indexDuckRows(duckRows.filter(duckRows.equalTo("number_value", value)).collectAsList()),
            "equalTo(String, int) parity at boundary/value " + value);
      }
    } finally {
      spark.stop();
    }
  }

  @Test
  void equalToDoubleMatchesSpark(@TempDir Path temp) throws Exception {
    SparkSession spark = newSparkSession("zframe-equal-to-double");
    try (var runtime = new DuckRuntime(new RuntimeConfig(
        "jdbc:duckdb:" + temp.resolve("equal-to-double.duckdb"), 1, 0, null, 1));
         var job = runtime.openJob()) {
      Dataset<org.apache.spark.sql.Row> sparkRows = spark.sql(
          "SELECT * FROM (VALUES (CAST(-3.25 AS DOUBLE)), (0.0D), (1.5D), (1.5D), "
              + "(CAST(1.7976931348623157E308 AS DOUBLE)), (CAST(NULL AS DOUBLE))) "
              + "AS records(number_value)");
      ZFrame<Frame, Row, DuckZColumn> duckRows = ZFrameDuckAdapter.wrap(job.sql(
          "SELECT * FROM (VALUES (CAST(-3.25 AS DOUBLE)), (0.0), (1.5), (1.5), "
              + "(CAST(1.7976931348623157E308 AS DOUBLE)), (CAST(NULL AS DOUBLE))) "
              + "AS records(number_value)"));

      Dataset<org.apache.spark.sql.Row> sparkProjected = sparkRows.withColumn(
          "matches_fraction", functions.col("number_value").equalTo(1.5d));
      assertEquals(indexSparkRows(sparkProjected.collectAsList()),
          indexDuckRows(duckRows.withColumn("matches_fraction",
              duckRows.equalTo("number_value", 1.5d)).collectAsList()),
          "equalTo(String, double) projection checks fractional equality and NULL propagation");

      for (double value : new double[] {-3.25d, 0.0d, 1.5d, Double.MAX_VALUE}) {
        Dataset<org.apache.spark.sql.Row> sparkMatches = sparkRows.filter(
            functions.col("number_value").equalTo(value));
        assertEquals(indexSparkRows(sparkMatches.collectAsList()),
            indexDuckRows(duckRows.filter(duckRows.equalTo("number_value", value)).collectAsList()),
            "equalTo(String, double) parity at " + value);
      }
    } finally {
      spark.stop();
    }
  }

  @Test
  void gtDoubleMatchesSpark(@TempDir Path temp) throws Exception {
    SparkSession spark = newSparkSession("zframe-gt-double");
    try (var runtime = new DuckRuntime(new RuntimeConfig(
        "jdbc:duckdb:" + temp.resolve("gt-double.duckdb"), 1, 0, null, 1));
         var job = runtime.openJob()) {
      Dataset<org.apache.spark.sql.Row> sparkRows = spark.sql(
          "SELECT * FROM (VALUES (1, -3.25D), (2, -0.0D), (3, 0.0D), (4, 1.5D), (5, 1.5D), "
              + "(6, CAST(1.7976931348623157E308 AS DOUBLE)), (7, CAST(NULL AS DOUBLE))) "
              + "AS records(row_id, number_value)");
      ZFrame<Frame, Row, DuckZColumn> duckRows = ZFrameDuckAdapter.wrap(job.sql(
          "SELECT * FROM (VALUES (1, -3.25), (2, -0.0), (3, 0.0), (4, 1.5), (5, 1.5), "
              + "(6, CAST(1.7976931348623157E308 AS DOUBLE)), (7, CAST(NULL AS DOUBLE))) "
              + "AS records(row_id, number_value)"));

      Dataset<org.apache.spark.sql.Row> sparkProjected = sparkRows.withColumn(
          "above_threshold", functions.col("number_value").gt(0.0d));
      assertEquals(indexSparkRows(sparkProjected.select("row_id", "above_threshold").collectAsList()),
          indexDuckRows(duckRows.withColumn("above_threshold",
              duckRows.gt("number_value", 0.0d)).select("row_id", "above_threshold").collectAsList()),
          "gt(String, double) projection matches Spark including NULL/UNKNOWN and duplicates");

      Dataset<org.apache.spark.sql.Row> sparkMatches = sparkRows.filter(
          functions.col("number_value").gt(0.0d));
      assertEquals(indexSparkRows(sparkMatches.select("row_id").collectAsList()),
          indexDuckRows(duckRows.filter(duckRows.gt("number_value", 0.0d)).select("row_id").collectAsList()),
          "gt(String, double) filter preserves Spark boundary and multiplicity semantics");
    } finally {
      spark.stop();
    }
  }

  @Test
  void gtColumnToIdentityMatchesSpark(@TempDir Path temp) throws Exception {
    SparkSession spark = newSparkSession("zframe-gt-identity-column");
    try (var runtime = new DuckRuntime(new RuntimeConfig(
        "jdbc:duckdb:" + temp.resolve("gt-identity-column.duckdb"), 1, 0, null, 1));
         var job = runtime.openJob()) {
      Dataset<org.apache.spark.sql.Row> sparkRows = spark.sql(
          "SELECT * FROM (VALUES (1, 1.0D, 1.0D), (2, 2.0D, 1.0D), (3, 0.0D, 2.0D), "
              + "(4, CAST(NULL AS DOUBLE), 1.0D), (5, 1.0D, CAST(NULL AS DOUBLE)), "
              + "(6, 2.0D, 1.0D), (7, 2.0D, 1.0D)) AS records(row_id, metric, z_metric)");
      ZFrame<Frame, Row, DuckZColumn> duckRows = ZFrameDuckAdapter.wrap(job.sql(
          "SELECT * FROM (VALUES (1, 1.0, 1.0), (2, 2.0, 1.0), (3, 0.0, 2.0), "
              + "(4, CAST(NULL AS DOUBLE), 1.0), (5, 1.0, CAST(NULL AS DOUBLE)), "
              + "(6, 2.0, 1.0), (7, 2.0, 1.0)) AS records(row_id, metric, z_metric)"));

      Dataset<org.apache.spark.sql.Row> sparkProjected = sparkRows.withColumn(
          "greater", functions.col("metric").gt(functions.col("z_metric")));
      assertEquals(indexSparkRows(sparkProjected.select("row_id", "greater").collectAsList()),
          indexDuckRows(duckRows.withColumn("greater", duckRows.gt("metric"))
              .select("row_id", "greater").collectAsList()),
          "gt(String) compares against the z_<column> identity and preserves NULL predicate values");

      Dataset<org.apache.spark.sql.Row> sparkMatches = sparkRows.filter(
          functions.col("metric").gt(functions.col("z_metric")));
      assertEquals(indexSparkRows(sparkMatches.select("row_id").collectAsList()),
          indexDuckRows(duckRows.filter(duckRows.gt("metric")).select("row_id").collectAsList()),
          "gt(String) filters identity-column matches with duplicate multiplicity");
    } finally {
      spark.stop();
    }
  }

  @Test
  void andColumnsMatchesSpark(@TempDir Path temp) throws Exception {
    SparkSession spark = newSparkSession("zframe-and-columns");
    try (var runtime = new DuckRuntime(new RuntimeConfig(
        "jdbc:duckdb:" + temp.resolve("and-columns.duckdb"), 1, 0, null, 1));
         var job = runtime.openJob()) {
      Dataset<org.apache.spark.sql.Row> sparkRows = spark.sql(
          "SELECT * FROM (VALUES (1, TRUE, TRUE), (2, TRUE, FALSE), (3, TRUE, CAST(NULL AS BOOLEAN)), "
              + "(4, FALSE, TRUE), (5, FALSE, FALSE), (6, FALSE, CAST(NULL AS BOOLEAN)), "
              + "(7, CAST(NULL AS BOOLEAN), TRUE), (8, CAST(NULL AS BOOLEAN), FALSE), "
              + "(9, CAST(NULL AS BOOLEAN), CAST(NULL AS BOOLEAN)), (10, TRUE, TRUE)) "
              + "AS records(row_id, left_flag, right_flag)");
      ZFrame<Frame, Row, DuckZColumn> duckRows = ZFrameDuckAdapter.wrap(job.sql(
          "SELECT * FROM (VALUES (1, TRUE, TRUE), (2, TRUE, FALSE), (3, TRUE, CAST(NULL AS BOOLEAN)), "
              + "(4, FALSE, TRUE), (5, FALSE, FALSE), (6, FALSE, CAST(NULL AS BOOLEAN)), "
              + "(7, CAST(NULL AS BOOLEAN), TRUE), (8, CAST(NULL AS BOOLEAN), FALSE), "
              + "(9, CAST(NULL AS BOOLEAN), CAST(NULL AS BOOLEAN)), (10, TRUE, TRUE)) "
              + "AS records(row_id, left_flag, right_flag)"));

      Dataset<org.apache.spark.sql.Row> sparkProjected = sparkRows.withColumn(
          "conjunction", functions.col("left_flag").and(functions.col("right_flag")));
      assertEquals(indexSparkRows(sparkProjected.select("row_id", "conjunction").collectAsList()),
          indexDuckRows(duckRows.withColumn("conjunction", duckRows.and(
              duckRows.col("left_flag"), duckRows.col("right_flag")))
              .select("row_id", "conjunction").collectAsList()),
          "and(C, C) matches Spark's full three-valued truth table and duplicate behavior");

      Dataset<org.apache.spark.sql.Row> sparkMatches = sparkRows.filter(
          functions.col("left_flag").and(functions.col("right_flag")));
      assertEquals(indexSparkRows(sparkMatches.select("row_id").collectAsList()),
          indexDuckRows(duckRows.filter(duckRows.and(
              duckRows.col("left_flag"), duckRows.col("right_flag"))).select("row_id").collectAsList()),
          "and(C, C) filtering keeps only TRUE, excluding FALSE and UNKNOWN");
    } finally {
      spark.stop();
    }
  }

  @Test
  void notColumnMatchesSpark(@TempDir Path temp) throws Exception {
    SparkSession spark = newSparkSession("zframe-not-column");
    try (var runtime = new DuckRuntime(new RuntimeConfig(
        "jdbc:duckdb:" + temp.resolve("not-column.duckdb"), 1, 0, null, 1));
         var job = runtime.openJob()) {
      Dataset<org.apache.spark.sql.Row> sparkRows = spark.sql(
          "SELECT * FROM (VALUES (1, TRUE), (2, FALSE), (3, CAST(NULL AS BOOLEAN)), "
              + "(4, FALSE), (5, TRUE)) AS records(row_id, flag_value)");
      ZFrame<Frame, Row, DuckZColumn> duckRows = ZFrameDuckAdapter.wrap(job.sql(
          "SELECT * FROM (VALUES (1, TRUE), (2, FALSE), (3, CAST(NULL AS BOOLEAN)), "
              + "(4, FALSE), (5, TRUE)) AS records(row_id, flag_value)"));

      Dataset<org.apache.spark.sql.Row> sparkProjected = sparkRows.withColumn(
          "negated", functions.not(functions.col("flag_value")));
      assertEquals(indexSparkRows(sparkProjected.select("row_id", "negated").collectAsList()),
          indexDuckRows(duckRows.withColumn("negated", duckRows.not(
              duckRows.col("flag_value"))).select("row_id", "negated").collectAsList()),
          "not(C) matches Spark for TRUE, FALSE, and NULL with duplicate values");

      Dataset<org.apache.spark.sql.Row> sparkMatches = sparkRows.filter(
          functions.not(functions.col("flag_value")));
      assertEquals(indexSparkRows(sparkMatches.select("row_id").collectAsList()),
          indexDuckRows(duckRows.filter(duckRows.not(
              duckRows.col("flag_value"))).select("row_id").collectAsList()),
          "not(C) filter retains FALSE inputs, excluding TRUE and UNKNOWN");
    } finally {
      spark.stop();
    }
  }

  @Test
  void orColumnsMatchesSpark(@TempDir Path temp) throws Exception {
    SparkSession spark = newSparkSession("zframe-or-columns");
    try (var runtime = new DuckRuntime(new RuntimeConfig(
        "jdbc:duckdb:" + temp.resolve("or-columns.duckdb"), 1, 0, null, 1));
         var job = runtime.openJob()) {
      Dataset<org.apache.spark.sql.Row> sparkRows = spark.sql(
          "SELECT * FROM (VALUES (1, TRUE, TRUE), (2, TRUE, FALSE), (3, TRUE, CAST(NULL AS BOOLEAN)), "
              + "(4, FALSE, TRUE), (5, FALSE, FALSE), (6, FALSE, CAST(NULL AS BOOLEAN)), "
              + "(7, CAST(NULL AS BOOLEAN), TRUE), (8, CAST(NULL AS BOOLEAN), FALSE), "
              + "(9, CAST(NULL AS BOOLEAN), CAST(NULL AS BOOLEAN)), (10, FALSE, FALSE)) "
              + "AS records(row_id, left_flag, right_flag)");
      ZFrame<Frame, Row, DuckZColumn> duckRows = ZFrameDuckAdapter.wrap(job.sql(
          "SELECT * FROM (VALUES (1, TRUE, TRUE), (2, TRUE, FALSE), (3, TRUE, CAST(NULL AS BOOLEAN)), "
              + "(4, FALSE, TRUE), (5, FALSE, FALSE), (6, FALSE, CAST(NULL AS BOOLEAN)), "
              + "(7, CAST(NULL AS BOOLEAN), TRUE), (8, CAST(NULL AS BOOLEAN), FALSE), "
              + "(9, CAST(NULL AS BOOLEAN), CAST(NULL AS BOOLEAN)), (10, FALSE, FALSE)) "
              + "AS records(row_id, left_flag, right_flag)"));

      Dataset<org.apache.spark.sql.Row> sparkProjected = sparkRows.withColumn(
          "disjunction", functions.col("left_flag").or(functions.col("right_flag")));
      assertEquals(indexSparkRows(sparkProjected.select("row_id", "disjunction").collectAsList()),
          indexDuckRows(duckRows.withColumn("disjunction", duckRows.or(
              duckRows.col("left_flag"), duckRows.col("right_flag")))
              .select("row_id", "disjunction").collectAsList()),
          "or(C, C) matches Spark's full three-valued truth table and duplicate behavior");

      Dataset<org.apache.spark.sql.Row> sparkMatches = sparkRows.filter(
          functions.col("left_flag").or(functions.col("right_flag")));
      assertEquals(indexSparkRows(sparkMatches.select("row_id").collectAsList()),
          indexDuckRows(duckRows.filter(duckRows.or(
              duckRows.col("left_flag"), duckRows.col("right_flag"))).select("row_id").collectAsList()),
          "or(C, C) filtering keeps only TRUE, excluding FALSE and UNKNOWN");
    } finally {
      spark.stop();
    }
  }

  @Test
  void equalToColumnsMatchesSpark(@TempDir Path temp) throws Exception {
    SparkSession spark = newSparkSession("zframe-equal-to-columns");
    try (var runtime = new DuckRuntime(new RuntimeConfig(
        "jdbc:duckdb:" + temp.resolve("equal-to-columns.duckdb"), 1, 0, null, 1));
         var job = runtime.openJob()) {
      Dataset<org.apache.spark.sql.Row> sparkRows = spark.sql(
          "SELECT * FROM (VALUES ('same', 'same'), ('same', 'different'), "
              + "(CAST(NULL AS STRING), CAST(NULL AS STRING)), (CAST(NULL AS STRING), 'present'), "
              + "('repeat', 'repeat'), ('repeat', 'repeat')) AS records(left_value, right_value)");
      ZFrame<Frame, Row, DuckZColumn> duckRows = ZFrameDuckAdapter.wrap(job.sql(
          "SELECT * FROM (VALUES ('same', 'same'), ('same', 'different'), "
              + "(CAST(NULL AS VARCHAR), CAST(NULL AS VARCHAR)), (CAST(NULL AS VARCHAR), 'present'), "
              + "('repeat', 'repeat'), ('repeat', 'repeat')) AS records(left_value, right_value)"));

      Dataset<org.apache.spark.sql.Row> sparkProjected = sparkRows.withColumn(
          "matches", functions.col("left_value").equalTo(functions.col("right_value")));
      assertEquals(indexSparkRows(sparkProjected.collectAsList()),
          indexDuckRows(duckRows.withColumn("matches", duckRows.equalTo(
              duckRows.col("left_value"), duckRows.col("right_value"))).collectAsList()),
          "equalTo(C, C) compares expression values and preserves SQL NULL semantics");

      Dataset<org.apache.spark.sql.Row> sparkMatches = sparkRows.filter(
          functions.col("left_value").equalTo(functions.col("right_value")));
      assertEquals(indexSparkRows(sparkMatches.collectAsList()),
          indexDuckRows(duckRows.filter(duckRows.equalTo(
              duckRows.col("left_value"), duckRows.col("right_value"))).collectAsList()),
          "equalTo(C, C) filters matches while excluding false/unknown rows");
    } finally {
      spark.stop();
    }
  }

  @Test
  void gtColumnsMatchesSpark(@TempDir Path temp) throws Exception {
    SparkSession spark = newSparkSession("zframe-gt-columns");
    try (var runtime = new DuckRuntime(new RuntimeConfig(
        "jdbc:duckdb:" + temp.resolve("gt-columns.duckdb"), 1, 0, null, 1));
         var job = runtime.openJob()) {
      Dataset<org.apache.spark.sql.Row> sparkRows = spark.sql(
          "SELECT * FROM (VALUES (1, 9, 2), (2, 2, 9), (3, 5, 5), "
              + "(4, CAST(NULL AS INT), 1), (5, 1, CAST(NULL AS INT)), "
              + "(6, CAST(NULL AS INT), CAST(NULL AS INT)), (7, 9, 2), (8, -3, -8)) "
              + "AS records(row_id, left_value, right_value)");
      ZFrame<Frame, Row, DuckZColumn> duckRows = ZFrameDuckAdapter.wrap(job.sql(
          "SELECT * FROM (VALUES (1, 9, 2), (2, 2, 9), (3, 5, 5), "
              + "(4, CAST(NULL AS INTEGER), 1), (5, 1, CAST(NULL AS INTEGER)), "
              + "(6, CAST(NULL AS INTEGER), CAST(NULL AS INTEGER)), (7, 9, 2), (8, -3, -8)) "
              + "AS records(row_id, left_value, right_value)"));

      Dataset<org.apache.spark.sql.Row> sparkProjected = sparkRows.withColumn(
          "greater", functions.col("left_value").gt(functions.col("right_value")));
      assertEquals(indexSparkRows(sparkProjected.select("row_id", "greater").collectAsList()),
          indexDuckRows(duckRows.withColumn("greater", duckRows.gt(
              duckRows.col("left_value"), duckRows.col("right_value")))
              .select("row_id", "greater").collectAsList()),
          "gt(C, C) projection compares numeric columns and preserves NULL and duplicate results");

      Dataset<org.apache.spark.sql.Row> sparkMatches = sparkRows.filter(
          functions.col("left_value").gt(functions.col("right_value")));
      assertEquals(indexSparkRows(sparkMatches.select("row_id").collectAsList()),
          indexDuckRows(duckRows.filter(duckRows.gt(
              duckRows.col("left_value"), duckRows.col("right_value"))).select("row_id").collectAsList()),
          "gt(C, C) filtering retains only greater-than TRUE rows");
    } finally {
      spark.stop();
    }
  }

  @Test
  void getColsMatchesSpark(@TempDir Path temp) throws Exception {
    SparkSession spark = newSparkSession("zframe-get-cols");
    try (var runtime = new DuckRuntime(new RuntimeConfig(
        "jdbc:duckdb:" + temp.resolve("get-cols.duckdb"), 1, 0, null, 1));
         var job = runtime.openJob()) {
      Dataset<org.apache.spark.sql.Row> sparkRows = spark.sql(
          "SELECT * FROM (VALUES (1, 'alpha', CAST(NULL AS STRING)), "
              + "(2, 'beta', 'present'), (2, 'beta', 'present')) AS records(id, label, nullable_value)");
      ZFrame<Frame, Row, DuckZColumn> duckRows = ZFrameDuckAdapter.wrap(job.sql(
          "SELECT * FROM (VALUES (1, 'alpha', CAST(NULL AS VARCHAR)), "
              + "(2, 'beta', 'present'), (2, 'beta', 'present')) AS records(id, label, nullable_value)"));

      assertEquals(List.of(sparkRows.columns()),
          Arrays.stream(duckRows.getCols()).map(DuckZColumn::sql).map(sql -> sql.replace("\"", "" )).toList(),
          "getCols() exposes columns in source schema order");
      assertEquals(indexSparkRows(sparkRows.select("id", "label", "nullable_value").collectAsList()),
          indexDuckRows(duckRows.select(duckRows.getCols()).collectAsList()),
          "getCols() columns consumed by the exact select(C...) overload preserve schema, values, NULLs, and duplicates");
    } finally {
      spark.stop();
    }
  }

  @Test
  void fieldsMatchesSpark(@TempDir Path temp) throws Exception {
    SparkSession spark = newSparkSession("zframe-fields");
    try (var runtime = new DuckRuntime(new RuntimeConfig(
        "jdbc:duckdb:" + temp.resolve("fields.duckdb"), 1, 0, null, 1));
         var job = runtime.openJob()) {
      Dataset<org.apache.spark.sql.Row> sparkRows = spark.sql(
          "SELECT CAST(id AS INT) AS id, CAST(label AS STRING) AS label, "
              + "CAST(NULL AS DOUBLE) AS nullable_number FROM VALUES (1, 'alpha'), (2, 'beta') "
              + "AS records(id, label)");
      ZFrame<Frame, Row, DuckZColumn> duckRows = ZFrameDuckAdapter.wrap(job.sql(
          "SELECT CAST(id AS INTEGER) AS id, CAST(label AS VARCHAR) AS label, "
              + "CAST(NULL AS DOUBLE) AS nullable_number FROM (VALUES (1, 'alpha'), (2, 'beta')) "
              + "AS records(id, label)"));

      org.apache.spark.sql.types.StructField[] sparkFields = sparkRows.schema().fields();
      zingg.common.client.FieldData[] duckFields = duckRows.fields();
      assertEquals(sparkFields.length, duckRows.fields().length, "fields() schema width");
      for (int index = 0; index < sparkFields.length; index++) {
        assertEquals(sparkFields[index].name(), duckFields[index].getName(), "fields() name at " + index);
        assertEquals(sparkFields[index].dataType().toString(), duckFields[index].getDataType(),
            "fields() Spark type at " + index);
        assertTrue(!sparkFields[index].nullable() || duckFields[index].isNullable(),
            "fields() cannot claim stronger nullability at " + index);
      }

      Dataset<org.apache.spark.sql.Row> sparkEmpty = spark.sql(
          "SELECT CAST(NULL AS INT) AS id, CAST(NULL AS STRING) AS label WHERE FALSE");
      ZFrame<Frame, Row, DuckZColumn> duckEmpty = ZFrameDuckAdapter.wrap(job.sql(
          "SELECT CAST(NULL AS INTEGER) AS id, CAST(NULL AS VARCHAR) AS label WHERE FALSE"));
      assertEquals(Arrays.stream(sparkEmpty.schema().fields()).map(field -> field.name()).toList(),
          Arrays.stream(duckEmpty.fields()).map(zingg.common.client.FieldData::getName).toList(),
          "fields() preserves typed schema metadata on zero-row inputs");
      assertEquals(Arrays.stream(sparkEmpty.schema().fields()).map(field -> field.dataType().toString()).toList(),
          Arrays.stream(duckEmpty.fields()).map(zingg.common.client.FieldData::getDataType).toList(),
          "fields() preserves Spark type names on zero-row inputs");
    } finally {
      spark.stop();
    }
  }

  @Test
  void fieldIndexMatchesSpark(@TempDir Path temp) throws Exception {
    SparkSession spark = newSparkSession("zframe-field-index");
    try (var runtime = new DuckRuntime(new RuntimeConfig(
        "jdbc:duckdb:" + temp.resolve("field-index.duckdb"), 1, 0, null, 1));
         var job = runtime.openJob()) {
      Dataset<org.apache.spark.sql.Row> sparkUnique = spark.sql("SELECT 10 AS first_field, 20 AS second_field");
      ZFrame<Frame, Row, DuckZColumn> duckUnique = ZFrameDuckAdapter.wrap(
          job.sql("SELECT 10 AS first_field, 20 AS second_field"));
      assertEquals(sparkUnique.schema().fieldIndex("first_field"), duckUnique.fieldIndex("first_field"),
          "fieldIndex(String) resolves first field");
      assertEquals(sparkUnique.schema().fieldIndex("second_field"), duckUnique.fieldIndex("second_field"),
          "fieldIndex(String) resolves later field");
      assertThrows(IllegalArgumentException.class, () -> sparkUnique.schema().fieldIndex("missing"));
      assertThrows(IllegalArgumentException.class, () -> duckUnique.fieldIndex("missing"));

      Dataset<org.apache.spark.sql.Row> sparkDuplicate = sparkUnique.toDF("same", "same");
      ZFrame<Frame, Row, DuckZColumn> duckDuplicate = duckUnique.toDF(new String[] {"same", "same"});
      assertEquals(sparkDuplicate.schema().fieldIndex("same"), duckDuplicate.fieldIndex("same"),
          "fieldIndex(String) selects Spark's final matching ordinal for duplicate names");
    } finally {
      spark.stop();
    }
  }

  @Test
  void filterNotNullCondMatchesSpark(@TempDir Path temp) throws Exception {
    SparkSession spark = newSparkSession("zframe-filter-not-null");
    try (var runtime = new DuckRuntime(new RuntimeConfig(
        "jdbc:duckdb:" + temp.resolve("filter-not-null.duckdb"), 1, 0, null, 1));
         var job = runtime.openJob()) {
      Dataset<org.apache.spark.sql.Row> sparkRows = spark.sql(
          "SELECT * FROM (VALUES (1, 'same'), (2, CAST(NULL AS STRING)), "
              + "(3, 'same'), (3, 'same')) AS records(id, nullable_value)");
      ZFrame<Frame, Row, DuckZColumn> duckRows = ZFrameDuckAdapter.wrap(job.sql(
          "SELECT * FROM (VALUES (1, 'same'), (2, CAST(NULL AS VARCHAR)), "
              + "(3, 'same'), (3, 'same')) AS records(id, nullable_value)"));
      assertEquals(indexSparkRows(sparkRows.filter(functions.col("nullable_value").isNotNull()).collectAsList()),
          indexDuckRows(duckRows.filterNotNullCond("nullable_value").collectAsList()),
          "filterNotNullCond(String) keeps all non-NULL rows and duplicate multiplicities");

      Dataset<org.apache.spark.sql.Row> sparkAllNull = spark.sql(
          "SELECT CAST(NULL AS STRING) AS nullable_value WHERE TRUE UNION ALL "
              + "SELECT CAST(NULL AS STRING)");
      ZFrame<Frame, Row, DuckZColumn> duckAllNull = ZFrameDuckAdapter.wrap(job.sql(
          "SELECT CAST(NULL AS VARCHAR) AS nullable_value UNION ALL SELECT CAST(NULL AS VARCHAR)"));
      assertEquals(indexSparkRows(sparkAllNull.filter(functions.col("nullable_value").isNotNull()).collectAsList()),
          indexDuckRows(duckAllNull.filterNotNullCond("nullable_value").collectAsList()),
          "filterNotNullCond(String) on all-NULL input returns no rows");
    } finally {
      spark.stop();
    }
  }

  @Test
  void filterNullCondMatchesSpark(@TempDir Path temp) throws Exception {
    SparkSession spark = newSparkSession("zframe-filter-null");
    try (var runtime = new DuckRuntime(new RuntimeConfig(
        "jdbc:duckdb:" + temp.resolve("filter-null.duckdb"), 1, 0, null, 1));
         var job = runtime.openJob()) {
      Dataset<org.apache.spark.sql.Row> sparkRows = spark.sql(
          "SELECT * FROM (VALUES (1, 'same'), (2, CAST(NULL AS STRING)), "
              + "(3, CAST(NULL AS STRING)), (3, 'same')) AS records(id, nullable_value)");
      ZFrame<Frame, Row, DuckZColumn> duckRows = ZFrameDuckAdapter.wrap(job.sql(
          "SELECT * FROM (VALUES (1, 'same'), (2, CAST(NULL AS VARCHAR)), "
              + "(3, CAST(NULL AS VARCHAR)), (3, 'same')) AS records(id, nullable_value)"));
      assertEquals(indexSparkRows(sparkRows.filter(functions.col("nullable_value").isNull()).collectAsList()),
          indexDuckRows(duckRows.filterNullCond("nullable_value").collectAsList()),
          "filterNullCond(String) keeps every NULL row and its duplicate multiplicity");

      Dataset<org.apache.spark.sql.Row> sparkNoNull = spark.sql(
          "SELECT * FROM (VALUES (1, 'same'), (2, 'other'), (2, 'other')) AS records(id, nullable_value)");
      ZFrame<Frame, Row, DuckZColumn> duckNoNull = ZFrameDuckAdapter.wrap(job.sql(
          "SELECT * FROM (VALUES (1, 'same'), (2, 'other'), (2, 'other')) AS records(id, nullable_value)"));
      assertEquals(indexSparkRows(sparkNoNull.filter(functions.col("nullable_value").isNull()).collectAsList()),
          indexDuckRows(duckNoNull.filterNullCond("nullable_value").collectAsList()),
          "filterNullCond(String) returns no rows when the column has no NULLs");
    } finally {
      spark.stop();
    }
  }

  @Test
  void isNotNullColumnMatchesSpark(@TempDir Path temp) throws Exception {
    SparkSession spark = newSparkSession("zframe-is-not-null-column");
    try (var runtime = new DuckRuntime(new RuntimeConfig(
        "jdbc:duckdb:" + temp.resolve("is-not-null-column.duckdb"), 1, 0, null, 1));
         var job = runtime.openJob()) {
      Dataset<org.apache.spark.sql.Row> sparkRows = spark.sql(
          "SELECT * FROM (VALUES (1, 'same'), (2, CAST(NULL AS STRING)), "
              + "(3, 'same'), (4, 'other')) AS records(id, nullable_value)");
      ZFrame<Frame, Row, DuckZColumn> duckRows = ZFrameDuckAdapter.wrap(job.sql(
          "SELECT * FROM (VALUES (1, 'same'), (2, CAST(NULL AS VARCHAR)), "
              + "(3, 'same'), (4, 'other')) AS records(id, nullable_value)"));
      assertEquals(indexSparkRows(sparkRows.filter(functions.col("nullable_value").isNotNull()).collectAsList()),
          indexDuckRows(duckRows.filter(duckRows.isNotNull(duckRows.col("nullable_value"))).collectAsList()),
          "isNotNull(C) expression filters NULLs while preserving non-NULL row multiplicity");
    } finally {
      spark.stop();
    }
  }

  @Test
  void colStringMatchesSpark(@TempDir Path temp) throws Exception {
    SparkSession spark = newSparkSession("zframe-col-string");
    try (var runtime = new DuckRuntime(new RuntimeConfig(
        "jdbc:duckdb:" + temp.resolve("col-string.duckdb"), 1, 0, null, 1));
         var job = runtime.openJob()) {
      Dataset<org.apache.spark.sql.Row> sparkRows = spark.sql(
          "SELECT * FROM (VALUES (1, 'same'), (2, CAST(NULL AS STRING)), "
              + "(3, 'same'), (4, 'other')) AS records(id, nullable_value)");
      ZFrame<Frame, Row, DuckZColumn> duckRows = ZFrameDuckAdapter.wrap(job.sql(
          "SELECT * FROM (VALUES (1, 'same'), (2, CAST(NULL AS VARCHAR)), "
              + "(3, 'same'), (4, 'other')) AS records(id, nullable_value)"));
      Dataset<org.apache.spark.sql.Row> sparkProjected = sparkRows.select(functions.col("nullable_value"));
      ZFrame<Frame, Row, DuckZColumn> duckProjected = duckRows.select(duckRows.col("nullable_value"));
      assertEquals(Arrays.asList(sparkProjected.columns()), Arrays.asList(duckProjected.columns()),
          "col(String) projection preserves the selected column name");
      assertEquals(indexSparkRows(sparkProjected.collectAsList()),
          indexDuckRows(duckRows.select(duckRows.col("nullable_value")).collectAsList()),
          "col(String) selects identical NULL-aware values and duplicate multiplicities");
    } finally {
      spark.stop();
    }
  }

  @Test
  void concatColumnsMatchesSpark(@TempDir Path temp) throws Exception {
    SparkSession spark = newSparkSession("zframe-concat-columns");
    try (var runtime = new DuckRuntime(new RuntimeConfig(
        "jdbc:duckdb:" + temp.resolve("concat-columns.duckdb"), 1, 0, null, 1));
         var job = runtime.openJob()) {
      Dataset<org.apache.spark.sql.Row> sparkRows = spark.sql(
          "SELECT * FROM (VALUES ('a', 'b'), ('a', 'b'), ('', 'x'), "
              + "(CAST(NULL AS STRING), 'y'), ('z', CAST(NULL AS STRING)), ('', '')) "
              + "AS records(left_value, right_value)");
      ZFrame<Frame, Row, DuckZColumn> duckRows = ZFrameDuckAdapter.wrap(job.sql(
          "SELECT * FROM (VALUES ('a', 'b'), ('a', 'b'), ('', 'x'), "
              + "(CAST(NULL AS VARCHAR), 'y'), ('z', CAST(NULL AS VARCHAR)), ('', '')) "
              + "AS records(left_value, right_value)"));
      assertEquals(indexSparkRows(sparkRows.select(functions.concat(
              functions.col("left_value"), functions.col("right_value"))).collectAsList()),
          indexDuckRows(duckRows.select(duckRows.concat(
              duckRows.col("left_value"), duckRows.col("right_value"))).collectAsList()),
          "concat(C, C) matches Spark string values, NULL propagation, and duplicate multiplicities");
    } finally {
      spark.stop();
    }
  }

  @Test
  void notEqualStringMatchesSpark(@TempDir Path temp) throws Exception {
    SparkSession spark = newSparkSession("zframe-not-equal-string");
    try (var runtime = new DuckRuntime(new RuntimeConfig(
        "jdbc:duckdb:" + temp.resolve("not-equal-string.duckdb"), 1, 0, null, 1));
         var job = runtime.openJob()) {
      Dataset<org.apache.spark.sql.Row> sparkRows = spark.sql(
          "SELECT * FROM (VALUES ('O\\'Reilly', 1), ('other', 2), ('other', 2), "
              + "(CAST(NULL AS STRING), 3), ('', 4)) AS records(text_value, id)");
      ZFrame<Frame, Row, DuckZColumn> duckRows = ZFrameDuckAdapter.wrap(job.sql(
          "SELECT * FROM (VALUES ('O''Reilly', 1), ('other', 2), ('other', 2), "
              + "(CAST(NULL AS VARCHAR), 3), ('', 4)) AS records(text_value, id)"));
      assertEquals(indexSparkRows(sparkRows.filter(
              functions.col("text_value").notEqual("O'Reilly")).collectAsList()),
          indexDuckRows(duckRows.filter(duckRows.notEqual("text_value", "O'Reilly")).collectAsList()),
          "notEqual(String, String) matches Spark filtering, escaped literals, NULL semantics, and multiplicity");
      assertEquals(indexSparkRows(sparkRows.filter(
              functions.col("text_value").notEqual((Object) null)).collectAsList()),
          indexDuckRows(duckRows.filter(duckRows.notEqual("text_value", null)).collectAsList()),
          "notEqual(String, null) preserves SQL UNKNOWN filtering semantics");
    } finally {
      spark.stop();
    }
  }

  @Test
  void notEqualColumnToIdentityMatchesSpark(@TempDir Path temp) throws Exception {
    SparkSession spark = newSparkSession("zframe-not-equal-identity");
    try (var runtime = new DuckRuntime(new RuntimeConfig(
        "jdbc:duckdb:" + temp.resolve("not-equal-identity.duckdb"), 1, 0, null, 1));
         var job = runtime.openJob()) {
      Dataset<org.apache.spark.sql.Row> sparkRows = spark.sql(
          "SELECT * FROM (VALUES ('same', 'same', 1), ('different-left', 'different-right', 2), "
              + "('same', 'same', 1), (CAST(NULL AS STRING), 'present', 3), "
              + "('present', CAST(NULL AS STRING), 4), (CAST(NULL AS STRING), CAST(NULL AS STRING), 5)) "
              + "AS records(value, z_value, id)");
      ZFrame<Frame, Row, DuckZColumn> duckRows = ZFrameDuckAdapter.wrap(job.sql(
          "SELECT * FROM (VALUES ('same', 'same', 1), ('different-left', 'different-right', 2), "
              + "('same', 'same', 1), (CAST(NULL AS VARCHAR), 'present', 3), "
              + "('present', CAST(NULL AS VARCHAR), 4), (CAST(NULL AS VARCHAR), CAST(NULL AS VARCHAR), 5)) "
              + "AS records(value, z_value, id)"));
      assertEquals(indexSparkRows(sparkRows.filter(
              functions.col("value").notEqual(functions.col("z_value"))).collectAsList()),
          indexDuckRows(duckRows.filter(duckRows.notEqual("value")).collectAsList()),
          "notEqual(String) compares with the z_<column> identity and preserves SQL NULL/multiplicity semantics");
    } finally {
      spark.stop();
    }
  }

  @Test
  void notEqualIntMatchesSpark(@TempDir Path temp) throws Exception {
    SparkSession spark = newSparkSession("zframe-not-equal-int");
    try (var runtime = new DuckRuntime(new RuntimeConfig(
        "jdbc:duckdb:" + temp.resolve("not-equal-int.duckdb"), 1, 0, null, 1));
         var job = runtime.openJob()) {
      Dataset<org.apache.spark.sql.Row> sparkRows = spark.sql(
          "SELECT * FROM (VALUES (7, 'equal-target'), (8, 'ordinary'), (8, 'ordinary'), "
              + "(CAST(-2147483648 AS INT), 'minimum'), (CAST(2147483647 AS INT), 'maximum'), "
              + "(CAST(NULL AS INT), 'null')) AS records(metric, label)");
      ZFrame<Frame, Row, DuckZColumn> duckRows = ZFrameDuckAdapter.wrap(job.sql(
          "SELECT * FROM (VALUES (7, 'equal-target'), (8, 'ordinary'), (8, 'ordinary'), "
              + "(CAST(-2147483648 AS INTEGER), 'minimum'), (CAST(2147483647 AS INTEGER), 'maximum'), "
              + "(CAST(NULL AS INTEGER), 'null')) AS records(metric, label)"));
      assertEquals(indexSparkRows(sparkRows.filter(
              functions.col("metric").notEqual(7)).collectAsList()),
          indexDuckRows(duckRows.filter(duckRows.notEqual("metric", 7)).collectAsList()),
          "notEqual(String, int) matches Spark at integer extremes, duplicates, and NULL filtering");
    } finally {
      spark.stop();
    }
  }

  @Test
  void headMatchesSpark(@TempDir Path temp) throws Exception {
    SparkSession spark = newSparkSession("zframe-head");
    try (var runtime = new DuckRuntime(new RuntimeConfig(
        "jdbc:duckdb:" + temp.resolve("head.duckdb"), 1, 0, null, 1));
         var job = runtime.openJob()) {
      Dataset<org.apache.spark.sql.Row> sparkRows = spark.sql(
          "SELECT * FROM (VALUES (2, 'later'), (1, 'first')) AS records(id, value) ORDER BY id");
      ZFrame<Frame, Row, DuckZColumn> duckRows = ZFrameDuckAdapter.wrap(job.sql(
          "SELECT * FROM (VALUES (2, 'later'), (1, 'first')) AS records(id, value) ORDER BY id"));
      assertEquals(sparkRows.head().getInt(0), duckRows.head().get("id"), "head() first ordered row");

      Dataset<org.apache.spark.sql.Row> sparkEmpty = spark.sql("SELECT 1 AS value WHERE FALSE");
      ZFrame<Frame, Row, DuckZColumn> duckEmpty = ZFrameDuckAdapter.wrap(
          job.sql("SELECT 1 AS value WHERE FALSE"));
      assertThrows(java.util.NoSuchElementException.class, sparkEmpty::head);
      assertThrows(java.util.NoSuchElementException.class, duckEmpty::head);
    } finally {
      spark.stop();
    }
  }

  @Test
  void getOnlyObjectFromRowMatchesSpark(@TempDir Path temp) throws Exception {
    SparkSession spark = newSparkSession("zframe-get-only-object");
    try (var runtime = new DuckRuntime(new RuntimeConfig(
        "jdbc:duckdb:" + temp.resolve("get-only-object.duckdb"), 1, 0, null, 1));
         var job = runtime.openJob()) {
      Dataset<org.apache.spark.sql.Row> sparkFrame = spark.sql(
          "SELECT CAST(NULL AS STRING) AS first_value, 5 AS second_value");
      ZFrame<Frame, Row, DuckZColumn> duckFrame = ZFrameDuckAdapter.wrap(job.sql(
          "SELECT CAST(NULL AS VARCHAR) AS first_value, 5 AS second_value"));
      assertEquals(sparkFrame.head().get(0), duckFrame.getOnlyObjectFromRow(duckFrame.head()),
          "getOnlyObjectFromRow() first NULL value");

      Dataset<org.apache.spark.sql.Row> sparkPresent = spark.sql("SELECT 'first' AS first_value, 5 AS second_value");
      ZFrame<Frame, Row, DuckZColumn> duckPresent = ZFrameDuckAdapter.wrap(
          job.sql("SELECT 'first' AS first_value, 5 AS second_value"));
      assertEquals(sparkPresent.head().get(0), duckPresent.getOnlyObjectFromRow(duckPresent.head()),
          "getOnlyObjectFromRow() first value of multi-column row");
    } finally {
      spark.stop();
    }
  }

  @Test
  void collectAsListMatchesSpark(@TempDir Path temp) throws Exception {
    SparkSession spark = newSparkSession("zframe-collect-as-list");
    try (var runtime = new DuckRuntime(new RuntimeConfig(
        "jdbc:duckdb:" + temp.resolve("collect-as-list.duckdb"), 1, 0, null, 1));
         var job = runtime.openJob()) {
      Dataset<org.apache.spark.sql.Row> sparkRows = spark.sql(
          "SELECT * FROM (VALUES (1, 'same'), (2, CAST(NULL AS STRING)), (3, 'same')) "
              + "AS records(id, value) ORDER BY id");
      ZFrame<Frame, Row, DuckZColumn> duckRows = ZFrameDuckAdapter.wrap(job.sql(
          "SELECT * FROM (VALUES (1, 'same'), (2, CAST(NULL AS VARCHAR)), (3, 'same')) "
              + "AS records(id, value) ORDER BY id"));
      assertEquals(indexSparkRows(sparkRows.collectAsList()), indexDuckRows(duckRows.collectAsList()),
          "collectAsList() preserves full-row values and multiplicity");

      Dataset<org.apache.spark.sql.Row> sparkEmpty = spark.sql("SELECT 1 AS value WHERE FALSE");
      ZFrame<Frame, Row, DuckZColumn> duckEmpty = ZFrameDuckAdapter.wrap(
          job.sql("SELECT 1 AS value WHERE FALSE"));
      assertEquals(sparkEmpty.collectAsList().size(), duckEmpty.collectAsList().size(),
          "collectAsList() empty result");
    } finally {
      spark.stop();
    }
  }

  @Test
  void getMatchesSpark(@TempDir Path temp) throws Exception {
    SparkSession spark = newSparkSession("zframe-get");
    try (var runtime = new DuckRuntime(new RuntimeConfig(
        "jdbc:duckdb:" + temp.resolve("get.duckdb"), 1, 0, null, 1));
         var job = runtime.openJob()) {
      Dataset<org.apache.spark.sql.Row> sparkFrame = spark.sql(
          "SELECT 7 AS int_value, CAST(19 AS BIGINT) AS long_value, 2.5D AS double_value, "
              + "'text' AS text_value, CAST(NULL AS STRING) AS nullable_value");
      ZFrame<Frame, Row, DuckZColumn> duckFrame = ZFrameDuckAdapter.wrap(job.sql(
          "SELECT 7 AS int_value, CAST(19 AS BIGINT) AS long_value, CAST(2.5 AS DOUBLE) AS double_value, "
              + "'text' AS text_value, CAST(NULL AS VARCHAR) AS nullable_value"));
      org.apache.spark.sql.Row sparkRow = sparkFrame.head();
      Row duckRow = duckFrame.head();
      assertEquals(sparkRow.getAs("int_value"), duckFrame.get(duckRow, "int_value"), "get() integer value");
      assertEquals(sparkRow.getAs("nullable_value"), duckFrame.get(duckRow, "nullable_value"),
          "get() NULL value");
    } finally {
      spark.stop();
    }
  }

  @Test
  void getAsIntMatchesSpark(@TempDir Path temp) throws Exception {
    SparkSession spark = newSparkSession("zframe-get-as-int");
    try (var runtime = new DuckRuntime(new RuntimeConfig(
        "jdbc:duckdb:" + temp.resolve("get-as-int.duckdb"), 1, 0, null, 1));
         var job = runtime.openJob()) {
      Dataset<org.apache.spark.sql.Row> sparkFrame = spark.sql(
          "SELECT 7 AS int_value, CAST(19 AS BIGINT) AS long_value, 2.5D AS double_value, 'text' AS text_value");
      ZFrame<Frame, Row, DuckZColumn> duckFrame = ZFrameDuckAdapter.wrap(job.sql(
          "SELECT 7 AS int_value, CAST(19 AS BIGINT) AS long_value, CAST(2.5 AS DOUBLE) AS double_value, 'text' AS text_value"));
      org.apache.spark.sql.Row sparkRow = sparkFrame.head();
      Row duckRow = duckFrame.head();
      assertEquals(sparkRow.getInt(sparkFrame.schema().fieldIndex("int_value")),
          duckFrame.getAsInt(duckRow, "int_value"), "getAsInt() Spark parity");
      assertThrows(ClassCastException.class, () -> sparkRow.getInt(sparkFrame.schema().fieldIndex("double_value")));
      assertThrows(ClassCastException.class, () -> duckFrame.getAsInt(duckRow, "double_value"));
    } finally {
      spark.stop();
    }
  }

  @Test
  void getAsLongMatchesSpark(@TempDir Path temp) throws Exception {
    SparkSession spark = newSparkSession("zframe-get-as-long");
    try (var runtime = new DuckRuntime(new RuntimeConfig(
        "jdbc:duckdb:" + temp.resolve("get-as-long.duckdb"), 1, 0, null, 1));
         var job = runtime.openJob()) {
      Dataset<org.apache.spark.sql.Row> sparkFrame = spark.sql(
          "SELECT 7 AS int_value, CAST(19 AS BIGINT) AS long_value, 2.5D AS double_value, 'text' AS text_value");
      ZFrame<Frame, Row, DuckZColumn> duckFrame = ZFrameDuckAdapter.wrap(job.sql(
          "SELECT 7 AS int_value, CAST(19 AS BIGINT) AS long_value, CAST(2.5 AS DOUBLE) AS double_value, 'text' AS text_value"));
      org.apache.spark.sql.Row sparkRow = sparkFrame.head();
      Row duckRow = duckFrame.head();
      assertEquals(sparkRow.getLong(sparkFrame.schema().fieldIndex("long_value")),
          duckFrame.getAsLong(duckRow, "long_value"), "getAsLong() Spark parity");
      assertThrows(ClassCastException.class, () -> sparkRow.getLong(sparkFrame.schema().fieldIndex("int_value")));
      assertThrows(ClassCastException.class, () -> duckFrame.getAsLong(duckRow, "int_value"));
    } finally {
      spark.stop();
    }
  }

  @Test
  void getAsDoubleMatchesSpark(@TempDir Path temp) throws Exception {
    SparkSession spark = newSparkSession("zframe-get-as-double");
    try (var runtime = new DuckRuntime(new RuntimeConfig(
        "jdbc:duckdb:" + temp.resolve("get-as-double.duckdb"), 1, 0, null, 1));
         var job = runtime.openJob()) {
      Dataset<org.apache.spark.sql.Row> sparkFrame = spark.sql(
          "SELECT 7 AS int_value, CAST(19 AS BIGINT) AS long_value, 2.5D AS double_value, 'text' AS text_value");
      ZFrame<Frame, Row, DuckZColumn> duckFrame = ZFrameDuckAdapter.wrap(job.sql(
          "SELECT 7 AS int_value, CAST(19 AS BIGINT) AS long_value, CAST(2.5 AS DOUBLE) AS double_value, 'text' AS text_value"));
      org.apache.spark.sql.Row sparkRow = sparkFrame.head();
      Row duckRow = duckFrame.head();
      assertEquals(sparkRow.getDouble(sparkFrame.schema().fieldIndex("double_value")),
          duckFrame.getAsDouble(duckRow, "double_value"), 0.0d, "getAsDouble() Spark parity");
      assertThrows(ClassCastException.class,
          () -> sparkRow.getDouble(sparkFrame.schema().fieldIndex("int_value")));
      assertThrows(ClassCastException.class, () -> duckFrame.getAsDouble(duckRow, "int_value"));
    } finally {
      spark.stop();
    }
  }

  @Test
  void getAsStringMatchesSpark(@TempDir Path temp) throws Exception {
    SparkSession spark = newSparkSession("zframe-get-as-string");
    try (var runtime = new DuckRuntime(new RuntimeConfig(
        "jdbc:duckdb:" + temp.resolve("get-as-string.duckdb"), 1, 0, null, 1));
         var job = runtime.openJob()) {
      Dataset<org.apache.spark.sql.Row> sparkFrame = spark.sql(
          "SELECT 7 AS int_value, CAST(19 AS BIGINT) AS long_value, 2.5D AS double_value, "
              + "'text' AS text_value, CAST(NULL AS STRING) AS nullable_value");
      ZFrame<Frame, Row, DuckZColumn> duckFrame = ZFrameDuckAdapter.wrap(job.sql(
          "SELECT 7 AS int_value, CAST(19 AS BIGINT) AS long_value, CAST(2.5 AS DOUBLE) AS double_value, "
              + "'text' AS text_value, CAST(NULL AS VARCHAR) AS nullable_value"));
      org.apache.spark.sql.Row sparkRow = sparkFrame.head();
      Row duckRow = duckFrame.head();
      assertEquals(sparkRow.getString(sparkFrame.schema().fieldIndex("text_value")),
          duckFrame.getAsString(duckRow, "text_value"), "getAsString() Spark parity");
      assertEquals(sparkRow.getAs("nullable_value"), duckFrame.getAsString(duckRow, "nullable_value"),
          "getAsString() NULL parity");
      assertThrows(ClassCastException.class,
          () -> sparkRow.getString(sparkFrame.schema().fieldIndex("int_value")));
      assertThrows(ClassCastException.class, () -> duckFrame.getAsString(duckRow, "int_value"));
    } finally {
      spark.stop();
    }
  }

  @Test
  void countMatchesSpark(@TempDir Path temp) throws Exception {
    SparkSession spark = newSparkSession("zframe-count");
    try (var runtime = new DuckRuntime(new RuntimeConfig(
        "jdbc:duckdb:" + temp.resolve("count.duckdb"), 1, 0, null, 1));
         var job = runtime.openJob()) {
      Dataset<org.apache.spark.sql.Row> sparkRows = spark.sql(
          "SELECT CAST(NULL AS STRING) AS value UNION ALL SELECT 'present'");
      ZFrame<Frame, Row, DuckZColumn> duckRows = ZFrameDuckAdapter.wrap(job.sql(
          "SELECT CAST(NULL AS VARCHAR) AS value UNION ALL SELECT 'present'"));
      assertEquals(sparkRows.count(), duckRows.count(), "count() non-empty Spark parity");

      Dataset<org.apache.spark.sql.Row> sparkEmpty = spark.sql("SELECT 1 AS value WHERE FALSE");
      ZFrame<Frame, Row, DuckZColumn> duckEmpty = ZFrameDuckAdapter.wrap(
          job.sql("SELECT 1 AS value WHERE FALSE"));
      assertEquals(sparkEmpty.count(), duckEmpty.count(), "count() empty Spark parity");
    } finally {
      spark.stop();
    }
  }

  @Test
  void showSchemaMatchesSpark(@TempDir Path temp) throws Exception {
    SparkSession spark = newSparkSession("zframe-show-schema");
    try (var runtime = new DuckRuntime(new RuntimeConfig(
        "jdbc:duckdb:" + temp.resolve("show-schema.duckdb"), 1, 0, null, 1));
         var job = runtime.openJob()) {
      Dataset<org.apache.spark.sql.Row> sparkSchema = spark.sql(
          "SELECT CAST(NULL AS STRING) AS nullable_value, CAST(-3 AS INT) AS amount WHERE FALSE");
      ZFrame<Frame, Row, DuckZColumn> duckSchema = ZFrameDuckAdapter.wrap(job.sql(
          "SELECT CAST(NULL AS VARCHAR) AS nullable_value, CAST(-3 AS INTEGER) AS amount WHERE FALSE"));
      assertEquals(normalizeSchemaNullability(sparkSchema.schema().toString()),
          normalizeSchemaNullability(duckSchema.showSchema()), "showSchema() typed empty schema parity");

      Dataset<org.apache.spark.sql.Row> sparkZeroColumns = spark.sql("SELECT 1 AS value WHERE FALSE").drop("value");
      ZFrame<Frame, Row, DuckZColumn> duckZeroColumns = ZFrameDuckAdapter.wrap(
          job.sql("SELECT 1 AS value WHERE FALSE")).drop("value");
      assertEquals(normalizeSchemaNullability(sparkZeroColumns.schema().toString()),
          normalizeSchemaNullability(duckZeroColumns.showSchema()), "showSchema() zero-column parity");
    } finally {
      spark.stop();
    }
  }

  @Test
  void columnsMatchesSpark(@TempDir Path temp) throws Exception {
    SparkSession spark = newSparkSession("zframe-columns");
    try (var runtime = new DuckRuntime(new RuntimeConfig(
        "jdbc:duckdb:" + temp.resolve("columns.duckdb"), 1, 0, null, 1));
         var job = runtime.openJob()) {
      Dataset<org.apache.spark.sql.Row> sparkFrame = spark.sql(
          "SELECT CAST(NULL AS STRING) AS nullable_value, 1 AS ordinal");
      ZFrame<Frame, Row, DuckZColumn> duckFrame = ZFrameDuckAdapter.wrap(job.sql(
          "SELECT CAST(NULL AS VARCHAR) AS nullable_value, 1 AS ordinal"));
      assertEquals(Arrays.asList(sparkFrame.columns()), Arrays.asList(duckFrame.columns()),
          "columns() Spark parity");
    } finally {
      spark.stop();
    }
  }

  @Test
  void fieldNamesMatchesSpark(@TempDir Path temp) throws Exception {
    SparkSession spark = newSparkSession("zframe-field-names");
    try (var runtime = new DuckRuntime(new RuntimeConfig(
        "jdbc:duckdb:" + temp.resolve("field-names.duckdb"), 1, 0, null, 1));
         var job = runtime.openJob()) {
      Dataset<org.apache.spark.sql.Row> sparkFrame = spark.sql(
          "SELECT CAST(NULL AS STRING) AS nullable_value, 1 AS ordinal");
      ZFrame<Frame, Row, DuckZColumn> duckFrame = ZFrameDuckAdapter.wrap(job.sql(
          "SELECT CAST(NULL AS VARCHAR) AS nullable_value, 1 AS ordinal"));
      assertEquals(Arrays.asList(sparkFrame.columns()), Arrays.asList(duckFrame.fieldNames()),
          "fieldNames() Spark parity");
    } finally {
      spark.stop();
    }
  }

  @Test
  void collectFirstColumnMatchesSpark(@TempDir Path temp) throws Exception {
    SparkSession spark = newSparkSession("zframe-collect-first-column");
    try (var runtime = new DuckRuntime(new RuntimeConfig(
        "jdbc:duckdb:" + temp.resolve("collect-first-column.duckdb"), 1, 0, null, 1));
         var job = runtime.openJob()) {
      Dataset<org.apache.spark.sql.Row> sparkFrame = spark.sql(
          "SELECT value FROM (VALUES (1, 'alpha'), (2, CAST(NULL AS STRING)), (3, '')) "
              + "AS records(ordinal, value) ORDER BY ordinal");
      ZFrame<Frame, Row, DuckZColumn> duckFrame = ZFrameDuckAdapter.wrap(job.sql(
          "SELECT value FROM (VALUES (1, 'alpha'), (2, CAST(NULL AS VARCHAR)), (3, '')) "
              + "AS records(ordinal, value) ORDER BY ordinal"));
      var sparkValues = new java.util.ArrayList<String>();
      for (org.apache.spark.sql.Row row : sparkFrame.collectAsList()) {
        sparkValues.add(row.isNullAt(0) ? null : row.getString(0));
      }
      assertEquals(sparkValues, duckFrame.collectFirstColumn(), "collectFirstColumn() Spark parity");
    } finally {
      spark.stop();
    }
  }

  @Test
  void isEmptyMatchesSpark(@TempDir Path temp) throws Exception {
    SparkSession spark = newSparkSession("zframe-is-empty");
    try (var runtime = new DuckRuntime(new RuntimeConfig(
        "jdbc:duckdb:" + temp.resolve("is-empty.duckdb"), 1, 0, null, 1));
         var job = runtime.openJob()) {
      Dataset<org.apache.spark.sql.Row> sparkRows = spark.sql("SELECT 1 AS value");
      ZFrame<Frame, Row, DuckZColumn> duckRows = ZFrameDuckAdapter.wrap(job.sql("SELECT 1 AS value"));
      assertEquals(sparkRows.isEmpty(), duckRows.isEmpty(), "isEmpty() non-empty Spark parity");

      Dataset<org.apache.spark.sql.Row> sparkEmpty = spark.sql("SELECT 1 AS value WHERE FALSE");
      ZFrame<Frame, Row, DuckZColumn> duckEmpty = ZFrameDuckAdapter.wrap(
          job.sql("SELECT 1 AS value WHERE FALSE"));
      assertEquals(sparkEmpty.isEmpty(), duckEmpty.isEmpty(), "isEmpty() empty Spark parity");
    } finally {
      spark.stop();
    }
  }

  @Test
  void aggSumMatchesSpark(@TempDir Path temp) throws Exception {
    SparkSession spark = newSparkSession("zframe-agg-sum");
    try (var runtime = new DuckRuntime(new RuntimeConfig(
        "jdbc:duckdb:" + temp.resolve("agg-sum.duckdb"), 1, 0, null, 1));
         var job = runtime.openJob()) {
      Dataset<org.apache.spark.sql.Row> sparkFrame = spark.sql(
          "SELECT 1.5 AS metric UNION ALL SELECT CAST(NULL AS DOUBLE) UNION ALL SELECT -2.25");
      ZFrame<Frame, Row, DuckZColumn> duckFrame = ZFrameDuckAdapter.wrap(job.sql(
          "SELECT 1.5 AS metric UNION ALL SELECT CAST(NULL AS DOUBLE) UNION ALL SELECT -2.25"));
      double sparkSum = sparkFrame.agg(functions.sum("metric").cast("double"))
          .collectAsList().get(0).getDouble(0);
      assertEquals(sparkSum, duckFrame.aggSum("metric"), 0.0, "aggSum(String) Spark parity");
    } finally {
      spark.stop();
    }
  }

  @Test
  void getMaxValMatchesSpark(@TempDir Path temp) throws Exception {
    SparkSession spark = newSparkSession("zframe-get-max-val");
    try (var runtime = new DuckRuntime(new RuntimeConfig(
        "jdbc:duckdb:" + temp.resolve("get-max-val.duckdb"), 1, 0, null, 1));
         var job = runtime.openJob()) {
      Dataset<org.apache.spark.sql.Row> sparkFrame = spark.sql(
          "SELECT 1.5 AS metric UNION ALL SELECT CAST(NULL AS DOUBLE) UNION ALL SELECT -2.25");
      ZFrame<Frame, Row, DuckZColumn> duckFrame = ZFrameDuckAdapter.wrap(job.sql(
          "SELECT 1.5 AS metric UNION ALL SELECT CAST(NULL AS DOUBLE) UNION ALL SELECT -2.25"));
      Object sparkMax = sparkFrame.agg(functions.max("metric")).collectAsList().get(0).get(0);
      assertEquals(sparkMax, duckFrame.getMaxVal("metric"), "getMaxVal(String) Spark parity");
    } finally {
      spark.stop();
    }
  }

  @Test
  void showNoArgsMatchesSpark(@TempDir Path temp) throws Exception {
    SparkSession spark = newSparkSession("zframe-show-no-args");
    try (var runtime = new DuckRuntime(new RuntimeConfig(
        "jdbc:duckdb:" + temp.resolve("show-no-args.duckdb"), 1, 0, null, 1));
         var job = runtime.openJob()) {
      Dataset<org.apache.spark.sql.Row> sparkFrame = spark.sql(
          "SELECT 1 AS id, 'first' AS value UNION ALL SELECT 2, 'second' ORDER BY id");
      ZFrame<Frame, Row, DuckZColumn> duckFrame = ZFrameDuckAdapter.wrap(job.sql(
          "SELECT 1 AS id, 'first' AS value UNION ALL SELECT 2, 'second' ORDER BY id"));
      assertEquals(normalizeConsoleOutput(sparkFrame.showString(20, 20, false)),
          normalizeConsoleOutput(captureStdout(duckFrame::show)), "show() Spark parity");
    } finally {
      spark.stop();
    }
  }

  @Test
  void showBooleanMatchesSpark(@TempDir Path temp) throws Exception {
    SparkSession spark = newSparkSession("zframe-show-boolean");
    try (var runtime = new DuckRuntime(new RuntimeConfig(
        "jdbc:duckdb:" + temp.resolve("show-boolean.duckdb"), 1, 0, null, 1));
         var job = runtime.openJob()) {
      Dataset<org.apache.spark.sql.Row> sparkFrame = spark.sql(
          "SELECT 1 AS id, 'first' AS value UNION ALL SELECT 2, 'second' ORDER BY id");
      ZFrame<Frame, Row, DuckZColumn> duckFrame = ZFrameDuckAdapter.wrap(job.sql(
          "SELECT 1 AS id, 'first' AS value UNION ALL SELECT 2, 'second' ORDER BY id"));
      assertEquals(normalizeConsoleOutput(sparkFrame.showString(20, 0, false)),
          normalizeConsoleOutput(captureStdout(() -> duckFrame.show(false))),
          "show(boolean) Spark parity");
    } finally {
      spark.stop();
    }
  }

  @Test
  void showIntMatchesSpark(@TempDir Path temp) throws Exception {
    SparkSession spark = newSparkSession("zframe-show-int");
    try (var runtime = new DuckRuntime(new RuntimeConfig(
        "jdbc:duckdb:" + temp.resolve("show-int.duckdb"), 1, 0, null, 1));
         var job = runtime.openJob()) {
      Dataset<org.apache.spark.sql.Row> sparkFrame = spark.sql(
          "SELECT 1 AS id, 'first' AS value UNION ALL SELECT 2, 'second' ORDER BY id");
      ZFrame<Frame, Row, DuckZColumn> duckFrame = ZFrameDuckAdapter.wrap(job.sql(
          "SELECT 1 AS id, 'first' AS value UNION ALL SELECT 2, 'second' ORDER BY id"));
      assertEquals(normalizeConsoleOutput(sparkFrame.showString(1, 20, false)),
          normalizeConsoleOutput(captureStdout(() -> duckFrame.show(1))), "show(int) Spark parity");
    } finally {
      spark.stop();
    }
  }

  @Test
  void showIntBooleanMatchesSpark(@TempDir Path temp) throws Exception {
    SparkSession spark = newSparkSession("zframe-show-int-boolean");
    try (var runtime = new DuckRuntime(new RuntimeConfig(
        "jdbc:duckdb:" + temp.resolve("show-int-boolean.duckdb"), 1, 0, null, 1));
         var job = runtime.openJob()) {
      Dataset<org.apache.spark.sql.Row> sparkFrame = spark.sql(
          "SELECT 1 AS id, 'first' AS value UNION ALL SELECT 2, 'second' ORDER BY id");
      ZFrame<Frame, Row, DuckZColumn> duckFrame = ZFrameDuckAdapter.wrap(job.sql(
          "SELECT 1 AS id, 'first' AS value UNION ALL SELECT 2, 'second' ORDER BY id"));
      assertEquals(normalizeConsoleOutput(sparkFrame.showString(2, 0, false)),
          normalizeConsoleOutput(captureStdout(() -> duckFrame.show(2, false))),
          "show(int, boolean) Spark parity");
    } finally {
      spark.stop();
    }
  }

  private static SparkSession newSparkSession(String appName) {
    SparkSession spark = SparkSession.builder()
        .appName(appName)
        .master("local[1]")
        .config("spark.ui.enabled", "false")
        .config("spark.sql.shuffle.partitions", "1")
        .config("spark.sql.adaptive.enabled", "false")
        .config("spark.driver.host", "127.0.0.1")
        .getOrCreate();
    spark.sparkContext().setLogLevel("ERROR");
    return spark;
  }

  @Test
  void aliasedSelfJoinPreservesDuplicateNamesAndSqlNullEquality(@TempDir Path temp) throws Exception {
    SparkSession spark = newSparkSession("zframe-aliased-self-join-duplicate-names");
    try (var runtime = new DuckRuntime(new RuntimeConfig(
        "jdbc:duckdb:" + temp.resolve("aliased-self-join.duckdb"), 1, 0, null, 1));
         var job = runtime.openJob()) {
      Dataset<org.apache.spark.sql.Row> sparkBase = spark.sql(
          "SELECT CAST(NULL AS STRING) AS primary_group, 'null-key' AS payload "
              + "UNION ALL SELECT 'p', 'first' UNION ALL SELECT 'p', 'second'");
      ZFrame<Frame, Row, DuckZColumn> duckBase = ZFrameDuckAdapter.wrap(job.sql(
          "SELECT CAST(NULL AS VARCHAR) AS primary_group, 'null-key' AS payload "
              + "UNION ALL SELECT 'p', 'first' UNION ALL SELECT 'p', 'second'"));
      Dataset<org.apache.spark.sql.Row> sparkLeft = sparkBase.as("left_records");
      Dataset<org.apache.spark.sql.Row> sparkRight = sparkBase.as("right_records");
      Dataset<org.apache.spark.sql.Row> sparkJoined = sparkLeft.join(sparkRight,
          sparkLeft.col("primary_group").equalTo(sparkRight.col("primary_group")), "inner");
      ZFrame<Frame, Row, DuckZColumn> duckLeft = duckBase.as("left_records");
      ZFrame<Frame, Row, DuckZColumn> duckRight = duckBase.as("right_records");
      DuckZColumn joinCondition = duckLeft.equalTo(
          duckLeft.col("primary_group"), duckRight.col("primary_group"));

      assertEquals(List.of(sparkJoined.columns()), List.of(duckLeft.join(duckRight, joinCondition, "inner").columns()),
          "condition join retains duplicate Spark-visible column names from both aliases");
      assertEquals(indexSparkRows(sparkJoined.collectAsList()),
          indexDuckRows(duckLeft.join(duckRight, joinCondition, "inner").collectAsList()),
          "aliased self-join retains Spark row contents and duplicate multiplicities");
      assertEquals(4L, sparkJoined.count(), "NULL keys do not match and two duplicate non-NULL keys form 2x2 rows");
      assertEquals(sparkJoined.count(), duckLeft.join(duckRight, joinCondition, "inner").count());
    } finally {
      spark.stop();
    }
  }

  @Test
  void asStringMatchesSparkForQualifiedSelfJoin(@TempDir Path temp) throws Exception {
    SparkSession spark = newSparkSession("zframe-as-string-qualified-self-join");
    try (var runtime = new DuckRuntime(new RuntimeConfig(
        "jdbc:duckdb:" + temp.resolve("as-string-qualified-self-join.duckdb"), 1, 0, null, 1));
         var job = runtime.openJob()) {
      Dataset<org.apache.spark.sql.Row> sparkBase = spark.sql(
          "SELECT 'p' AS group_key, 'left' AS payload UNION ALL SELECT 'p', 'right'");
      ZFrame<Frame, Row, DuckZColumn> duckBase = ZFrameDuckAdapter.wrap(job.sql(
          "SELECT 'p' AS group_key, 'left' AS payload UNION ALL SELECT 'p', 'right'"));

      assertEquals(List.of(sparkBase.as("comparison_alias").columns()),
          List.of(duckBase.as("comparison_alias").columns()));
      assertEquals(indexSparkRows(sparkBase.as("comparison_alias").collectAsList()),
          indexDuckRows(duckBase.as("comparison_alias").collectAsList()));

      Dataset<org.apache.spark.sql.Row> sparkLeft = sparkBase.as("left_records");
      Dataset<org.apache.spark.sql.Row> sparkRight = sparkBase.as("right_records");
      Dataset<org.apache.spark.sql.Row> sparkJoined = sparkLeft.join(sparkRight,
          sparkLeft.col("group_key").equalTo(sparkRight.col("group_key")), "inner");
      ZFrame<Frame, Row, DuckZColumn> duckLeft = duckBase.as("left_records");
      ZFrame<Frame, Row, DuckZColumn> duckRight = duckBase.as("right_records");
      DuckZColumn condition = duckLeft.equalTo(
          duckLeft.col("group_key"), duckRight.col("group_key"));
      ZFrame<Frame, Row, DuckZColumn> duckJoined = duckLeft.join(duckRight, condition, "inner");

      assertEquals(List.of(sparkJoined.columns()), List.of(duckJoined.columns()));
      assertEquals(indexSparkRows(sparkJoined.collectAsList()), indexDuckRows(duckJoined.collectAsList()),
          "separate as(String) aliases must retain independent qualified column references");
      assertEquals(4L, sparkJoined.count(), "duplicate keys produce the full many-to-many self-join");
      assertEquals(sparkJoined.count(), duckJoined.count());
    } finally {
      spark.stop();
    }
  }

  @Test
  void prefixedJoinMatchesSparkForDuplicatesAndNulls(@TempDir Path temp) throws Exception {
    SparkSession spark = newSparkSession("zframe-prefixed-join-duplicates-nulls");
    try (var runtime = new DuckRuntime(new RuntimeConfig(
        "jdbc:duckdb:" + temp.resolve("prefixed-join.duckdb"), 1, 0, null, 1));
         var job = runtime.openJob()) {
      Dataset<org.apache.spark.sql.Row> sparkLeft = spark.sql(
          "SELECT 'p' AS join_key, 'L1' AS left_payload "
              + "UNION ALL SELECT 'p', 'L2' UNION ALL SELECT 'q', 'L3' "
              + "UNION ALL SELECT CAST(NULL AS STRING), 'LN'");
      Dataset<org.apache.spark.sql.Row> sparkRight = spark.sql(
          "SELECT 'p' AS z_join_key, 'R1' AS right_payload "
              + "UNION ALL SELECT 'p', 'R2' UNION ALL SELECT 'r', 'R3' "
              + "UNION ALL SELECT CAST(NULL AS STRING), 'RN'");
      ZFrame<Frame, Row, DuckZColumn> duckLeft = ZFrameDuckAdapter.wrap(job.sql(
          "SELECT 'p' AS join_key, 'L1' AS left_payload "
              + "UNION ALL SELECT 'p', 'L2' UNION ALL SELECT 'q', 'L3' "
              + "UNION ALL SELECT CAST(NULL AS VARCHAR), 'LN'"));
      ZFrame<Frame, Row, DuckZColumn> duckRight = ZFrameDuckAdapter.wrap(job.sql(
          "SELECT 'p' AS z_join_key, 'R1' AS right_payload "
              + "UNION ALL SELECT 'p', 'R2' UNION ALL SELECT 'r', 'R3' "
              + "UNION ALL SELECT CAST(NULL AS VARCHAR), 'RN'"));
      Dataset<org.apache.spark.sql.Row> sparkJoined = sparkLeft.join(sparkRight,
          sparkLeft.col("join_key").equalTo(sparkRight.col("z_join_key")), "inner");

      assertEquals(List.of(sparkJoined.columns()),
          List.of(duckLeft.join(duckRight, "join_key").columns()),
          "two-argument join exposes the left key and prefixed right key schema");
      assertEquals(indexSparkRows(sparkJoined.collectAsList()),
          indexDuckRows(duckLeft.join(duckRight, "join_key").collectAsList()),
          "prefixed-key join preserves Spark rows and 2x2 duplicate matches");
      assertEquals(4L, sparkJoined.count(), "NULL keys do not match; duplicate p keys form four pairs");
      assertEquals(sparkJoined.count(), duckLeft.join(duckRight, "join_key").count());
    } finally {
      spark.stop();
    }
  }

  @Test
  void rightJoinMatchesSparkForDuplicateNullAndUnmatchedKeys(@TempDir Path temp) throws Exception {
    SparkSession spark = newSparkSession("zframe-right-join-duplicates-nulls");
    try (var runtime = new DuckRuntime(new RuntimeConfig(
        "jdbc:duckdb:" + temp.resolve("right-join.duckdb"), 1, 0, null, 1));
         var job = runtime.openJob()) {
      Dataset<org.apache.spark.sql.Row> sparkLeft = spark.sql(
          "SELECT 'a' AS join_key, 'L1' AS left_payload "
              + "UNION ALL SELECT 'a', 'L2' UNION ALL SELECT 'b', 'L3' "
              + "UNION ALL SELECT CAST(NULL AS STRING), 'LN'");
      Dataset<org.apache.spark.sql.Row> sparkRight = spark.sql(
          "SELECT 'a' AS join_key, 'R1' AS right_payload "
              + "UNION ALL SELECT 'a', 'R2' UNION ALL SELECT 'c', 'R3' "
              + "UNION ALL SELECT CAST(NULL AS STRING), 'RN'");
      ZFrame<Frame, Row, DuckZColumn> duckLeft = ZFrameDuckAdapter.wrap(job.sql(
          "SELECT 'a' AS join_key, 'L1' AS left_payload "
              + "UNION ALL SELECT 'a', 'L2' UNION ALL SELECT 'b', 'L3' "
              + "UNION ALL SELECT CAST(NULL AS VARCHAR), 'LN'"));
      ZFrame<Frame, Row, DuckZColumn> duckRight = ZFrameDuckAdapter.wrap(job.sql(
          "SELECT 'a' AS join_key, 'R1' AS right_payload "
              + "UNION ALL SELECT 'a', 'R2' UNION ALL SELECT 'c', 'R3' "
              + "UNION ALL SELECT CAST(NULL AS VARCHAR), 'RN'"));
      Dataset<org.apache.spark.sql.Row> sparkJoined = sparkLeft.join(sparkRight,
          sparkLeft.col("join_key").equalTo(sparkRight.col("join_key")), "right_outer");

      assertEquals(List.of(sparkJoined.columns()),
          List.of(duckLeft.joinRight(duckRight, "join_key").columns()),
          "right join preserves Spark's two-key-column schema");
      assertEquals(indexSparkRows(sparkJoined.collectAsList()),
          indexDuckRows(duckLeft.joinRight(duckRight, "join_key").collectAsList()),
          "right join preserves duplicate matches and unmatched right-side rows");
      assertEquals(6L, sparkJoined.count(), "2x2 matches plus unmatched and NULL-key right rows");
      assertEquals(sparkJoined.count(), duckLeft.joinRight(duckRight, "join_key").count());
    } finally {
      spark.stop();
    }
  }

  @Test
  void joinOnColumnUsingMatchesSparkForDuplicatesAndNulls(@TempDir Path temp) throws Exception {
    SparkSession spark = newSparkSession("zframe-join-on-column-using");
    try (var runtime = new DuckRuntime(new RuntimeConfig(
        "jdbc:duckdb:" + temp.resolve("join-on-column.duckdb"), 1, 0, null, 1));
         var job = runtime.openJob()) {
      Dataset<org.apache.spark.sql.Row> sparkLeft = spark.sql(
          "SELECT 'a' AS join_key, 'L1' AS left_payload "
              + "UNION ALL SELECT 'a', 'L2' UNION ALL SELECT 'b', 'L3' "
              + "UNION ALL SELECT CAST(NULL AS STRING), 'LN'");
      Dataset<org.apache.spark.sql.Row> sparkRight = spark.sql(
          "SELECT 'a' AS join_key, 'R1' AS right_payload "
              + "UNION ALL SELECT 'a', 'R2' UNION ALL SELECT 'c', 'R3' "
              + "UNION ALL SELECT CAST(NULL AS STRING), 'RN'");
      ZFrame<Frame, Row, DuckZColumn> duckLeft = ZFrameDuckAdapter.wrap(job.sql(
          "SELECT 'a' AS join_key, 'L1' AS left_payload "
              + "UNION ALL SELECT 'a', 'L2' UNION ALL SELECT 'b', 'L3' "
              + "UNION ALL SELECT CAST(NULL AS VARCHAR), 'LN'"));
      ZFrame<Frame, Row, DuckZColumn> duckRight = ZFrameDuckAdapter.wrap(job.sql(
          "SELECT 'a' AS join_key, 'R1' AS right_payload "
              + "UNION ALL SELECT 'a', 'R2' UNION ALL SELECT 'c', 'R3' "
              + "UNION ALL SELECT CAST(NULL AS VARCHAR), 'RN'"));
      Dataset<org.apache.spark.sql.Row> sparkJoined = sparkLeft.join(sparkRight, "join_key");

      assertEquals(List.of(sparkJoined.columns()),
          List.of(duckLeft.joinOnCol(duckRight, "join_key").columns()),
          "USING join emits the shared key once in Spark column order");
      assertEquals(indexSparkRows(sparkJoined.collectAsList()),
          indexDuckRows(duckLeft.joinOnCol(duckRight, "join_key").collectAsList()),
          "USING join preserves duplicate-key Cartesian matches and excludes NULL-key pairs");
      assertEquals(4L, sparkJoined.count(), "two duplicate a keys on each side produce four matches");
      assertEquals(sparkJoined.count(), duckLeft.joinOnCol(duckRight, "join_key").count());
    } finally {
      spark.stop();
    }
  }

  @Test
  void joinOnColumnExpressionPreservesSparkDuplicateNames(@TempDir Path temp) throws Exception {
    SparkSession spark = newSparkSession("zframe-join-on-column-expression-duplicate-names");
    try (var runtime = new DuckRuntime(new RuntimeConfig(
        "jdbc:duckdb:" + temp.resolve("join-on-column-expression.duckdb"), 1, 0, null, 1));
         var job = runtime.openJob()) {
      Dataset<org.apache.spark.sql.Row> sparkLeft = spark.sql(
          "SELECT 'a' AS join_key, 'L1' AS left_payload "
              + "UNION ALL SELECT 'a', 'L2' UNION ALL SELECT 'b', 'L3' "
              + "UNION ALL SELECT CAST(NULL AS STRING), 'LN'");
      Dataset<org.apache.spark.sql.Row> sparkRight = spark.sql(
          "SELECT 'a' AS join_key, 'R1' AS right_payload "
              + "UNION ALL SELECT 'a', 'R2' UNION ALL SELECT 'c', 'R3' "
              + "UNION ALL SELECT CAST(NULL AS STRING), 'RN'");
      ZFrame<Frame, Row, DuckZColumn> duckLeft = ZFrameDuckAdapter.wrap(job.sql(
          "SELECT 'a' AS join_key, 'L1' AS left_payload "
              + "UNION ALL SELECT 'a', 'L2' UNION ALL SELECT 'b', 'L3' "
              + "UNION ALL SELECT CAST(NULL AS VARCHAR), 'LN'"));
      ZFrame<Frame, Row, DuckZColumn> duckRight = ZFrameDuckAdapter.wrap(job.sql(
          "SELECT 'a' AS join_key, 'R1' AS right_payload "
              + "UNION ALL SELECT 'a', 'R2' UNION ALL SELECT 'c', 'R3' "
              + "UNION ALL SELECT CAST(NULL AS VARCHAR), 'RN'"));
      Dataset<org.apache.spark.sql.Row> sparkJoined = sparkLeft.join(sparkRight,
          sparkLeft.col("join_key").equalTo(sparkRight.col("join_key")));
      ZFrame<Frame, Row, DuckZColumn> duckJoined = duckLeft.joinOnCol(duckRight,
          duckLeft.equalTo(duckLeft.col("join_key"), duckRight.col("join_key")));
      assertEquals(List.of(sparkJoined.columns()), List.of(duckLeft.joinOnCol(duckRight,
          duckLeft.equalTo(duckLeft.col("join_key"), duckRight.col("join_key"))).columns()),
          "expression join preserves both duplicate Spark-visible key names");
      assertEquals(indexSparkRows(sparkJoined.collectAsList()), indexDuckRows(duckLeft.joinOnCol(duckRight,
          duckLeft.equalTo(duckLeft.col("join_key"), duckRight.col("join_key"))).collectAsList()),
          "expression join preserves Spark row values and duplicate-key multiplicity");
      assertEquals(4L, sparkJoined.count(), "duplicate keys produce four matches and NULL keys do not match");
      assertEquals(sparkJoined.count(), duckLeft.joinOnCol(duckRight,
          duckLeft.equalTo(duckLeft.col("join_key"), duckRight.col("join_key"))).count());
    } finally {
      spark.stop();
    }
  }

  @Test
  void distinctMatchesSparkForDuplicateAndNullRows(@TempDir Path temp) throws Exception {
    SparkSession spark = newSparkSession("zframe-distinct-duplicate-null-rows");
    try (var runtime = new DuckRuntime(new RuntimeConfig(
        "jdbc:duckdb:" + temp.resolve("distinct.duckdb"), 1, 0, null, 1));
         var job = runtime.openJob()) {
      Dataset<org.apache.spark.sql.Row> sparkInput = spark.sql(
          "SELECT 'p' AS group_key, 'same' AS payload "
              + "UNION ALL SELECT 'p', 'same' UNION ALL SELECT 'p', 'other' "
              + "UNION ALL SELECT CAST(NULL AS STRING), 'null-row' "
              + "UNION ALL SELECT CAST(NULL AS STRING), 'null-row'");
      ZFrame<Frame, Row, DuckZColumn> duckInput = ZFrameDuckAdapter.wrap(job.sql(
          "SELECT 'p' AS group_key, 'same' AS payload "
              + "UNION ALL SELECT 'p', 'same' UNION ALL SELECT 'p', 'other' "
              + "UNION ALL SELECT CAST(NULL AS VARCHAR), 'null-row' "
              + "UNION ALL SELECT CAST(NULL AS VARCHAR), 'null-row'"));

      assertEquals(List.of(sparkInput.columns()), List.of(duckInput.distinct().columns()));
      assertEquals(indexSparkRows(sparkInput.distinct().collectAsList()),
          indexDuckRows(duckInput.distinct().collectAsList()),
          "distinct removes duplicate full rows while retaining one NULL-bearing row");
      assertEquals(3L, sparkInput.distinct().count());
      assertEquals(sparkInput.distinct().count(), duckInput.distinct().count());
    } finally {
      spark.stop();
    }
  }

  @Test
  void exceptAndIntersectMatchSparkSetSemanticsForDuplicatesAndNulls(@TempDir Path temp) throws Exception {
    SparkSession spark = newSparkSession("zframe-except-intersect-null-set-semantics");
    try (var runtime = new DuckRuntime(new RuntimeConfig(
        "jdbc:duckdb:" + temp.resolve("except-intersect.duckdb"), 1, 0, null, 1));
         var job = runtime.openJob()) {
      Dataset<org.apache.spark.sql.Row> sparkLeft = spark.sql(
          "SELECT 'a' AS group_key, 'shared' AS payload "
              + "UNION ALL SELECT 'a', 'shared' UNION ALL SELECT 'b', 'left-only' "
              + "UNION ALL SELECT CAST(NULL AS STRING), 'null-shared' "
              + "UNION ALL SELECT CAST(NULL AS STRING), 'null-shared' "
              + "UNION ALL SELECT 'd', 'left-only'");
      Dataset<org.apache.spark.sql.Row> sparkRight = spark.sql(
          "SELECT 'a' AS group_key, 'shared' AS payload "
              + "UNION ALL SELECT 'a', 'shared' UNION ALL SELECT 'c', 'right-only' "
              + "UNION ALL SELECT CAST(NULL AS STRING), 'null-shared' "
              + "UNION ALL SELECT 'd', 'right-only'");
      ZFrame<Frame, Row, DuckZColumn> duckLeft = ZFrameDuckAdapter.wrap(job.sql(
          "SELECT 'a' AS group_key, 'shared' AS payload "
              + "UNION ALL SELECT 'a', 'shared' UNION ALL SELECT 'b', 'left-only' "
              + "UNION ALL SELECT CAST(NULL AS VARCHAR), 'null-shared' "
              + "UNION ALL SELECT CAST(NULL AS VARCHAR), 'null-shared' "
              + "UNION ALL SELECT 'd', 'left-only'"));
      ZFrame<Frame, Row, DuckZColumn> duckRight = ZFrameDuckAdapter.wrap(job.sql(
          "SELECT 'a' AS group_key, 'shared' AS payload "
              + "UNION ALL SELECT 'a', 'shared' UNION ALL SELECT 'c', 'right-only' "
              + "UNION ALL SELECT CAST(NULL AS VARCHAR), 'null-shared' "
              + "UNION ALL SELECT 'd', 'right-only'"));
      Dataset<org.apache.spark.sql.Row> sparkExcept = sparkLeft.except(sparkRight);
      Dataset<org.apache.spark.sql.Row> sparkIntersect = sparkLeft.intersect(sparkRight);

      assertEquals(List.of(sparkExcept.columns()), List.of(duckLeft.except(duckRight).columns()));
      assertEquals(indexSparkRows(sparkExcept.collectAsList()),
          indexDuckRows(duckLeft.except(duckRight).collectAsList()),
          "except removes shared rows null-safely and returns distinct unmatched rows");
      assertEquals(2L, sparkExcept.count());
      assertEquals(sparkExcept.count(), duckLeft.except(duckRight).count());

      assertEquals(List.of(sparkIntersect.columns()), List.of(duckLeft.intersect(duckRight).columns()));
      assertEquals(indexSparkRows(sparkIntersect.collectAsList()),
          indexDuckRows(duckLeft.intersect(duckRight).collectAsList()),
          "intersect retains one copy of each shared row, including the NULL-bearing row");
      assertEquals(2L, sparkIntersect.count());
      assertEquals(sparkIntersect.count(), duckLeft.intersect(duckRight).count());
    } finally {
      spark.stop();
    }
  }

  @Test
  void groupByCountOverloadsMatchSparkForDuplicatesAndNullKeys(@TempDir Path temp) throws Exception {
    SparkSession spark = newSparkSession("zframe-group-by-count-null-semantics");
    try (var runtime = new DuckRuntime(new RuntimeConfig(
        "jdbc:duckdb:" + temp.resolve("group-by-count.duckdb"), 1, 0, null, 1));
         var job = runtime.openJob()) {
      Dataset<org.apache.spark.sql.Row> sparkInput = spark.sql(
          "SELECT 'p' AS first_key, 'x' AS second_key "
              + "UNION ALL SELECT 'p', 'x' UNION ALL SELECT 'p', CAST(NULL AS STRING) "
              + "UNION ALL SELECT CAST(NULL AS STRING), 'x' "
              + "UNION ALL SELECT CAST(NULL AS STRING), 'x' "
              + "UNION ALL SELECT CAST(NULL AS STRING), CAST(NULL AS STRING) "
              + "UNION ALL SELECT 'q', 'x'");
      ZFrame<Frame, Row, DuckZColumn> duckInput = ZFrameDuckAdapter.wrap(job.sql(
          "SELECT 'p' AS first_key, 'x' AS second_key "
              + "UNION ALL SELECT 'p', 'x' UNION ALL SELECT 'p', CAST(NULL AS VARCHAR) "
              + "UNION ALL SELECT CAST(NULL AS VARCHAR), 'x' "
              + "UNION ALL SELECT CAST(NULL AS VARCHAR), 'x' "
              + "UNION ALL SELECT CAST(NULL AS VARCHAR), CAST(NULL AS VARCHAR) "
              + "UNION ALL SELECT 'q', 'x'"));

      Dataset<org.apache.spark.sql.Row> sparkOneKey = sparkInput.groupBy("first_key")
          .agg(functions.count(functions.lit(1)).alias("row_count"));
      Dataset<org.apache.spark.sql.Row> sparkTwoKeys = sparkInput.groupBy("first_key", "second_key")
          .agg(functions.count(functions.col("first_key")).alias("first_key_count"));

      assertEquals(List.of(sparkOneKey.columns()), List.of(duckInput.groupByCount(
          "first_key", "row_count").columns()));
      assertEquals(indexSparkRows(sparkOneKey.collectAsList()), indexDuckRows(duckInput.groupByCount(
          "first_key", "row_count").collectAsList()),
          "one-key overload counts every row, including NULL-key groups");
      assertEquals(indexSparkRows(sparkTwoKeys.collectAsList()), indexDuckRows(duckInput.groupByCount(
          "first_key", "second_key", "first_key_count").collectAsList()),
          "two-key overload counts the first key, so NULL first-key groups have count zero");
    } finally {
      spark.stop();
    }
  }

  @Test
  void substrMatchesSparkForNegativeZeroAndLongRanges(@TempDir Path temp) throws Exception {
    SparkSession spark = newSparkSession("zframe-substr-position-boundaries");
    try (var runtime = new DuckRuntime(new RuntimeConfig(
        "jdbc:duckdb:" + temp.resolve("substr.duckdb"), 1, 0, null, 1));
         var job = runtime.openJob()) {
      Dataset<org.apache.spark.sql.Row> sparkInput = spark.sql(
          "SELECT 1 AS row_id, 'abcdef' AS text_value "
              + "UNION ALL SELECT 2, 'xy' UNION ALL SELECT 3, '' "
              + "UNION ALL SELECT 4, CAST(NULL AS STRING)");
      ZFrame<Frame, Row, DuckZColumn> duckInput = ZFrameDuckAdapter.wrap(job.sql(
          "SELECT 1 AS row_id, 'abcdef' AS text_value "
              + "UNION ALL SELECT 2, 'xy' UNION ALL SELECT 3, '' "
              + "UNION ALL SELECT 4, CAST(NULL AS VARCHAR)"));
      Dataset<org.apache.spark.sql.Row> sparkProjected = sparkInput.select(
          functions.col("row_id"),
          functions.substring(functions.col("text_value"), -2, 2).alias("negative_slice"),
          functions.substring(functions.col("text_value"), 0, 3).alias("zero_start_slice"),
          functions.substring(functions.col("text_value"), 2, 20).alias("long_slice"));

      assertEquals(List.of(sparkProjected.columns()), List.of(duckInput.select(new DuckZColumn[] {
          duckInput.col("row_id"),
          duckInput.substr(duckInput.col("text_value"), -2, 2).as("negative_slice"),
          duckInput.substr(duckInput.col("text_value"), 0, 3).as("zero_start_slice"),
          duckInput.substr(duckInput.col("text_value"), 2, 20).as("long_slice")}).columns()));
      assertEquals(indexSparkRows(sparkProjected.collectAsList()), indexDuckRows(duckInput.select(
          new DuckZColumn[] {duckInput.col("row_id"),
              duckInput.substr(duckInput.col("text_value"), -2, 2).as("negative_slice"),
              duckInput.substr(duckInput.col("text_value"), 0, 3).as("zero_start_slice"),
              duckInput.substr(duckInput.col("text_value"), 2, 20).as("long_slice")})
          .collectAsList()),
          "substring matches Spark for negative/zero starts, long lengths, empty strings and NULL");
    } finally {
      spark.stop();
    }
  }

  @Test
  void splitMatchesSparkForRepeatedTrailingEmptyAndNullValues(@TempDir Path temp) throws Exception {
    SparkSession spark = newSparkSession("zframe-split-edge-cases");
    try (var runtime = new DuckRuntime(new RuntimeConfig(
        "jdbc:duckdb:" + temp.resolve("split-edge-cases.duckdb"), 1, 0, null, 1));
         var job = runtime.openJob()) {
      Dataset<org.apache.spark.sql.Row> sparkInput = spark.sql(
          "SELECT 'alpha|beta' AS words UNION ALL SELECT 'left||right' "
              + "UNION ALL SELECT 'trailing|' UNION ALL SELECT '' "
              + "UNION ALL SELECT CAST(NULL AS STRING)");
      ZFrame<Frame, Row, DuckZColumn> duckInput = ZFrameDuckAdapter.wrap(job.sql(
          "SELECT 'alpha|beta' AS words UNION ALL SELECT 'left||right' "
              + "UNION ALL SELECT 'trailing|' UNION ALL SELECT '' "
              + "UNION ALL SELECT CAST(NULL AS VARCHAR)"));

      Dataset<org.apache.spark.sql.Row> expected = sparkInput.select(
          functions.split(functions.col("words"), "\\|").alias("parts"));
      assertEquals(List.of(expected.columns()),
          List.of(duckInput.split("words", "\\|", "parts").columns()));
      assertEquals(indexSparkArrays(expected.collectAsList(), "parts"),
          indexDuckArrays(duckInput.split("words", "\\|", "parts").collectAsList(), "parts"),
          "split output schema, token order, repeated delimiters, trailing empty tokens, empty and NULL input");
    } finally {
      spark.stop();
    }
  }

  @Test
  void sampleDoubleMatchesSparkForNoReplacementStatistics(@TempDir Path temp) throws Exception {
    SparkSession spark = newSparkSession("zframe-sample-double-statistics");
    try (var runtime = new DuckRuntime(new RuntimeConfig(
        "jdbc:duckdb:" + temp.resolve("sample-double-statistics.duckdb"), 1, 0, null, 1));
         var job = runtime.openJob()) {
      Dataset<org.apache.spark.sql.Row> sparkInput = spark.range(20_000).toDF("sample_id");
      ZFrame<Frame, Row, DuckZColumn> duckInput = ZFrameDuckAdapter.wrap(
          job.sql("SELECT range AS sample_id FROM range(20000)"));
      long sparkRows = sparkInput.sample(false, 0.25d).count();
      assertTrue(sparkRows >= 4_500 && sparkRows <= 5_500,
          "Spark double no-replacement sample should approximate the requested 25% fraction");
      assertTrue(isValidNoReplacementSample(duckInput.sample(false, 0.25d)),
          "DuckDB exact sample(boolean,double) overload should approximate Spark's 25% sample rate without duplicate source IDs");
    } finally {
      spark.stop();
    }
  }

  @Test
  void withColumnRenamedMatchesSparkForExistingAndMissingNames(@TempDir Path temp) throws Exception {
    SparkSession spark = newSparkSession("zframe-with-column-renamed");
    try (var runtime = new DuckRuntime(new RuntimeConfig(
        "jdbc:duckdb:" + temp.resolve("with-column-renamed.duckdb"), 1, 0, null, 1));
         var job = runtime.openJob()) {
      Dataset<org.apache.spark.sql.Row> sparkInput = spark.sql(
          "SELECT * FROM (VALUES ('a', 'first'), ('a', 'first'), (CAST(NULL AS STRING), 'null-key')) "
              + "AS records(group_key, value_key)");
      ZFrame<Frame, Row, DuckZColumn> duckInput = ZFrameDuckAdapter.wrap(job.sql(
          "SELECT * FROM (VALUES ('a', 'first'), ('a', 'first'), (CAST(NULL AS VARCHAR), 'null-key')) "
              + "AS records(group_key, value_key)"));

      Dataset<org.apache.spark.sql.Row> sparkRenamed = sparkInput.withColumnRenamed("group_key", "renamed_key");
      assertEquals(List.of(sparkRenamed.columns()),
          List.of(duckInput.withColumnRenamed("group_key", "renamed_key").columns()));
      assertEquals(indexSparkRows(sparkRenamed.collectAsList()),
          indexDuckRows(duckInput.withColumnRenamed("group_key", "renamed_key").collectAsList()),
          "renaming an existing column preserves NULLs and duplicate row multiplicity");

      Dataset<org.apache.spark.sql.Row> sparkMissing = sparkInput.withColumnRenamed("missing", "renamed");
      assertEquals(List.of(sparkMissing.columns()),
          List.of(duckInput.withColumnRenamed("missing", "renamed").columns()));
      assertEquals(indexSparkRows(sparkMissing.collectAsList()),
          indexDuckRows(duckInput.withColumnRenamed("missing", "renamed").collectAsList()),
          "renaming a missing column is a no-op in Spark and the adapter");
    } finally {
      spark.stop();
    }
  }

  @Test
  void explodeMatchesSparkForNullEmptyAndDuplicateArrayElements(@TempDir Path temp) throws Exception {
    SparkSession spark = newSparkSession("zframe-explode-edge-cases");
    try (var runtime = new DuckRuntime(new RuntimeConfig(
        "jdbc:duckdb:" + temp.resolve("explode-edge-cases.duckdb"), 1, 0, null, 1));
         var job = runtime.openJob()) {
      Dataset<org.apache.spark.sql.Row> sparkInput = spark.sql(
          "SELECT ARRAY(1, CAST(NULL AS INT), 2) AS items "
              + "UNION ALL SELECT ARRAY(2, 2) "
              + "UNION ALL SELECT CAST(ARRAY() AS ARRAY<INT>) "
              + "UNION ALL SELECT CAST(NULL AS ARRAY<INT>)");
      ZFrame<Frame, Row, DuckZColumn> duckInput = ZFrameDuckAdapter.wrap(job.sql(
          "SELECT [1, CAST(NULL AS INTEGER), 2] AS items "
              + "UNION ALL SELECT [2, 2] "
              + "UNION ALL SELECT CAST([] AS INTEGER[]) "
              + "UNION ALL SELECT CAST(NULL AS INTEGER[])"));
      Dataset<org.apache.spark.sql.Row> expected = sparkInput.select(
          functions.explode(functions.col("items")).alias("item"));
      assertEquals(List.of(expected.columns()),
          List.of(duckInput.explode("items", "item").columns()));
      assertEquals(indexSparkRows(expected.collectAsList()),
          indexDuckRows(duckInput.explode("items", "item").collectAsList()),
          "explode must preserve repeated and NULL elements while dropping empty/NULL arrays");
    } finally {
      spark.stop();
    }
  }

  @Test
  void selectStringVarargsMatchesSparkForOrderDuplicatesAndNulls(@TempDir Path temp) throws Exception {
    SparkSession spark = newSparkSession("zframe-select-string-varargs");
    try (var runtime = new DuckRuntime(new RuntimeConfig(
        "jdbc:duckdb:" + temp.resolve("select-string-varargs.duckdb"), 1, 0, null, 1));
         var job = runtime.openJob()) {
      Dataset<org.apache.spark.sql.Row> sparkInput = spark.sql(
          "SELECT * FROM (VALUES ('a', 'first'), ('a', 'first'), "
              + "(CAST(NULL AS STRING), 'null-key')) AS records(group_key, value_key)");
      ZFrame<Frame, Row, DuckZColumn> duckInput = ZFrameDuckAdapter.wrap(job.sql(
          "SELECT * FROM (VALUES ('a', 'first'), ('a', 'first'), "
              + "(CAST(NULL AS VARCHAR), 'null-key')) AS records(group_key, value_key)"));
      Dataset<org.apache.spark.sql.Row> expected = sparkInput.select(
          "value_key", "group_key", "value_key");
      assertEquals(List.of(expected.columns()),
          List.of(duckInput.select("value_key", "group_key", "value_key").columns()));
      assertEquals(indexSparkRows(expected.collectAsList()),
          indexDuckRows(duckInput.select("value_key", "group_key", "value_key").collectAsList()),
          "String varargs selection must preserve requested order, duplicate projection, NULLs and row multiplicity");
    } finally {
      spark.stop();
    }
  }

  @Test
  void selectedOperationsMatchSparkForNullAndDuplicateFixtures(@TempDir Path temp) throws Exception {
    SparkSession spark = SparkSession.builder()
        .appName("zingg-duckdb-zframe-differential")
        .master("local[1]")
        .config("spark.ui.enabled", "false")
        .config("spark.sql.shuffle.partitions", "1")
        .config("spark.sql.adaptive.enabled", "false")
        .config("spark.driver.host", "127.0.0.1")
        .getOrCreate();
    spark.sparkContext().setLogLevel("ERROR");
    try (var runtime = new DuckRuntime(new RuntimeConfig(
        "jdbc:duckdb:" + temp.resolve("group-count.duckdb"), 1, 0, null, 1));
         var job = runtime.openJob()) {
      Dataset<org.apache.spark.sql.Row> sparkInput = spark.sql(
          "SELECT CAST(NULL AS STRING) AS primary_group, 'x' AS secondary_group "
              + "UNION ALL SELECT CAST(NULL AS STRING), 'y' "
              + "UNION ALL SELECT CAST(NULL AS STRING), 'x' "
              + "UNION ALL SELECT 'p', 'x' "
              + "UNION ALL SELECT 'p', 'x'");
      Frame duckInput = job.sql(
          "SELECT CAST(NULL AS VARCHAR) AS primary_group, 'x' AS secondary_group "
              + "UNION ALL SELECT CAST(NULL AS VARCHAR), 'y' "
              + "UNION ALL SELECT CAST(NULL AS VARCHAR), 'x' "
              + "UNION ALL SELECT 'p', 'x' "
              + "UNION ALL SELECT 'p', 'x'");
      ZFrame<Frame, Row, DuckZColumn> duckFrame = ZFrameDuckAdapter.wrap(duckInput);

      assertEquals(Arrays.asList(sparkInput.columns()), duckFrame.df().columns());
      assertEquals(sparkInput.count(), duckFrame.df().count());
      assertEquals(List.of("\"primary_group\"", "\"secondary_group\""),
          Arrays.stream(duckFrame.getCols()).map(DuckZColumn::sql).toList());
      assertFrameContents(sparkInput, ZFrameDuckAdapter.wrap(duckFrame.df()).select(duckFrame.getCols()));

      Dataset<org.apache.spark.sql.Row> sparkShowFrame = spark.sql(
          "SELECT 1 AS id, 'FIRST_MARKER_abcdefghijklmnopqrstuvwxyz' AS description "
              + "UNION ALL SELECT 2, 'SECOND_MARKER_abcdefghijklmnopqrstuvwxyz' ORDER BY id");
      ZFrame<Frame, Row, DuckZColumn> duckShowFrame = ZFrameDuckAdapter.wrap(job.sql(
          "SELECT 1 AS id, 'FIRST_MARKER_abcdefghijklmnopqrstuvwxyz' AS description "
              + "UNION ALL SELECT 2, 'SECOND_MARKER_abcdefghijklmnopqrstuvwxyz' ORDER BY id"));
      String sparkDefaultShow = sparkShowFrame.showString(20, 20, false);
      String duckDefaultShow = captureStdout(duckShowFrame::show);
      assertEquals(normalizeConsoleOutput(sparkDefaultShow), normalizeConsoleOutput(duckDefaultShow), "show()");
      String sparkNoTruncate = sparkShowFrame.showString(20, 0, false);
      String duckNoTruncate = captureStdout(() -> duckShowFrame.show(false));
      assertEquals(normalizeConsoleOutput(sparkNoTruncate), normalizeConsoleOutput(duckNoTruncate), "show(boolean)");
      String sparkOneRow = sparkShowFrame.showString(1, 20, false);
      String duckOneRow = captureStdout(() -> duckShowFrame.show(1));
      assertEquals(normalizeConsoleOutput(sparkOneRow), normalizeConsoleOutput(duckOneRow), "show(int)");
      String sparkLimitedNoTruncate = sparkShowFrame.showString(2, 0, false);
      String duckLimitedNoTruncate = captureStdout(() -> duckShowFrame.show(2, false));
      assertEquals(normalizeConsoleOutput(sparkLimitedNoTruncate), normalizeConsoleOutput(duckLimitedNoTruncate),
          "show(int, boolean)");

      Dataset<org.apache.spark.sql.Row> sparkShowEmpty = spark.sql(
          "SELECT CAST(NULL AS STRING) AS empty_value WHERE FALSE");
      ZFrame<Frame, Row, DuckZColumn> duckShowEmpty = ZFrameDuckAdapter.wrap(job.sql(
          "SELECT CAST(NULL AS VARCHAR) AS empty_value WHERE FALSE"));
      assertEquals(normalizeConsoleOutput(sparkShowEmpty.showString(20, 20, false)),
          normalizeConsoleOutput(captureStdout(duckShowEmpty::show)), "show() empty input");

      Dataset<org.apache.spark.sql.Row> sparkShowMany = spark.range(21).toDF("id");
      ZFrame<Frame, Row, DuckZColumn> duckShowMany = ZFrameDuckAdapter.wrap(
          job.sql("SELECT range AS id FROM range(21)"));
      assertEquals(normalizeConsoleOutput(sparkShowMany.showString(20, 20, false)),
          normalizeConsoleOutput(captureStdout(duckShowMany::show)), "show() with truncated row count");
      assertEquals(normalizeConsoleOutput(sparkShowMany.showString(-1, 20, false)),
          normalizeConsoleOutput(captureStdout(() -> duckShowMany.show(-1))), "show(int) with negative limit");

      Dataset<org.apache.spark.sql.Row> sparkShowSpecial = spark.sql(
          "SELECT CAST(NULL AS STRING) AS nullable, concat('line', chr(10), 'break') AS control");
      ZFrame<Frame, Row, DuckZColumn> duckShowSpecial = ZFrameDuckAdapter.wrap(job.sql(
          "SELECT CAST(NULL AS VARCHAR) AS nullable, concat('line', chr(10), 'break') AS control"));
      assertEquals(normalizeConsoleOutput(sparkShowSpecial.showString(20, 20, false)),
          normalizeConsoleOutput(captureStdout(duckShowSpecial::show)), "show() NULL/control escaping");

      Dataset<org.apache.spark.sql.Row> sparkShowDuplicateNames = spark.sql(
          "SELECT 1 AS first_value, 'x' AS second_value").toDF("same", "same");
      ZFrame<Frame, Row, DuckZColumn> duckShowDuplicateNames = ZFrameDuckAdapter.wrap(job.sql(
          "SELECT 1 AS first_value, 'x' AS second_value")).toDF(new String[] {"same", "same"});
      assertEquals(normalizeConsoleOutput(sparkShowDuplicateNames.showString(20, 20, false)),
          normalizeConsoleOutput(captureStdout(duckShowDuplicateNames::show)), "show() duplicate names");

      Dataset<org.apache.spark.sql.Row> sparkAliasLeft = sparkInput.as("left_records");
      Dataset<org.apache.spark.sql.Row> sparkAliasRight = sparkInput.as("right_records");
      Dataset<org.apache.spark.sql.Row> sparkAliasJoin = sparkAliasLeft.join(sparkAliasRight,
          sparkAliasLeft.col("primary_group").equalTo(sparkAliasRight.col("primary_group")), "inner");
      ZFrame<Frame, Row, DuckZColumn> duckAliasLeft = duckFrame.as("left_records");
      ZFrame<Frame, Row, DuckZColumn> duckAliasRight = duckFrame.as("right_records");
      ZFrame<Frame, Row, DuckZColumn> duckAliasJoin = duckAliasLeft.join(duckAliasRight,
          duckAliasLeft.equalTo(duckAliasLeft.col("primary_group"), duckAliasRight.col("primary_group")), "inner");
      assertEquals(List.of(sparkAliasLeft.columns()), List.of(duckAliasLeft.columns()));
      assertEquals(indexSparkRows(sparkAliasLeft.collectAsList()), indexDuckRows(duckAliasLeft.collectAsList()));
      assertEquals(List.of(sparkAliasJoin.columns()), List.of(duckAliasJoin.columns()));
      assertEquals(indexSparkRows(sparkAliasJoin.collectAsList()), indexDuckRows(duckAliasJoin.collectAsList()));
      assertEquals(4L, sparkAliasJoin.count(),
          "self-join equality must exclude NULL keys while retaining the 2x2 duplicate-key matches");
      assertEquals(sparkAliasJoin.count(), duckAliasJoin.count(),
          "aliased self-join cardinality must preserve duplicate multiplicity and SQL NULL semantics");

      Map<Object, Long> sparkOneKey = indexBy(sparkInput.groupBy("primary_group").count()
          .collectAsList(), "primary_group", "count");
      Map<Object, Long> duckOneKey = indexDuckBy(
          duckFrame.groupByCount("primary_group", "count").collectAsList(), "primary_group", "count");
      assertEquals(sparkOneKey, duckOneKey);

      Dataset<org.apache.spark.sql.Row> sparkComposite = sparkInput
          .groupBy("primary_group", "secondary_group")
          .agg(org.apache.spark.sql.functions.count(
              org.apache.spark.sql.functions.col("primary_group")).alias("non_null_primary_count"));
      Map<Key, Long> sparkTwoKey = indexByComposite(sparkComposite.collectAsList());
      Map<Key, Long> duckTwoKey = indexDuckComposite(duckFrame.groupByCount(
          "primary_group", "secondary_group", "non_null_primary_count").collectAsList());
      assertEquals(sparkTwoKey, duckTwoKey);
      assertEquals(0L, sparkTwoKey.get(new Key(null, "x")));
      assertEquals(2L, sparkTwoKey.get(new Key("p", "x")));

      Dataset<org.apache.spark.sql.Row> sparkDistinct = sparkInput.distinct();
      ZFrame<Frame, Row, DuckZColumn> duckDistinct = duckFrame.distinct();
      assertEquals(List.of(sparkDistinct.columns()), List.of(duckDistinct.columns()));
      assertEquals(indexSparkPairs(sparkDistinct.collectAsList(), "primary_group", "secondary_group"),
          indexDuckPairs(duckDistinct.collectAsList(), "primary_group", "secondary_group"));

      Dataset<org.apache.spark.sql.Row> sparkExceptRight = spark.sql(
          "SELECT 'p' AS primary_group, 'x' AS secondary_group");
      Frame duckExceptRight = job.sql(
          "SELECT 'p' AS primary_group, 'x' AS secondary_group");
      Dataset<org.apache.spark.sql.Row> sparkExcept = sparkInput.except(sparkExceptRight);
      ZFrame<Frame, Row, DuckZColumn> duckExcept = duckFrame.except(ZFrameDuckAdapter.wrap(duckExceptRight));
      assertEquals(List.of(sparkExcept.columns()), List.of(duckExcept.columns()));
      assertEquals(indexSparkPairs(sparkExcept.collectAsList(), "primary_group", "secondary_group"),
          indexDuckPairs(duckExcept.collectAsList(), "primary_group", "secondary_group"));

      Dataset<org.apache.spark.sql.Row> sparkIntersectRight = spark.sql(
          "SELECT CAST(NULL AS STRING) AS primary_group, 'x' AS secondary_group "
              + "UNION ALL SELECT 'p', 'x' UNION ALL SELECT 'p', 'x'");
      Frame duckIntersectRight = job.sql(
          "SELECT CAST(NULL AS VARCHAR) AS primary_group, 'x' AS secondary_group "
              + "UNION ALL SELECT 'p', 'x' UNION ALL SELECT 'p', 'x'");
      Dataset<org.apache.spark.sql.Row> sparkIntersect = sparkInput.intersect(sparkIntersectRight);
      ZFrame<Frame, Row, DuckZColumn> duckIntersect = duckFrame.intersect(
          ZFrameDuckAdapter.wrap(duckIntersectRight));
      assertEquals(List.of(sparkIntersect.columns()), List.of(duckIntersect.columns()));
      assertEquals(indexSparkPairs(sparkIntersect.collectAsList(), "primary_group", "secondary_group"),
          indexDuckPairs(duckIntersect.collectAsList(), "primary_group", "secondary_group"));

      Dataset<org.apache.spark.sql.Row> sparkUnionRight = spark.sql(
          "SELECT 'extra' AS secondary_group, 'q' AS primary_group");
      Frame duckUnionRight = job.sql(
          "SELECT 'extra' AS secondary_group, 'q' AS primary_group");
      Dataset<org.apache.spark.sql.Row> sparkUnion = sparkInput.unionByName(sparkUnionRight, false);
      ZFrame<Frame, Row, DuckZColumn> duckUnion = duckFrame.unionByName(
          ZFrameDuckAdapter.wrap(duckUnionRight), false);
      assertEquals(List.of(sparkUnion.columns()), List.of(duckUnion.columns()));
      assertEquals(indexSparkPairs(sparkUnion.collectAsList(), "primary_group", "secondary_group"),
          indexDuckPairs(duckUnion.collectAsList(), "primary_group", "secondary_group"));

      Dataset<org.apache.spark.sql.Row> sparkPositionalUnion = sparkInput.union(sparkExceptRight);
      ZFrame<Frame, Row, DuckZColumn> duckPositionalUnion = duckFrame.union(
          ZFrameDuckAdapter.wrap(duckExceptRight));
      assertEquals(List.of(sparkPositionalUnion.columns()), List.of(duckPositionalUnion.columns()));
      assertEquals(indexSparkRows(sparkPositionalUnion.collectAsList()),
          indexDuckRows(duckPositionalUnion.collectAsList()));
      Dataset<org.apache.spark.sql.Row> sparkUnionAll = sparkInput.unionAll(sparkExceptRight);
      ZFrame<Frame, Row, DuckZColumn> duckUnionAll = duckFrame.unionAll(
          ZFrameDuckAdapter.wrap(duckExceptRight));
      assertEquals(List.of(sparkUnionAll.columns()), List.of(duckUnionAll.columns()));
      assertEquals(indexSparkRows(sparkUnionAll.collectAsList()), indexDuckRows(duckUnionAll.collectAsList()));

      Dataset<org.apache.spark.sql.Row> sparkMissingColumnRight = spark.sql(
          "SELECT 'q' AS primary_group, 'new-value' AS added_column");
      Frame duckMissingColumnRight = job.sql(
          "SELECT 'q' AS primary_group, 'new-value' AS added_column");
      Dataset<org.apache.spark.sql.Row> sparkAllowMissing = sparkInput.unionByName(sparkMissingColumnRight, true);
      ZFrame<Frame, Row, DuckZColumn> duckAllowMissing = duckFrame.unionByName(
          ZFrameDuckAdapter.wrap(duckMissingColumnRight), true);
      assertEquals(List.of(sparkAllowMissing.columns()), List.of(duckAllowMissing.columns()));
      assertEquals(indexSparkRows(sparkAllowMissing.collectAsList()), indexDuckRows(duckAllowMissing.collectAsList()));

      Dataset<org.apache.spark.sql.Row> sparkDropDuplicatesAll =
          sparkInput.dropDuplicates("primary_group", "secondary_group");
      ZFrame<Frame, Row, DuckZColumn> duckDropDuplicatesAll =
          duckFrame.dropDuplicates("primary_group", "secondary_group");
      assertEquals(indexSparkRows(sparkDropDuplicatesAll.collectAsList()),
          indexDuckRows(duckDropDuplicatesAll.collectAsList()));
      Dataset<org.apache.spark.sql.Row> sparkDropDuplicatesSubset =
          sparkInput.dropDuplicates(new String[] {"primary_group"});
      ZFrame<Frame, Row, DuckZColumn> duckDropDuplicatesSubset =
          duckFrame.dropDuplicates(new String[] {"primary_group"});
      assertEquals(indexSingleColumn(sparkDropDuplicatesSubset.collectAsList(), "primary_group"),
          indexSingleColumnDuck(duckDropDuplicatesSubset.collectAsList(), "primary_group"));

      Dataset<org.apache.spark.sql.Row> sparkLiteralColumn = sparkInput.withColumn(
          "source_tag", functions.lit("fixture"));
      ZFrame<Frame, Row, DuckZColumn> duckLiteralColumn = duckFrame.withColumn("source_tag", "fixture");
      assertEquals(List.of(sparkLiteralColumn.columns()), List.of(duckLiteralColumn.columns()));
      assertEquals(indexSparkRows(sparkLiteralColumn.collectAsList()), indexDuckRows(duckLiteralColumn.collectAsList()));

      Dataset<org.apache.spark.sql.Row> sparkNullable = spark.sql(
          "SELECT 'x' AS group_key, CAST(NULL AS STRING) AS value_key "
              + "UNION ALL SELECT 'x', 'a' UNION ALL SELECT 'x', 'a' "
              + "UNION ALL SELECT CAST(NULL AS STRING), 'b' UNION ALL SELECT CAST(NULL AS STRING), CAST(NULL AS STRING)");
      Frame duckNullable = job.sql(
          "SELECT 'x' AS group_key, CAST(NULL AS VARCHAR) AS value_key "
              + "UNION ALL SELECT 'x', 'a' UNION ALL SELECT 'x', 'a' "
              + "UNION ALL SELECT CAST(NULL AS VARCHAR), 'b' UNION ALL SELECT CAST(NULL AS VARCHAR), CAST(NULL AS VARCHAR)");
      ZFrame<Frame, Row, DuckZColumn> nullableFrame = ZFrameDuckAdapter.wrap(duckNullable);
      Dataset<org.apache.spark.sql.Row> sparkEmptyColumnProjection = sparkNullable.select(new org.apache.spark.sql.Column[0]);
      assertEquals(List.of(), List.of(sparkEmptyColumnProjection.columns()));
      assertEquals(sparkNullable.count(), sparkEmptyColumnProjection.count());
      assertEquals(sparkNullable.collectAsList().size(), sparkEmptyColumnProjection.collectAsList().size());
      ZFrame<Frame, Row, DuckZColumn> duckEmptyColumnProjection = nullableFrame.select(new DuckZColumn[0]);
      assertFrameContents(sparkEmptyColumnProjection, duckEmptyColumnProjection);
      assertEquals(normalizeConsoleOutput(sparkEmptyColumnProjection.showString(20, 20, false)),
          normalizeConsoleOutput(captureStdout(duckEmptyColumnProjection::show)),
          "show() zero-column projection with preserved row count");
      assertEquals("StructType()", duckEmptyColumnProjection.showSchema());
      assertEquals(List.of(), duckEmptyColumnProjection.df().columns());
      assertEquals(indexDuckRows(duckEmptyColumnProjection.collectAsList()),
          indexDuckRows(duckEmptyColumnProjection.df().collect()));
      var sparkEmptyColumnSequence = JavaConverters.asScalaIteratorConverter(
          List.<org.apache.spark.sql.Column>of().iterator()).asScala().toSeq();
      Dataset<org.apache.spark.sql.Row> sparkEmptySequenceProjection = sparkNullable.select(sparkEmptyColumnSequence);
      assertEquals(List.of(), List.of(sparkEmptySequenceProjection.columns()));
      assertEquals(sparkNullable.count(), sparkEmptySequenceProjection.count());
      ZFrame<Frame, Row, DuckZColumn> duckEmptySequenceProjection = nullableFrame.select(List.of());
      assertFrameContents(sparkEmptySequenceProjection, duckEmptySequenceProjection);
      assertThrows(IllegalArgumentException.class, () -> nullableFrame.select((String[]) null));
      assertThrows(IllegalArgumentException.class, () -> nullableFrame.select(new String[0]));

      Dataset<org.apache.spark.sql.Row> sparkExprInput = spark.sql(
          "SELECT * FROM (VALUES (1, 1.5D, 'alpha', CAST(NULL AS STRING), 0, 'alpha', CAST(10 AS BIGINT)), "
              + "(2, 2.5D, 'beta', 'x', 1, 'gamma', CAST(20 AS BIGINT)), "
              + "(3, 3.5D, 'alphabet', '', 2, 'alphabet', CAST(30 AS BIGINT))) "
              + "t(id, metric, text_value, nullable_value, z_id, z_text_value, long_value)");
      Frame duckExprInput = job.sql(
          "SELECT * FROM (VALUES (1, CAST(1.5 AS DOUBLE), 'alpha', CAST(NULL AS VARCHAR), 0, 'alpha', CAST(10 AS BIGINT)), "
              + "(2, CAST(2.5 AS DOUBLE), 'beta', 'x', 1, 'gamma', CAST(20 AS BIGINT)), "
              + "(3, CAST(3.5 AS DOUBLE), 'alphabet', '', 2, 'alphabet', CAST(30 AS BIGINT))) "
              + "t(id, metric, text_value, nullable_value, z_id, z_text_value, long_value)");
      ZFrame<Frame, Row, DuckZColumn> sparkExprFrame = ZFrameDuckAdapter.wrap(duckExprInput);

      Dataset<org.apache.spark.sql.Row> sparkRowsForAccess = sparkExprInput.orderBy("id");
      ZFrame<Frame, Row, DuckZColumn> duckRowsForAccess = sparkExprFrame.sortAscending("id");
      org.apache.spark.sql.Row sparkHead = sparkRowsForAccess.head();
      Row duckHead = duckRowsForAccess.head();
      assertEquals(sparkHead.getAs("id"), duckRowsForAccess.get(duckHead, "id"));
      assertEquals(sparkHead.getAs("id"), duckRowsForAccess.getOnlyObjectFromRow(duckHead));
      assertEquals(((Number) sparkHead.getAs("id")).intValue(), duckRowsForAccess.getAsInt(duckHead, "id"));
      assertEquals(((Number) sparkHead.getAs("long_value")).longValue(),
          duckRowsForAccess.getAsLong(duckHead, "long_value"));
      assertEquals(((Number) sparkHead.getAs("metric")).doubleValue(),
          duckRowsForAccess.getAsDouble(duckHead, "metric"), 0.0d);
      assertThrows(ClassCastException.class,
          () -> sparkRowsForAccess.collectAsList().get(0).getInt(1));
      assertThrows(ClassCastException.class, () -> duckRowsForAccess.getAsInt(duckHead, "metric"));
      assertEquals(sparkHead.getAs("text_value"), duckRowsForAccess.getAsString(duckHead, "text_value"));
      assertThrows(ClassCastException.class, () -> sparkRowsForAccess.collectAsList().get(0).getString(0));
      assertThrows(ClassCastException.class, duckRowsForAccess::collectFirstColumn);
      assertThrows(ClassCastException.class, () -> sparkRowsForAccess.collectAsList().get(0).getInt(2));
      assertThrows(ClassCastException.class, () -> duckRowsForAccess.getAsInt(duckHead, "text_value"));
      assertNull(sparkRowsForAccess.collectAsList().get(0).getAs("nullable_value"));
      assertNull(duckRowsForAccess.get(duckHead, "nullable_value"));
      assertThrows(Throwable.class,
          () -> sparkRowsForAccess.collectAsList().get(0).getInt(3));
      assertThrows(NullPointerException.class,
          () -> duckRowsForAccess.getAsInt(duckHead, "nullable_value"));
      assertEquals(sparkNullable.collectAsList().stream().map(row -> (String) row.get(0)).toList(),
          nullableFrame.collectFirstColumn());
      ZFrame<Frame, Row, DuckZColumn> firstColumn = ZFrameDuckAdapter.wrap(
          job.sql("SELECT * FROM (VALUES ('alpha'), (CAST(NULL AS VARCHAR)), ('')) t(value)"));
      assertEquals(Arrays.asList("alpha", null, ""), firstColumn.collectFirstColumn());
      assertEquals(List.of(), ZFrameDuckAdapter.wrap(job.sql("SELECT 'x' AS value WHERE FALSE"))
          .collectFirstColumn());
      assertEquals(false, duckRowsForAccess.isEmpty());
      assertEquals(true, ZFrameDuckAdapter.wrap(job.sql("SELECT 1 AS value WHERE FALSE")).isEmpty());
      assertEquals(normalizeSchemaNullability(sparkRowsForAccess.schema().toString()),
          normalizeSchemaNullability(duckRowsForAccess.showSchema()));
      zingg.common.client.FieldData[] duckFields = duckRowsForAccess.fields();
      org.apache.spark.sql.types.StructField[] sparkFields = sparkRowsForAccess.schema().fields();
      assertEquals(sparkFields.length, duckFields.length);
      for (int index = 0; index < sparkFields.length; index++) {
        assertEquals(sparkFields[index].name(), duckFields[index].getName());
        assertEquals(sparkRowsForAccess.schema().fieldIndex(sparkFields[index].name()),
            duckRowsForAccess.fieldIndex(duckFields[index].getName()));
        assertEquals(sparkFields[index].dataType().toString(), duckFields[index].getDataType());
        assertTrue(!sparkFields[index].nullable() || duckFields[index].isNullable(),
            "DuckDB metadata must not claim stronger non-nullability than Spark");
      }
      assertThrows(IllegalArgumentException.class, () -> sparkRowsForAccess.schema().fieldIndex("missing_field"));
      assertThrows(IllegalArgumentException.class, () -> duckRowsForAccess.fieldIndex("missing_field"));
      Dataset<org.apache.spark.sql.Row> sparkUnsignedSchema = spark.sql(
          "SELECT CAST(255 AS SMALLINT) AS u8, CAST(65535 AS INTEGER) AS u16, "
              + "CAST(4294967295 AS BIGINT) AS u32, CAST(18446744073709551615 AS DECIMAL(20,0)) AS u64");
      ZFrame<Frame, Row, DuckZColumn> duckUnsignedSchema = ZFrameDuckAdapter.wrap(job.sql(
          "SELECT CAST(255 AS UTINYINT) AS u8, CAST(65535 AS USMALLINT) AS u16, "
              + "CAST(4294967295 AS UINTEGER) AS u32, CAST(18446744073709551615 AS UBIGINT) AS u64"));
      assertEquals(normalizeSchemaNullability(sparkUnsignedSchema.schema().toString()),
          normalizeSchemaNullability(duckUnsignedSchema.showSchema()));
      assertEquals(Arrays.stream(sparkUnsignedSchema.schema().fields())
              .map(field -> field.dataType().toString()).toList(),
          Arrays.stream(duckUnsignedSchema.fields()).map(zingg.common.client.FieldData::getDataType).toList());
      assertFrameContents(sparkExprInput.sample(false, 0.0), sparkExprFrame.sample(false, 0.0));
      assertFrameContents(sparkExprInput.sample(false, 1.0), sparkExprFrame.sample(false, 1.0));
      assertFrameContents(sparkExprInput.sample(false, 0.0f), sparkExprFrame.sample(false, 0.0f));
      assertFrameContents(sparkExprInput.sample(false, 1.0f), sparkExprFrame.sample(false, 1.0f));
      Dataset<org.apache.spark.sql.Row> sparkSamplingPopulation = spark.range(20_000).toDF("sample_id");
      ZFrame<Frame, Row, DuckZColumn> duckSamplingPopulation = ZFrameDuckAdapter.wrap(
          job.sql("SELECT range AS sample_id FROM range(20000)"));
      Dataset<org.apache.spark.sql.Row> sparkSample = sparkSamplingPopulation.sample(false, 0.25).cache();
      ZFrame<Frame, Row, DuckZColumn> duckSample = duckSamplingPopulation.sample(false, 0.25d).cache();
      assertEquals(List.of(sparkSamplingPopulation.columns()), List.of(duckSamplingPopulation.columns()));
      assertTrue(sparkSample.count() >= 4_500 && sparkSample.count() <= 5_500,
          "Spark no-replacement sample count should be consistent with its requested fraction");
      assertTrue(duckSample.count() >= 4_500 && duckSample.count() <= 5_500,
          "DuckDB no-replacement sample count should be consistent with its requested fraction");
      assertEquals(sparkSample.count(), sparkSample.select("sample_id").distinct().count());
      assertEquals(duckSample.count(), duckSample.select("sample_id").distinct().count());
      Dataset<org.apache.spark.sql.Row> sparkFloatSample = sparkSamplingPopulation.sample(false, 0.25f).cache();
      ZFrame<Frame, Row, DuckZColumn> duckFloatSample = duckSamplingPopulation.sample(false, 0.25f).cache();
      assertTrue(sparkFloatSample.count() >= 4_500 && sparkFloatSample.count() <= 5_500,
          "Spark float no-replacement sample count should be consistent with its requested fraction");
      assertTrue(duckFloatSample.count() >= 4_500 && duckFloatSample.count() <= 5_500,
          "DuckDB float no-replacement sample count should be consistent with its requested fraction");
      assertEquals(sparkFloatSample.count(), sparkFloatSample.select("sample_id").distinct().count());
      assertEquals(duckFloatSample.count(), duckFloatSample.select("sample_id").distinct().count());
      Dataset<org.apache.spark.sql.Row> sparkReplacementSample =
          sparkSamplingPopulation.sample(true, 0.25).cache();
      ZFrame<Frame, Row, DuckZColumn> duckReplacementSample =
          duckSamplingPopulation.sample(true, 0.25d).cache();
      assertReplacementSampleShape(sparkReplacementSample, "Spark double");
      assertReplacementSampleShape(duckReplacementSample, "DuckDB double");
      Dataset<org.apache.spark.sql.Row> sparkFloatReplacementSample =
          sparkSamplingPopulation.sample(true, 0.25f).cache();
      ZFrame<Frame, Row, DuckZColumn> duckFloatReplacementSample =
          duckSamplingPopulation.sample(true, 0.25f).cache();
      assertReplacementSampleShape(sparkFloatReplacementSample, "Spark float");
      assertReplacementSampleShape(duckFloatReplacementSample, "DuckDB float");
      assertFrameContents(sparkExprInput.coalesce(2), sparkExprFrame.coalesce(2));
      assertFrameContents(sparkExprInput.repartition(2), sparkExprFrame.repartition(2));
      assertFrameContents(sparkExprInput.repartition(2, functions.col("z_id")),
          sparkExprFrame.repartition(2, sparkExprFrame.col("z_id")));
      var sparkPartitionExpressions = JavaConverters.asScalaIteratorConverter(
          List.of(functions.col("z_id"), functions.col("text_value")).iterator()).asScala().toSeq();
      var duckPartitionExpressions = JavaConverters.asScalaIteratorConverter(
          List.of(sparkExprFrame.col("z_id"), sparkExprFrame.col("text_value")).iterator()).asScala().toSeq();
      assertFrameContents(sparkExprInput.repartition(2, sparkPartitionExpressions),
          sparkExprFrame.repartition(2, duckPartitionExpressions));
      assertFrameContents(sparkExprInput.repartition(sparkPartitionExpressions),
          sparkExprFrame.repartition(duckPartitionExpressions));

      Dataset<org.apache.spark.sql.Row> sparkSplitInput = spark.sql(
          "SELECT 'alpha|beta' AS words UNION ALL SELECT 'left||right' "
              + "UNION ALL SELECT 'trailing|' UNION ALL SELECT '' "
              + "UNION ALL SELECT CAST(NULL AS STRING)");
      ZFrame<Frame, Row, DuckZColumn> duckSplitInput = ZFrameDuckAdapter.wrap(job.sql(
          "SELECT 'alpha|beta' AS words UNION ALL SELECT 'left||right' "
              + "UNION ALL SELECT 'trailing|' UNION ALL SELECT '' "
              + "UNION ALL SELECT CAST(NULL AS VARCHAR)"));
      Dataset<org.apache.spark.sql.Row> sparkSplit = sparkSplitInput.select(
          functions.split(functions.col("words"), "\\|").alias("parts"));
      ZFrame<Frame, Row, DuckZColumn> duckSplit = duckSplitInput.split("words", "\\|", "parts");
      assertEquals(List.of(sparkSplit.columns()), List.of(duckSplit.columns()));
      assertEquals(indexSparkArrays(sparkSplit.collectAsList(), "parts"),
          indexDuckArrays(duckSplit.collectAsList(), "parts"));

      Dataset<org.apache.spark.sql.Row> sparkExplodeInput = spark.sql(
          "SELECT ARRAY(1, 2) AS items UNION ALL SELECT ARRAY(2, 2) "
              + "UNION ALL SELECT CAST(ARRAY() AS ARRAY<INT>) UNION ALL SELECT CAST(NULL AS ARRAY<INT>)");
      ZFrame<Frame, Row, DuckZColumn> duckExplodeInput = ZFrameDuckAdapter.wrap(job.sql(
          "SELECT [1, 2] AS items UNION ALL SELECT [2, 2] "
              + "UNION ALL SELECT CAST([] AS INTEGER[]) UNION ALL SELECT CAST(NULL AS INTEGER[])"));
      Dataset<org.apache.spark.sql.Row> sparkExploded = sparkExplodeInput.select(
          functions.explode(functions.col("items")).alias("item"));
      ZFrame<Frame, Row, DuckZColumn> duckExploded = duckExplodeInput.explode("items", "item");
      assertFrameContents(sparkExploded, duckExploded);

      assertThrows(java.util.NoSuchElementException.class, () -> spark.sql(
          "SELECT CAST(NULL AS INTEGER) AS value WHERE FALSE").head());
      assertThrows(java.util.NoSuchElementException.class, () -> ZFrameDuckAdapter.wrap(job.sql(
          "SELECT CAST(NULL AS INTEGER) AS value WHERE FALSE")).head());
      assertEquals(List.of(sparkRowsForAccess.columns()), List.of(duckRowsForAccess.fieldNames()));
      assertEquals(List.of(sparkRowsForAccess.columns()), List.of(duckRowsForAccess.columns()));

      assertExpressionParity(sparkExprInput.filter(functions.col("id").equalTo(2)),
          sparkExprFrame.filter(sparkExprFrame.equalTo("id", 2)));
      assertExpressionParity(sparkExprInput.filter(functions.col("id").equalTo(2.0d)),
          sparkExprFrame.filter(sparkExprFrame.equalTo("id", 2.0d)));
      assertExpressionParity(sparkExprInput.filter(functions.col("id").equalTo(functions.col("z_id"))),
          sparkExprFrame.filter(sparkExprFrame.equalTo(sparkExprFrame.col("id"), sparkExprFrame.col("z_id"))));
      assertExpressionParity(sparkExprInput.filter(functions.col("text_value").equalTo("beta")),
          sparkExprFrame.filter(sparkExprFrame.equalTo("text_value", "beta")));
      assertExpressionParity(sparkExprInput.filter(functions.col("id").gt(1.5d)),
          sparkExprFrame.filter(sparkExprFrame.gt("id", 1.5d)));
      assertExpressionParity(sparkExprInput.filter(functions.col("id").gt(functions.col("z_id"))),
          sparkExprFrame.filter(sparkExprFrame.gt("id")));
      assertExpressionParity(sparkExprInput.filter(functions.col("id").gt(functions.col("z_id"))),
          sparkExprFrame.filter(sparkExprFrame.gt(sparkExprFrame.col("id"), sparkExprFrame.col("z_id"))));
      Dataset<org.apache.spark.sql.Row> sparkExprRight = spark.sql(
          "SELECT 0 AS z_id UNION ALL SELECT 1 UNION ALL SELECT 2");
      Frame duckExprRight = job.sql("SELECT 0 AS z_id UNION ALL SELECT 1 UNION ALL SELECT 2");
      Dataset<org.apache.spark.sql.Row> sparkCrossRelationGt = sparkExprInput.join(sparkExprRight,
          sparkExprInput.col("id").gt(sparkExprRight.col("z_id")));
      ZFrame<Frame, Row, DuckZColumn> duckCrossRelationGt = sparkExprFrame.join(
          ZFrameDuckAdapter.wrap(duckExprRight), sparkExprFrame.gt(ZFrameDuckAdapter.wrap(duckExprRight), "id"),
          ZFrame.INNER_JOIN);
      assertEquals(List.of(sparkCrossRelationGt.columns()), List.of(duckCrossRelationGt.columns()));
      assertEquals(indexSparkRows(sparkCrossRelationGt.collectAsList()), indexDuckRows(duckCrossRelationGt.collectAsList()));
      assertExpressionParity(sparkExprInput.filter(functions.col("text_value").notEqual("beta")),
          sparkExprFrame.filter(sparkExprFrame.notEqual("text_value", "beta")));
      assertExpressionParity(sparkExprInput.filter(functions.col("id").notEqual(2)),
          sparkExprFrame.filter(sparkExprFrame.notEqual("id", 2)));
      assertExpressionParity(sparkExprInput.filter(functions.col("text_value").notEqual(functions.col("z_text_value"))),
          sparkExprFrame.filter(sparkExprFrame.notEqual("text_value")));
      assertExpressionParity(sparkExprInput.filter(functions.col("id").gt(functions.col("z_id"))
              .and(functions.col("metric").gt(2.0d))),
          sparkExprFrame.filter(sparkExprFrame.and(sparkExprFrame.gt(sparkExprFrame.col("id"), sparkExprFrame.col("z_id")),
              sparkExprFrame.gt("metric", 2.0d))));
      assertExpressionParity(sparkExprInput.filter(functions.not(functions.col("id").equalTo(2))
              .or(functions.col("nullable_value").isNotNull())),
          sparkExprFrame.filter(sparkExprFrame.or(sparkExprFrame.not(sparkExprFrame.equalTo("id", 2)),
              sparkExprFrame.isNotNull(sparkExprFrame.col("nullable_value")))));
      assertExpressionParity(sparkExprInput.filter(functions.col("nullable_value").isNotNull()),
          sparkExprFrame.filter(sparkExprFrame.isNotNull(sparkExprFrame.col("nullable_value"))));
      assertEquals(sparkExprInput.select(functions.concat(functions.col("text_value"),
              functions.col("nullable_value")).alias("value")).collectAsList().stream()
              .map(row -> row.get(0)).toList(),
          sparkExprFrame.select(sparkExprFrame.concat(sparkExprFrame.col("text_value"),
              sparkExprFrame.col("nullable_value")).as("value")).collectAsList().stream()
              .map(row -> row.values().get(0)).toList());
      assertEquals(sparkExprInput.select(functions.substring(functions.col("text_value"), 2, 3).alias("value"))
              .collectAsList().stream().map(row -> row.get(0)).toList(),
          sparkExprFrame.select(sparkExprFrame.substr(sparkExprFrame.col("text_value"), 2, 3).as("value"))
              .collectAsList().stream().map(row -> row.values().get(0)).toList());

      Dataset<org.apache.spark.sql.Row> sparkExpressionColumn = sparkNullable.withColumn(
          "is_x", functions.lit(functions.col("group_key").equalTo("x")));
      ZFrame<Frame, Row, DuckZColumn> duckExpressionColumn = nullableFrame.withColumn(
          "is_x", nullableFrame.equalTo("group_key", "x"));
      assertEquals(List.of(sparkExpressionColumn.columns()), List.of(duckExpressionColumn.columns()));
      assertEquals(indexSparkRows(sparkExpressionColumn.collectAsList()),
          indexDuckRows(duckExpressionColumn.collectAsList()));

      var sparkColumnNames = JavaConverters.asScalaIteratorConverter(
          List.of("group_key", "value_key").iterator()).asScala().toSeq();
      var sparkColumnValues = JavaConverters.asScalaIteratorConverter(
          List.of(functions.col("value_key"), functions.col("group_key")).iterator()).asScala().toSeq();
      Dataset<org.apache.spark.sql.Row> sparkSimultaneousColumns = sparkNullable.withColumns(
          sparkColumnNames, sparkColumnValues);
      ZFrame<Frame, Row, DuckZColumn> duckSimultaneousColumns = nullableFrame.withColumns(
          new String[] {"group_key", "value_key"},
          new DuckZColumn[] {nullableFrame.col("value_key"), nullableFrame.col("group_key")});
      assertEquals(List.of(sparkSimultaneousColumns.columns()), List.of(duckSimultaneousColumns.columns()));
      assertEquals(indexSparkRows(sparkSimultaneousColumns.collectAsList()),
          indexDuckRows(duckSimultaneousColumns.collectAsList()));

      Dataset<org.apache.spark.sql.Row> sparkOrderedSelect = sparkNullable.select("value_key", "group_key");
      ZFrame<Frame, Row, DuckZColumn> duckOrderedSelect = nullableFrame.select("value_key", "group_key");
      assertEquals(List.of(sparkOrderedSelect.columns()), List.of(duckOrderedSelect.columns()));
      assertEquals(indexSparkRows(sparkOrderedSelect.collectAsList()), indexDuckRows(duckOrderedSelect.collectAsList()));

      Dataset<org.apache.spark.sql.Row> sparkColumnVarargs = sparkNullable.select(
          functions.col("group_key").alias("group_label"), functions.col("value_key"),
          functions.col("group_key").equalTo("x").alias("is_x"));
      ZFrame<Frame, Row, DuckZColumn> duckColumnVarargs = nullableFrame.select(
          nullableFrame.col("group_key").as("group_label"), nullableFrame.col("value_key"),
          nullableFrame.equalTo("group_key", "x").as("is_x"));
      assertEquals(List.of(sparkColumnVarargs.columns()), List.of(duckColumnVarargs.columns()));
      assertEquals(indexSparkRows(sparkColumnVarargs.collectAsList()), indexDuckRows(duckColumnVarargs.collectAsList()));

      Dataset<org.apache.spark.sql.Row> sparkColumnList = sparkNullable.select(
          functions.col("value_key"), functions.col("group_key").alias("group_label"),
          functions.col("group_key").equalTo("x").alias("is_x"));
      ZFrame<Frame, Row, DuckZColumn> duckColumnList = nullableFrame.select(List.of(
          nullableFrame.col("value_key"), nullableFrame.col("group_key").as("group_label"),
          nullableFrame.equalTo("group_key", "x").as("is_x")));
      assertEquals(List.of(sparkColumnList.columns()), List.of(duckColumnList.columns()));
      assertEquals(indexSparkRows(sparkColumnList.collectAsList()), indexDuckRows(duckColumnList.collectAsList()));

      Dataset<org.apache.spark.sql.Row> sparkExprSelect = sparkNullable.selectExpr(
          "group_key AS renamed_group", "upper(value_key) AS upper_value");
      ZFrame<Frame, Row, DuckZColumn> duckExprSelect = nullableFrame.selectExpr(
          "group_key AS renamed_group", "upper(value_key) AS upper_value");
      assertEquals(List.of(sparkExprSelect.columns()), List.of(duckExprSelect.columns()));
      assertEquals(indexSparkRows(sparkExprSelect.collectAsList()), indexDuckRows(duckExprSelect.collectAsList()));
      Dataset<org.apache.spark.sql.Row> sparkMixedExprSelect = sparkNullable.selectExpr(
          "group_key AS renamed_group", "upper(value_key)");
      ZFrame<Frame, Row, DuckZColumn> duckMixedExprSelect = nullableFrame.selectExpr(
          "group_key AS renamed_group", "upper(value_key)");
      assertEquals(List.of(sparkMixedExprSelect.columns()), List.of(duckMixedExprSelect.columns()));
      assertEquals(indexSparkRows(sparkMixedExprSelect.collectAsList()),
          indexDuckRows(duckMixedExprSelect.collectAsList()));
      Dataset<org.apache.spark.sql.Row> sparkStarExprSelect = sparkNullable.selectExpr("*", "upper(value_key) AS upper_value");
      ZFrame<Frame, Row, DuckZColumn> duckStarExprSelect = nullableFrame.selectExpr("*", "upper(value_key) AS upper_value");
      assertEquals(List.of(sparkStarExprSelect.columns()), List.of(duckStarExprSelect.columns()));
      assertEquals(indexSparkRows(sparkStarExprSelect.collectAsList()), indexDuckRows(duckStarExprSelect.collectAsList()));

      Dataset<org.apache.spark.sql.Row> sparkDropColumn = sparkNullable.drop(functions.col("value_key"));
      ZFrame<Frame, Row, DuckZColumn> duckDropColumn = nullableFrame.drop(nullableFrame.col("value_key"));
      assertEquals(List.of(sparkDropColumn.columns()), List.of(duckDropColumn.columns()));
      assertEquals(indexSparkRows(sparkDropColumn.collectAsList()), indexDuckRows(duckDropColumn.collectAsList()));
      Dataset<org.apache.spark.sql.Row> sparkDropColumns = sparkExprInput.drop("nullable_value", "z_text_value");
      ZFrame<Frame, Row, DuckZColumn> duckDropColumns = sparkExprFrame.drop("nullable_value", "z_text_value");
      assertEquals(List.of(sparkDropColumns.columns()), List.of(duckDropColumns.columns()));
      assertEquals(indexSparkRows(sparkDropColumns.collectAsList()), indexDuckRows(duckDropColumns.collectAsList()));
      Dataset<org.apache.spark.sql.Row> sparkDropVarargs = sparkNullable.drop(
          new String[] {"value_key", "missing_column"});
      ZFrame<Frame, Row, DuckZColumn> duckDropVarargs = nullableFrame.drop(
          new String[] {"value_key", "missing_column"});
      assertEquals(List.of(sparkDropVarargs.columns()), List.of(duckDropVarargs.columns()));
      assertEquals(indexSparkRows(sparkDropVarargs.collectAsList()), indexDuckRows(duckDropVarargs.collectAsList()));
      Dataset<org.apache.spark.sql.Row> sparkDropMissing = sparkNullable.drop("missing_column");
      ZFrame<Frame, Row, DuckZColumn> duckDropMissing = nullableFrame.drop("missing_column");
      assertEquals(List.of(sparkDropMissing.columns()), List.of(duckDropMissing.columns()));
      assertEquals(indexSparkRows(sparkDropMissing.collectAsList()), indexDuckRows(duckDropMissing.collectAsList()));

      Dataset<org.apache.spark.sql.Row> sparkRenamed = sparkNullable.withColumnRenamed("group_key", "renamed_group");
      ZFrame<Frame, Row, DuckZColumn> duckRenamed = nullableFrame.withColumnRenamed("group_key", "renamed_group");
      assertEquals(List.of(sparkRenamed.columns()), List.of(duckRenamed.columns()));
      assertEquals(indexSparkRows(sparkRenamed.collectAsList()), indexDuckRows(duckRenamed.collectAsList()));
      Dataset<org.apache.spark.sql.Row> sparkRenameMissing = sparkNullable.withColumnRenamed("missing_column", "renamed");
      ZFrame<Frame, Row, DuckZColumn> duckRenameMissing = nullableFrame.withColumnRenamed("missing_column", "renamed");
      assertEquals(List.of(sparkRenameMissing.columns()), List.of(duckRenameMissing.columns()));
      assertEquals(indexSparkRows(sparkRenameMissing.collectAsList()), indexDuckRows(duckRenameMissing.collectAsList()));
      Dataset<org.apache.spark.sql.Row> sparkRenameCollision = sparkNullable.withColumnRenamed("group_key", "value_key");
      ZFrame<Frame, Row, DuckZColumn> duckRenameCollision = nullableFrame.withColumnRenamed("group_key", "value_key");
      assertEquals(List.of(sparkRenameCollision.columns()), List.of(duckRenameCollision.columns()));
      assertEquals(indexSparkRows(sparkRenameCollision.collectAsList()), indexDuckRows(duckRenameCollision.collectAsList()));

      Dataset<org.apache.spark.sql.Row> sparkToDfPair = sparkNullable.toDF("renamed_group", "renamed_value");
      ZFrame<Frame, Row, DuckZColumn> duckToDfPair = nullableFrame.toDF("renamed_group", "renamed_value");
      assertEquals(List.of(sparkToDfPair.columns()), List.of(duckToDfPair.columns()));
      assertEquals(indexSparkRows(sparkToDfPair.collectAsList()), indexDuckRows(duckToDfPair.collectAsList()));

      Dataset<org.apache.spark.sql.Row> sparkToDfDuplicates = sparkNullable.toDF("same_name", "same_name");
      ZFrame<Frame, Row, DuckZColumn> duckToDfDuplicates = nullableFrame.toDF(
          new String[] {"same_name", "same_name"});
      assertEquals(List.of(sparkToDfDuplicates.columns()), List.of(duckToDfDuplicates.columns()));
      assertEquals(indexSparkRows(sparkToDfDuplicates.collectAsList()), indexDuckRows(duckToDfDuplicates.collectAsList()));
      assertEquals(List.of("same_name", "same_name"), List.of(duckToDfDuplicates.columns()));
      assertEquals(List.of("same_name", "same_name"), Arrays.stream(duckToDfDuplicates.fields())
          .map(zingg.common.client.FieldData::getName).toList());
      assertEquals(sparkToDfDuplicates.schema().fieldIndex("same_name"), duckToDfDuplicates.fieldIndex("same_name"));
      assertThrows(RuntimeException.class, () -> sparkNullable.toDF("only_one_name"));
      assertThrows(IllegalArgumentException.class, () -> nullableFrame.toDF(new String[] {"only_one_name"}));

      Dataset<org.apache.spark.sql.Row> sparkDistinctCounts = sparkNullable.groupBy("group_key")
          .agg(functions.countDistinct("value_key").alias("distinct_count"));
      ZFrame<Frame, Row, DuckZColumn> duckDistinctCounts = nullableFrame.countDistinct(
          "group_key", "value_key", "distinct_count");
      assertEquals(indexBy(sparkDistinctCounts.collectAsList(), "group_key", "distinct_count"),
          indexDuckBy(duckDistinctCounts.collectAsList(), "group_key", "distinct_count"));

      Dataset<org.apache.spark.sql.Row> sparkNullRows = sparkNullable.filter(functions.col("value_key").isNull());
      ZFrame<Frame, Row, DuckZColumn> duckNullRows = nullableFrame.filterNullCond("value_key");
      assertEquals(List.of(sparkNullRows.columns()), List.of(duckNullRows.columns()));
      assertEquals(indexSparkPairs(sparkNullRows.collectAsList(), "group_key", "value_key"),
          indexDuckPairs(duckNullRows.collectAsList(), "group_key", "value_key"));

      Dataset<org.apache.spark.sql.Row> sparkNonNullRows = sparkNullable.filter(functions.col("value_key").isNotNull());
      ZFrame<Frame, Row, DuckZColumn> duckNonNullRows = nullableFrame.filterNotNullCond("value_key");
      assertEquals(List.of(sparkNonNullRows.columns()), List.of(duckNonNullRows.columns()));
      assertEquals(indexSparkPairs(sparkNonNullRows.collectAsList(), "group_key", "value_key"),
          indexDuckPairs(duckNonNullRows.collectAsList(), "group_key", "value_key"));

      Dataset<org.apache.spark.sql.Row> sparkMetric = spark.sql(
          "SELECT 1.5 AS metric UNION ALL SELECT CAST(NULL AS DOUBLE) UNION ALL SELECT 2.5");
      Frame duckMetric = job.sql(
          "SELECT 1.5 AS metric UNION ALL SELECT CAST(NULL AS DOUBLE) UNION ALL SELECT 2.5");
      ZFrame<Frame, Row, DuckZColumn> metricFrame = ZFrameDuckAdapter.wrap(duckMetric);
      double sparkSum = sparkMetric.agg(functions.sum("metric").cast("double")).collectAsList().get(0).getDouble(0);
      assertEquals(sparkSum, metricFrame.aggSum("metric"), 0.0);
      assertEquals(sparkMetric.agg(functions.max("metric")).collectAsList().get(0).get(0),
          metricFrame.getMaxVal("metric"));

      Dataset<org.apache.spark.sql.Row> sparkScoreGroups = spark.sql(
          "SELECT 'a' AS z_id, 1.0D AS z_score UNION ALL SELECT 'a', 3.0D "
              + "UNION ALL SELECT 'b', CAST(NULL AS DOUBLE) "
              + "UNION ALL SELECT CAST(NULL AS STRING), 4.0D "
              + "UNION ALL SELECT CAST(NULL AS STRING), 2.0D "
              + "UNION ALL SELECT 'c', CAST(NULL AS DOUBLE)");
      ZFrame<Frame, Row, DuckZColumn> duckScoreGroups = ZFrameDuckAdapter.wrap(job.sql(
          "SELECT 'a' AS z_id, 1.0 AS z_score UNION ALL SELECT 'a', 3.0 "
              + "UNION ALL SELECT 'b', CAST(NULL AS DOUBLE) "
              + "UNION ALL SELECT CAST(NULL AS VARCHAR), 4.0 "
              + "UNION ALL SELECT CAST(NULL AS VARCHAR), 2.0 "
              + "UNION ALL SELECT 'c', CAST(NULL AS DOUBLE)"));
      Dataset<org.apache.spark.sql.Row> sparkMinMax = sparkScoreGroups.groupBy("z_id").agg(
          functions.min("z_score").alias("z_minScore"), functions.max("z_score").alias("z_maxScore"));
      ZFrame<Frame, Row, DuckZColumn> duckMinMax = duckScoreGroups.groupByMinMaxScore(
          duckScoreGroups.col("z_id"));
      assertEquals(List.of(sparkMinMax.columns()), List.of(duckMinMax.columns()));
      assertEquals(indexSparkRows(sparkMinMax.collectAsList()), indexDuckRows(duckMinMax.collectAsList()));

      Dataset<org.apache.spark.sql.Row> sparkAllNullMetric = spark.sql(
          "SELECT CAST(NULL AS DOUBLE) AS metric UNION ALL SELECT CAST(NULL AS DOUBLE)");
      Frame duckAllNullMetric = job.sql(
          "SELECT CAST(NULL AS DOUBLE) AS metric UNION ALL SELECT CAST(NULL AS DOUBLE)");
      ZFrame<Frame, Row, DuckZColumn> allNullMetricFrame = ZFrameDuckAdapter.wrap(duckAllNullMetric);
      assertThrows(SparkException.class, () -> sparkAllNullMetric.agg(
          functions.sum("metric").cast("double")).collectAsList().get(0).getDouble(0));
      assertThrows(DuckException.class, () -> allNullMetricFrame.aggSum("metric"));
      assertNull(sparkAllNullMetric.agg(functions.max("metric")).collectAsList().get(0).get(0));
      assertNull(allNullMetricFrame.getMaxVal("metric"));

      Dataset<org.apache.spark.sql.Row> sparkEmptyMetric = spark.sql(
          "SELECT CAST(NULL AS DOUBLE) AS metric WHERE FALSE");
      Frame duckEmptyMetric = job.sql("SELECT CAST(NULL AS DOUBLE) AS metric WHERE FALSE");
      ZFrame<Frame, Row, DuckZColumn> emptyMetricFrame = ZFrameDuckAdapter.wrap(duckEmptyMetric);
      assertThrows(SparkException.class, () -> sparkEmptyMetric.agg(
          functions.sum("metric").cast("double")).collectAsList().get(0).getDouble(0));
      assertThrows(DuckException.class, () -> emptyMetricFrame.aggSum("metric"));
      assertNull(sparkEmptyMetric.agg(functions.max("metric")).collectAsList().get(0).get(0));
      assertNull(emptyMetricFrame.getMaxVal("metric"));

      Dataset<org.apache.spark.sql.Row> sparkSortInput = spark.sql(
          "SELECT CAST(NULL AS INT) AS sort_key, 'n1' AS sort_payload "
              + "UNION ALL SELECT 3, 'c' UNION ALL SELECT 1, 'a' "
              + "UNION ALL SELECT CAST(NULL AS INT), 'n2' UNION ALL SELECT 2, 'b'");
      Frame duckSortInput = job.sql(
          "SELECT CAST(NULL AS BIGINT) AS sort_key, 'n1' AS sort_payload "
              + "UNION ALL SELECT 3, 'c' UNION ALL SELECT 1, 'a' "
              + "UNION ALL SELECT CAST(NULL AS BIGINT), 'n2' UNION ALL SELECT 2, 'b'");
      ZFrame<Frame, Row, DuckZColumn> sortFrame = ZFrameDuckAdapter.wrap(duckSortInput);
      List<Long> ascendingExpected = java.util.Arrays.asList(null, null, 1L, 2L, 3L);
      List<Long> descendingExpected = java.util.Arrays.asList(3L, 2L, 1L, null, null);
      assertEquals(ascendingExpected, sortKeysSpark(sparkSortInput.sort(functions.asc("sort_key")).collectAsList()));
      assertEquals(ascendingExpected, sortKeysDuck(sortFrame.sortAscending("sort_key").collectAsList()));
      assertEquals(ascendingExpected, sortKeysDuck(sortFrame.orderBy("sort_key").collectAsList()));
      assertEquals(descendingExpected, sortKeysSpark(sparkSortInput.sort(functions.desc("sort_key")).collectAsList()));
      assertEquals(descendingExpected, sortKeysDuck(sortFrame.sortDescending("sort_key").collectAsList()));
      assertEquals(ascendingExpected.subList(0, 3), sortKeysSpark(
          sparkSortInput.sort(functions.asc("sort_key")).limit(3).collectAsList()));
      assertEquals(ascendingExpected.subList(0, 3), sortKeysDuck(
          sortFrame.sortAscending("sort_key").limit(3).collectAsList()));

      Dataset<org.apache.spark.sql.Row> sparkLeft = spark.sql(
          "SELECT 'a' AS left_key, 'L1' AS payload "
              + "UNION ALL SELECT 'a', 'L2' "
              + "UNION ALL SELECT 'b', 'L3' "
              + "UNION ALL SELECT CAST(NULL AS STRING), 'LN'");
      Dataset<org.apache.spark.sql.Row> sparkRight = spark.sql(
          "SELECT 'a' AS lookup_key UNION ALL SELECT 'a' UNION ALL SELECT CAST(NULL AS STRING)");
      Frame duckLeft = job.sql(
          "SELECT 'a' AS left_key, 'L1' AS payload "
              + "UNION ALL SELECT 'a', 'L2' "
              + "UNION ALL SELECT 'b', 'L3' "
              + "UNION ALL SELECT CAST(NULL AS VARCHAR), 'LN'");
      Frame duckRight = job.sql(
          "SELECT 'a' AS lookup_key UNION ALL SELECT 'a' UNION ALL SELECT CAST(NULL AS VARCHAR)");
      ZFrame<Frame, Row, DuckZColumn> leftFrame = ZFrameDuckAdapter.wrap(duckLeft);
      ZFrame<Frame, Row, DuckZColumn> rightFrame = ZFrameDuckAdapter.wrap(duckRight);

      Dataset<org.apache.spark.sql.Row> sparkFiltered = sparkLeft.join(
          sparkRight.select(functions.col("lookup_key").alias("left_key")), "left_key");
      ZFrame<Frame, Row, DuckZColumn> duckFiltered = leftFrame.filterInCond(
          "left_key", rightFrame, "lookup_key");
      assertEquals(List.of(sparkFiltered.columns()), List.of(duckFiltered.columns()));
      assertEquals(indexSparkPairs(sparkFiltered.collectAsList()), indexDuckPairs(duckFiltered.collectAsList()));
      assertEquals(4, duckFiltered.count()); // two matching right rows multiply each matching left row

      ZFrame<Frame, Row, DuckZColumn> projectedRight = rightFrame.select(
          List.of(rightFrame.col("lookup_key").as("left_key")));
      ZFrame<Frame, Row, DuckZColumn> duckUsingJoin = leftFrame.joinOnCol(projectedRight, "left_key");
      assertEquals(List.of(sparkFiltered.columns()), List.of(duckUsingJoin.columns()));
      assertEquals(indexSparkPairs(sparkFiltered.collectAsList()), indexDuckPairs(duckUsingJoin.collectAsList()));

      Dataset<org.apache.spark.sql.Row> sparkProjectedRight = sparkRight.select(
          functions.col("lookup_key").alias("left_key"));
      Dataset<org.apache.spark.sql.Row> sparkExpressionJoin = sparkLeft.join(sparkProjectedRight,
          sparkLeft.col("left_key").equalTo(sparkProjectedRight.col("left_key")));
      ZFrame<Frame, Row, DuckZColumn> duckExpressionJoin = leftFrame.joinOnCol(projectedRight,
          leftFrame.equalTo(leftFrame.col("left_key"), projectedRight.col("left_key")));
      assertEquals(List.of(sparkExpressionJoin.columns()), List.of(duckExpressionJoin.columns()));
      assertEquals(indexSparkRows(sparkExpressionJoin.collectAsList()), indexDuckRows(duckExpressionJoin.collectAsList()));

      Dataset<org.apache.spark.sql.Row> sparkExpressionLeftJoin = sparkLeft.join(sparkProjectedRight,
          sparkLeft.col("left_key").equalTo(sparkProjectedRight.col("left_key")), "left_outer");
      ZFrame<Frame, Row, DuckZColumn> duckExpressionLeftJoin = leftFrame.join(projectedRight,
          leftFrame.equalTo(leftFrame.col("left_key"), projectedRight.col("left_key")), "left_outer");
      assertEquals(List.of(sparkExpressionLeftJoin.columns()), List.of(duckExpressionLeftJoin.columns()));
      assertEquals(indexSparkRows(sparkExpressionLeftJoin.collectAsList()),
          indexDuckRows(duckExpressionLeftJoin.collectAsList()));

      Dataset<org.apache.spark.sql.Row> sparkPairLeft = spark.sql(
          "SELECT 'a' AS join_key, 'x' AS group_key, 'L1' AS payload "
              + "UNION ALL SELECT 'a', 'x', 'L2' UNION ALL SELECT 'b', 'y', 'L3' "
              + "UNION ALL SELECT CAST(NULL AS STRING), 'z', 'LN'");
      Dataset<org.apache.spark.sql.Row> sparkPairRight = spark.sql(
          "SELECT 'a' AS join_key, 'x' AS group_key, 'R1' AS right_payload "
              + "UNION ALL SELECT 'a', 'x', 'R2' UNION ALL SELECT 'c', 'z', 'R3' "
              + "UNION ALL SELECT CAST(NULL AS STRING), 'z', 'RN'");
      Frame duckPairLeft = job.sql(
          "SELECT 'a' AS join_key, 'x' AS group_key, 'L1' AS payload "
              + "UNION ALL SELECT 'a', 'x', 'L2' UNION ALL SELECT 'b', 'y', 'L3' "
              + "UNION ALL SELECT CAST(NULL AS VARCHAR), 'z', 'LN'");
      Frame duckPairRight = job.sql(
          "SELECT 'a' AS join_key, 'x' AS group_key, 'R1' AS right_payload "
              + "UNION ALL SELECT 'a', 'x', 'R2' UNION ALL SELECT 'c', 'z', 'R3' "
              + "UNION ALL SELECT CAST(NULL AS VARCHAR), 'z', 'RN'");
      ZFrame<Frame, Row, DuckZColumn> pairLeft = ZFrameDuckAdapter.wrap(duckPairLeft);
      ZFrame<Frame, Row, DuckZColumn> pairRight = ZFrameDuckAdapter.wrap(duckPairRight);
      Dataset<org.apache.spark.sql.Row> sparkTwoKeyJoin = sparkPairLeft.join(sparkPairRight,
          sparkPairLeft.col("join_key").equalTo(sparkPairRight.col("join_key"))
              .and(sparkPairLeft.col("group_key").equalTo(sparkPairRight.col("group_key"))));
      ZFrame<Frame, Row, DuckZColumn> duckTwoKeyJoin = pairLeft.join(
          pairRight, "join_key", "group_key");
      assertEquals(List.of(sparkTwoKeyJoin.columns()), List.of(duckTwoKeyJoin.columns()));
      assertEquals(indexSparkRows(sparkTwoKeyJoin.collectAsList()), indexDuckRows(duckTwoKeyJoin.collectAsList()));

      Dataset<org.apache.spark.sql.Row> sparkTwoKeyLeftJoin = sparkPairLeft.join(sparkPairRight,
          sparkPairLeft.col("join_key").equalTo(sparkPairRight.col("join_key"))
              .and(sparkPairLeft.col("group_key").equalTo(sparkPairRight.col("group_key"))), "left_outer");
      ZFrame<Frame, Row, DuckZColumn> duckTwoKeyLeftJoin = pairLeft.join(
          pairRight, "join_key", "group_key", "left");
      assertEquals(List.of(sparkTwoKeyLeftJoin.columns()), List.of(duckTwoKeyLeftJoin.columns()));
      assertEquals(indexSparkRows(sparkTwoKeyLeftJoin.collectAsList()), indexDuckRows(duckTwoKeyLeftJoin.collectAsList()));

      Dataset<org.apache.spark.sql.Row> sparkLeftOuter = sparkPairLeft.join(sparkPairRight,
          sparkPairLeft.col("join_key").equalTo(sparkPairRight.col("join_key")), "left_outer");
      ZFrame<Frame, Row, DuckZColumn> duckLeftOuter = pairLeft.join(
          pairRight, "join_key", false, "left");
      assertEquals(List.of(sparkLeftOuter.columns()), List.of(duckLeftOuter.columns()));
      assertEquals(indexSparkRows(sparkLeftOuter.collectAsList()), indexDuckRows(duckLeftOuter.collectAsList()));

      Dataset<org.apache.spark.sql.Row> sparkPrefixedRight = spark.sql(
          "SELECT 'a' AS z_join_key, 'PR1' AS right_payload "
              + "UNION ALL SELECT 'a', 'PR2' UNION ALL SELECT 'c', 'PR3' "
              + "UNION ALL SELECT CAST(NULL AS STRING), 'PRN'");
      Frame duckPrefixedRight = job.sql(
          "SELECT 'a' AS z_join_key, 'PR1' AS right_payload "
              + "UNION ALL SELECT 'a', 'PR2' UNION ALL SELECT 'c', 'PR3' "
              + "UNION ALL SELECT CAST(NULL AS VARCHAR), 'PRN'");
      Dataset<org.apache.spark.sql.Row> sparkImplicitPrefixJoin = sparkPairLeft.join(sparkPrefixedRight,
          sparkPairLeft.col("join_key").equalTo(sparkPrefixedRight.col("z_join_key")));
      ZFrame<Frame, Row, DuckZColumn> duckImplicitPrefixJoin = pairLeft.join(
          ZFrameDuckAdapter.wrap(duckPrefixedRight), "join_key");
      assertEquals(List.of(sparkImplicitPrefixJoin.columns()), List.of(duckImplicitPrefixJoin.columns()));
      assertEquals(indexSparkRows(sparkImplicitPrefixJoin.collectAsList()),
          indexDuckRows(duckImplicitPrefixJoin.collectAsList()));

      Dataset<org.apache.spark.sql.Row> sparkPrefixedJoin = sparkPairLeft.join(sparkPrefixedRight,
          sparkPairLeft.col("join_key").equalTo(sparkPrefixedRight.col("z_join_key")), "left_outer");
      ZFrame<Frame, Row, DuckZColumn> duckPrefixedJoin = pairLeft.join(
          ZFrameDuckAdapter.wrap(duckPrefixedRight), "join_key", true, "left");
      assertEquals(List.of(sparkPrefixedJoin.columns()), List.of(duckPrefixedJoin.columns()));
      assertEquals(indexSparkRows(sparkPrefixedJoin.collectAsList()), indexDuckRows(duckPrefixedJoin.collectAsList()));

      Dataset<org.apache.spark.sql.Row> sparkRightOuter = sparkPairLeft.join(sparkPairRight,
          sparkPairLeft.col("join_key").equalTo(sparkPairRight.col("join_key")), "right_outer");
      ZFrame<Frame, Row, DuckZColumn> duckRightOuter = pairLeft.joinRight(pairRight, "join_key");
      assertEquals(List.of(sparkRightOuter.columns()), List.of(duckRightOuter.columns()));
      assertEquals(indexSparkRows(sparkRightOuter.collectAsList()), indexDuckRows(duckRightOuter.collectAsList()));
    } finally {
      spark.stop();
    }
  }

  private static Map<Object, Long> indexBy(List<org.apache.spark.sql.Row> rows, String key, String value) {
    var result = new HashMap<Object, Long>();
    for (var row : rows) result.put(row.getAs(key), ((Number) row.getAs(value)).longValue());
    return result;
  }

  private static String captureStdout(Runnable action) throws Exception {
    PrintStream original = System.out;
    ByteArrayOutputStream captured = new ByteArrayOutputStream();
    try (PrintStream replacement = new PrintStream(captured, true, StandardCharsets.UTF_8)) {
      System.setOut(replacement);
      action.run();
    } finally {
      System.setOut(original);
    }
    return captured.toString(StandardCharsets.UTF_8);
  }

  private static String normalizeConsoleOutput(String output) {
    return output.replace("\r\n", "\n").stripTrailing();
  }

  private static void assertExpressionParity(
      Dataset<org.apache.spark.sql.Row> sparkFrame, ZFrame<Frame, Row, DuckZColumn> duckFrame) {
    assertEquals(List.of(sparkFrame.columns()), List.of(duckFrame.columns()));
    assertEquals(indexSparkRows(sparkFrame.collectAsList()), indexDuckRows(duckFrame.collectAsList()));
  }

  private static void assertReplacementSampleShape(
      Dataset<org.apache.spark.sql.Row> sample, String engine) {
    assertReplacementSampleIds(sample.collectAsList().stream()
        .map(row -> ((Number) row.get(0)).longValue()).toList(), engine);
  }

  private static boolean isValidNoReplacementSample(ZFrame<Frame, Row, DuckZColumn> sample) {
    List<Row> rows = sample.collectAsList();
    long distinctIds = rows.stream().map(row -> row.get("sample_id")).distinct().count();
    return rows.size() >= 4_500 && rows.size() <= 5_500 && distinctIds == rows.size();
  }

  private static void assertReplacementSampleShape(
      ZFrame<Frame, Row, DuckZColumn> sample, String engine) {
    assertReplacementSampleIds(sample.collectAsList().stream()
        .map(row -> ((Number) row.get("sample_id")).longValue()).toList(), engine);
  }

  private static void assertReplacementSampleIds(List<Long> ids, String engine) {
    assertTrue(ids.size() >= 4_200 && ids.size() <= 5_800,
        engine + " Poisson replacement sample count should be consistent with lambda=0.25");
    Map<Long, Long> multiplicities = new HashMap<>();
    for (long id : ids) multiplicities.merge(id, 1L, Long::sum);
    assertTrue(multiplicities.size() >= 4_100 && multiplicities.size() <= 4_750,
        engine + " count of represented source IDs should follow Poisson(0.25) occupancy");
    long repeatedIds = multiplicities.values().stream().filter(count -> count > 1).count();
    assertTrue(repeatedIds >= 350 && repeatedIds <= 750,
        engine + " repeated source IDs should follow Poisson(0.25) multiplicities");
    assertTrue(ids.size() > multiplicities.size(),
        engine + " replacement sample should contain duplicate source IDs");
    assertTrue(ids.stream().allMatch(id -> id >= 0 && id < 20_000),
        engine + " replacement sample must only replicate source IDs");
  }

  private static Map<Object, Long> indexDuckBy(List<Row> rows, String key, String value) {
    var result = new HashMap<Object, Long>();
    for (var row : rows) result.put(row.get(key), ((Number) row.get(value)).longValue());
    return result;
  }

  private record Key(Object first, Object second) {}
  private record PairRow(Object key, Object payload) {}

  private static Map<List<Object>, Long> indexSparkRows(List<org.apache.spark.sql.Row> rows) {
    var result = new HashMap<List<Object>, Long>();
    for (var row : rows) {
      var values = new java.util.ArrayList<Object>();
      for (int index = 0; index < row.size(); index++) values.add(row.get(index));
      result.merge(values, 1L, Long::sum);
    }
    return result;
  }

  private static Map<List<Object>, Long> indexDuckRows(List<Row> rows) {
    var result = new HashMap<List<Object>, Long>();
    for (var row : rows) result.merge(row.values(), 1L, Long::sum);
    return result;
  }

  private static void assertFrameContents(Dataset<org.apache.spark.sql.Row> sparkFrame,
      ZFrame<Frame, Row, DuckZColumn> duckFrame) {
    assertEquals(List.of(sparkFrame.columns()), List.of(duckFrame.columns()));
    assertEquals(indexSparkRows(sparkFrame.collectAsList()), indexDuckRows(duckFrame.collectAsList()));
  }

  private static Map<Object, Long> indexSingleColumn(
      List<org.apache.spark.sql.Row> rows, String column) {
    var result = new HashMap<Object, Long>();
    for (var row : rows) result.merge(row.getAs(column), 1L, Long::sum);
    return result;
  }

  private static Map<Object, Long> indexSingleColumnDuck(List<Row> rows, String column) {
    var result = new HashMap<Object, Long>();
    for (var row : rows) result.merge(row.get(column), 1L, Long::sum);
    return result;
  }

  private static Map<Object, Long> indexSparkArrays(List<org.apache.spark.sql.Row> rows, String column) {
    var result = new HashMap<Object, Long>();
    for (var row : rows) {
      int ordinal = row.fieldIndex(column);
      Object value = row.isNullAt(ordinal) ? null : new java.util.ArrayList<>(row.getList(ordinal));
      result.merge(value, 1L, Long::sum);
    }
    return result;
  }

  private static Map<Object, Long> indexDuckArrays(List<Row> rows, String column) throws java.sql.SQLException {
    var result = new HashMap<Object, Long>();
    for (var row : rows) {
      Object raw = row.get(column);
      Object value = raw == null ? null : new java.util.ArrayList<>(
          Arrays.asList((Object[]) ((java.sql.Array) raw).getArray()));
      result.merge(value, 1L, Long::sum);
    }
    return result;
  }

  private static List<Long> sortKeysSpark(List<org.apache.spark.sql.Row> rows) {
    return rows.stream().map(row -> (Number) row.getAs("sort_key"))
        .map(value -> value == null ? null : value.longValue()).toList();
  }

  private static List<Long> sortKeysDuck(List<Row> rows) {
    return rows.stream().map(row -> (Number) row.get("sort_key"))
        .map(value -> value == null ? null : value.longValue()).toList();
  }

  private static Map<PairRow, Long> indexSparkPairs(List<org.apache.spark.sql.Row> rows) {
    return indexSparkPairs(rows, "left_key", "payload");
  }

  private static Map<PairRow, Long> indexSparkPairs(
      List<org.apache.spark.sql.Row> rows, String keyColumn, String payloadColumn) {
    var result = new HashMap<PairRow, Long>();
    for (var row : rows) result.merge(
        new PairRow(row.getAs(keyColumn), row.getAs(payloadColumn)), 1L, Long::sum);
    return result;
  }

  private static Map<PairRow, Long> indexDuckPairs(List<Row> rows) {
    return indexDuckPairs(rows, "left_key", "payload");
  }

  private static Map<PairRow, Long> indexDuckPairs(List<Row> rows, String keyColumn, String payloadColumn) {
    var result = new HashMap<PairRow, Long>();
    for (var row : rows) result.merge(
        new PairRow(row.get(keyColumn), row.get(payloadColumn)), 1L, Long::sum);
    return result;
  }

  private static Map<Key, Long> indexByComposite(List<org.apache.spark.sql.Row> rows) {
    var result = new HashMap<Key, Long>();
    for (var row : rows) result.put(new Key(row.getAs("primary_group"), row.getAs("secondary_group")),
        ((Number) row.getAs("non_null_primary_count")).longValue());
    return result;
  }

  private static Map<Key, Long> indexDuckComposite(List<Row> rows) {
    var result = new HashMap<Key, Long>();
    for (var row : rows) result.put(new Key(row.get("primary_group"), row.get("secondary_group")),
        ((Number) row.get("non_null_primary_count")).longValue());
    return result;
  }

  private static String normalizeSchemaNullability(String schema) {
    return schema.replaceAll(",(true|false)\\)", ",nullable)");
  }
}
