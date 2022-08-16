package zingg.client;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.Metadata;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

public class TestSparkFrameBase {

	public static Arguments args;
	public static JavaSparkContext ctx;
	public static SparkSession spark;

	public static final Log LOG = LogFactory.getLog(TestSparkFrameBase.class);

	public static final String STR_RECID = "recid";
	public static final String STR_GIVENNAME = "givenname";
	public static final String STR_SURNAME = "surname";
	public static final String STR_COST = "cost";
	public static final String STR_POSTCODE = "postcode";
	public static final String STR_SUBURB = "suburb";

	@BeforeAll
	public static void setup() {
		try {
			spark = SparkSession
					.builder()
					.master("local[*]")
					.appName("Zingg" + "Junit")
					.getOrCreate();
			ctx = new JavaSparkContext(spark.sparkContext());
			JavaSparkContext.jarOfClass(TestSparkFrameBase.class);
			args = new Arguments();
		} catch (Throwable e) {
			if (LOG.isDebugEnabled())
				e.printStackTrace();
			LOG.info("Problem in spark env setup");
		}
	}

	@AfterAll
	public static void teardown() {
		if (ctx != null) {
			ctx.stop();
			ctx = null;
		}
		if (spark != null) {
			spark.stop();
			spark = null;
		}
	}

	public Dataset<Row> createSampleDataset() {
		StructType schemaOfSample = new StructType(new StructField[] {
				new StructField("recid", DataTypes.StringType, false, Metadata.empty()),
				new StructField("givenname", DataTypes.StringType, false, Metadata.empty()),
				new StructField("surname", DataTypes.StringType, false, Metadata.empty()),
				new StructField("suburb", DataTypes.StringType, false, Metadata.empty()),
				new StructField("postcode", DataTypes.StringType, false, Metadata.empty())
		});

		Dataset<Row> sample = spark.createDataFrame(Arrays.asList(
				RowFactory.create("07317257", "erjc", "henson", "hendersonville", "2873g"),
				RowFactory.create("03102490", "jhon", "kozak", "henders0nville", "28792"),
				RowFactory.create("02890805", "david", "pisczek", "durham", "27717"),
				RowFactory.create("04437063", "e5in", "bbrown", "greenville", "27858"),
				RowFactory.create("03211564", "susan", "jones", "greenjboro", "274o7"),
				RowFactory.create("04155808", "jerome", "wilkins", "battleborn", "2780g"),
				RowFactory.create("05723231", "clarinw", "pastoreus", "elizabeth city", "27909"),
				RowFactory.create("06087743", "william", "craven", "greenshoro", "27405"),
				RowFactory.create("00538491", "marh", "jackdon", "greensboro", "27406"),
				RowFactory.create("01306702", "vonnell", "palmer", "siler sity", "273q4")), schemaOfSample);

		return sample;
	}

	public Dataset<Row> createSampleDatasetHavingMixedDataTypes() {
		StructType schemaOfSample = new StructType(new StructField[] {
				new StructField(STR_RECID, DataTypes.IntegerType, false, Metadata.empty()),
				new StructField(STR_GIVENNAME, DataTypes.StringType, false, Metadata.empty()),
				new StructField(STR_SURNAME, DataTypes.StringType, false, Metadata.empty()),
				new StructField(STR_COST, DataTypes.DoubleType, false, Metadata.empty()),
				new StructField(STR_POSTCODE, DataTypes.IntegerType, false, Metadata.empty())
		});

		Dataset<Row> sample = spark.createDataFrame(Arrays.asList(
				RowFactory.create(7317, "erjc", "henson", 0.54, 2873),
				RowFactory.create(3102, "jhon", "kozak", 99.009, 28792),
				RowFactory.create(2890, "david", "pisczek", 58.456, 27717),
				RowFactory.create(4437, "e5in", "bbrown", 128.45, 27858)
				), schemaOfSample);

		return sample;
	}

	protected void assertTrueCheckingExceptOutput(ZFrame<Dataset<Row>, Row, Column> sf1, ZFrame<Dataset<Row>, Row, Column> sf2, String message) {
		assertTrue(sf1.except(sf2).isEmpty(), message);
	}

	protected void assertTrueCheckingExceptOutput(ZFrame<Dataset<Row>, Row, Column> sf1, Dataset<Row> df2, String message) {
		SparkFrame sf2 = new SparkFrame(df2);
		assertTrue(sf1.except(sf2).isEmpty(), message);
	}
}