package zingg.client;

import static org.apache.spark.sql.functions.col;
import org.apache.spark.sql.functions;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.junit.jupiter.api.Test;

import scala.collection.JavaConverters;

public class TestSparkFrame extends TestSparkFrameBase {
	public static final Log LOG = LogFactory.getLog(TestSparkFrame.class);

	public static final String NEW_COLUMN = "newColumn";

	@Test
	public void testCreateSparkDataFrameAndGetDF() {
		SparkFrame sf = new SparkFrame(createSampleDataset());
		Dataset<Row> df = sf.df();
		assertTrue(df.except(createSampleDataset()).isEmpty(), "Two datasets are not equal");
	}

	@Test
	public void testColumnsNamesandCount() {
		SparkFrame sf = new SparkFrame(createSampleDataset());
		assertTrue(Arrays.equals(sf.columns(), createSampleDataset().columns()),
				"Columns of SparkFrame and the dataset are not equal");
	}

	@Test
	public void testAliasOfSparkFrame() {
		SparkFrame sf = new SparkFrame(createSampleDataset());
		String aliasName = "AnotherName";
		sf.as(aliasName);
		assertTrueCheckingExceptOutput(sf.as(aliasName), sf, "Dataframe and its alias are not same");
	}

	@Test
	public void testSelectWithSingleColumnName() {
		Dataset<Row> df = createSampleDataset();
		SparkFrame sf = new SparkFrame(df);
		String colName = "recid";
		ZFrame<Dataset<Row>, Row, Column> sf2 = sf.select(colName);
		SparkFrame sf3 = new SparkFrame(df.select(colName));
		assertTrueCheckingExceptOutput(sf2, sf3, "SparkFrame.select(colName) does not have expected value");
	}

	@Test
	public void testSelectWithColumnList() {
		Dataset<Row> df = createSampleDataset();
		SparkFrame sf = new SparkFrame(df);
		List<Column> columnList = Arrays.asList(col("recid"), col("surname"), col("postcode"));
		ZFrame<Dataset<Row>, Row, Column> sf2 = sf.select(columnList);
		SparkFrame sf3 = new SparkFrame(
				df.select(JavaConverters.asScalaIteratorConverter(columnList.iterator()).asScala().toSeq()));
		assertTrueCheckingExceptOutput(sf2, sf3, "SparkFrame.select(columnList) does not have expected value");
	}

	@Test
	public void testSelectWithColumnArray() {
		Dataset<Row> df = createSampleDataset();
		SparkFrame sf = new SparkFrame(df);
		Column[] columnArray = new Column[] {col("recid"), col("surname"), col("postcode")};
		ZFrame<Dataset<Row>, Row, Column> sf2 = sf.select(columnArray);
		SparkFrame sf3 = new SparkFrame(df.select(columnArray));
		assertTrueCheckingExceptOutput(sf2, sf3, "SparkFrame.select(columnArray) value does not match with standard select output");
	}

	@Test
	public void testSelectWithMultipleColumnNamesAsString() {
		Dataset<Row> df = createSampleDataset();
		SparkFrame sf = new SparkFrame(df);
		ZFrame<Dataset<Row>, Row, Column> sf2 = sf.select("recid",  "surname",  "postcode");
		SparkFrame sf3 = new SparkFrame(df.select("recid",  "surname",  "postcode"));
		assertTrueCheckingExceptOutput(sf2, sf3, "SparkFrame.select(str1, str2, ...) value does not match with standard select output");
	}

	@Test
	public void testSelectExprByPassingColumnStringsAsInSQLStatement() {
		Dataset<Row> df = createSampleDataset();
		SparkFrame sf = new SparkFrame(df);
		ZFrame<Dataset<Row>, Row, Column> sf2 = sf.selectExpr("recid as RecordId",  "surname as FamilyName",  "postcode as Pin");
 		SparkFrame sf3 = new SparkFrame(df.selectExpr("recid",  "surname",  "postcode"));
 		assertTrueCheckingExceptOutput(sf2, sf3, "SparkFrame.selectExpr(str1, str2, ...) value does not match with standard selectExpr output");
	}

