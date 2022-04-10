package zingg.client;

import static org.apache.spark.sql.functions.col;
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

public class TestSparkFrame extends BaseSparkTest {
	public static final Log LOG = LogFactory.getLog(TestSparkFrame.class);

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
		Dataset<Row> df = sf.df();
		String aliasName = "AnotherName";
		sf.as(aliasName);
		assertTrue(sf.as(aliasName).except(sf).isEmpty(), "Dataframe and its alias are not same");
	}

	@Test
	public void testSelectWithSingleColumnName() {
		Dataset<Row> df = createSampleDataset();
		SparkFrame sf = new SparkFrame(df);
		String colName = "recid";
		ZFrame<Dataset<Row>, Row, Column> sf2 = sf.select(colName);
		SparkFrame sf3 = new SparkFrame(df.select(colName));
		assertTrue(sf2.except(sf3).isEmpty(), "SparkFrame.select(colName) does not have expected value");
	}

	@Test
	public void testSelectWithColumnList() {
		Dataset<Row> df = createSampleDataset();
		SparkFrame sf = new SparkFrame(df);
		List<Column> columnList = Arrays.asList(col("recid"), col("surname"), col("postcode"));
		ZFrame<Dataset<Row>, Row, Column> sf2 = sf.select(columnList);
		SparkFrame sf3 = new SparkFrame(
				df.select(JavaConverters.asScalaIteratorConverter(columnList.iterator()).asScala().toSeq()));
		assertTrue(sf2.except(sf3).isEmpty(), "SparkFrame.select(columnList) does not have expected value");
	}
}