	@Test
	public void testDistinct() {
		Dataset<Row> df = createSampleDataset();
		SparkFrame sf = new SparkFrame(df);
 		SparkFrame sf2 = new SparkFrame(df.distinct());
		 assertTrueCheckingExceptOutput(sf.distinct(), sf2, "SparkFrame.distict() does not match with standard distict() output");
	}

	@Test
	public void testDropSingleColumn() {
		Dataset<Row> df = createSampleDataset();
		SparkFrame sf = new SparkFrame(df);
		ZFrame<Dataset<Row>, Row, Column> sf2 = new SparkFrame(df.drop("recid"));
		assertTrueCheckingExceptOutput(sf2, sf.drop("recid"), "SparkFrame.drop(str) does not match with standard drop() output");
	}

	@Test
	public void testDropColumnsAsStringArray() {
		Dataset<Row> df = createSampleDataset();
		SparkFrame sf = new SparkFrame(df);
		ZFrame<Dataset<Row>, Row, Column> sf2 = new SparkFrame(df.drop("recid",  "surname",  "postcode"));
		assertTrueCheckingExceptOutput(sf2, sf.drop("recid", "surname", "postcode"), "SparkFrame.drop(str...) does not match with standard drop(str...) output");
	}

	@Test
	public void testLimit() {
		Dataset<Row> df = createSampleDataset();
		SparkFrame sf = new SparkFrame(df);
		int len = 5;
		ZFrame<Dataset<Row>, Row, Column> sf2 = sf.limit(len);
		assertTrue(sf2.count() == len);
		assertTrueCheckingExceptOutput(sf2, sf.limit(len), "SparkFrame.limit(len) does not match with standard limit(len) output");
	}

	@Test
	public void testDropDuplicatesConsideringGivenColumnsAsStringArray() {
		Dataset<Row> df = createSampleDataset();
		SparkFrame sf = new SparkFrame(df);
		String[] columnArray = new String[] {"surname", "postcode"};
		ZFrame<Dataset<Row>, Row, Column> sf2 = new SparkFrame(df.dropDuplicates(columnArray));
		assertTrueCheckingExceptOutput(sf2, sf.dropDuplicates(columnArray), "SparkFrame.dropDuplicates(str[]) does not match with standard dropDuplicates(str[]) output");
	}

	@Test
	public void testDropDuplicatesConsideringGivenIndividualColumnsAsString() {
		Dataset<Row> df = createSampleDataset();
		SparkFrame sf = new SparkFrame(df);
		ZFrame<Dataset<Row>, Row, Column> sf2 = new SparkFrame(df.dropDuplicates("surname", "postcode"));
		assertTrueCheckingExceptOutput(sf2, sf.dropDuplicates("surname"), "SparkFrame.dropDuplicates(col1, col2) does not match with standard dropDuplicates(col1, col2) output");
	}

	@Test
	public void testHead() {
		Dataset<Row> df = createSampleDataset();
		SparkFrame sf = new SparkFrame(df);
		Row row = sf.head();
		assertTrue(row.equals(df.head()), "Top Row is not the expected one");
	 }

	@Test
	public void testIsEmpty() {
		Dataset<Row> df = spark.emptyDataFrame();
		SparkFrame sf = new SparkFrame(df);
		assertTrue(sf.isEmpty(), "DataFrame is not empty");
	 }

	@Test
	public void testGetAsInt() {
		Dataset<Row> df = createSampleDatasetHavingMixedDataTypes();
		SparkFrame sf = new SparkFrame(df);
		Row row = sf.head();
		LOG.debug("Value: " + row.getAs("recid"));
		assertTrue(sf.getAsInt(row, "recid") == (int) row.getAs("recid"), "row.getAsInt(col) hasn't returned correct int value");
	}
	@Test
	public void testGetAsString() {
		Dataset<Row> df = createSampleDatasetHavingMixedDataTypes();
		SparkFrame sf = new SparkFrame(df);
		Row row = sf.head();
		LOG.debug("Value: " + row.getAs("surname"));
		assertTrue(sf.getAsString(row, "surname").equals(row.getAs("surname")), "row.getAsString(col) hasn't returned correct string value");
	}
	@Test
	public void testGetAsDouble() {
		Dataset<Row> df = createSampleDatasetHavingMixedDataTypes();
		SparkFrame sf = new SparkFrame(df);
		Row row = sf.head();
		LOG.debug("Value: " + row.getAs("cost"));
		assertTrue(sf.getAsDouble(row, "cost") == (double) row.getAs("cost"), "row.getAsDouble(col) hasn't returned correct double value");
	}
	@Test
	public void testSortDescending() {
		Dataset<Row> df = createSampleDatasetHavingMixedDataTypes();
		SparkFrame sf = new SparkFrame(df);
		String col = STR_RECID;
		ZFrame<Dataset<Row>,Row,Column> sf2 = sf.sortDescending(col);
		assertTrueCheckingExceptOutput(sf2, df.sort(functions.desc(col)), "SparkFrame.sortDescending() output is not as expected");
	}
	
	@Test
	public void testSortAscending() {
		Dataset<Row> df = createSampleDatasetHavingMixedDataTypes();
		SparkFrame sf = new SparkFrame(df);
		String col = STR_RECID;
		ZFrame<Dataset<Row>,Row,Column> sf2 = sf.sortAscending(col);
		assertTrueCheckingExceptOutput(sf2, df.sort(functions.asc(col)), "SparkFrame.sortAscending() output is not as expected");
	}

	@Test
	public void testWithColumnforIntegerValue() {
		Dataset<Row> df = createSampleDatasetHavingMixedDataTypes();
		SparkFrame sf = new SparkFrame(df);
		String newCol = NEW_COLUMN;
		int newColVal = 36;
		ZFrame<Dataset<Row>,Row,Column> sf2 = sf.withColumn(newCol, newColVal);
 		assertTrueCheckingExceptOutput(sf2, df.withColumn(newCol, functions.lit(newColVal)), "SparkFrame.withColumn(c, int) output is not as expected");
	}

	@Test
	public void testWithColumnforDoubleValue() {
		Dataset<Row> df = createSampleDatasetHavingMixedDataTypes();
		SparkFrame sf = new SparkFrame(df);
		String newCol = NEW_COLUMN;
		double newColVal = 3.14;
		ZFrame<Dataset<Row>,Row,Column> sf2 = sf.withColumn(newCol, newColVal);
 		assertTrueCheckingExceptOutput(sf2, df.withColumn(newCol, functions.lit(newColVal)), "SparkFrame.withColumn(c, double) output is not as expected");
	}

	@Test
	public void testWithColumnforStringValue() {
		Dataset<Row> df = createSampleDatasetHavingMixedDataTypes();
		SparkFrame sf = new SparkFrame(df);
		String newCol = NEW_COLUMN;
		String newColVal = "zingg";
		ZFrame<Dataset<Row>,Row,Column> sf2 = sf.withColumn(newCol, newColVal);
 		assertTrueCheckingExceptOutput(sf2, df.withColumn(newCol, functions.lit(newColVal)), "SparkFrame.withColumn(c, String) output is not as expected");
	}

	@Test
	public void testWithColumnforAnotherColumn() {
		Dataset<Row> df = createSampleDatasetHavingMixedDataTypes();
		SparkFrame sf = new SparkFrame(df);
		String oldCol = STR_RECID;
		String newCol = NEW_COLUMN;
		ZFrame<Dataset<Row>,Row,Column> sf2 = sf.withColumn(newCol, col(oldCol));
  		assertTrueCheckingExceptOutput(sf2, df.withColumn(newCol, col(oldCol)), "SparkFrame.withColumn(c, Column) output is not as expected");
	}
}