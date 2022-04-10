package zingg.client;

import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.api.java.JavaSparkContext;
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

public class BaseSparkTest {

	public static Arguments args;
	public static JavaSparkContext ctx;
	public static SparkSession spark;

	public static final Log LOG = LogFactory.getLog(BaseSparkTest.class);

	@BeforeAll
	public static void setup() {
		try {
			spark = SparkSession
					.builder()
					.master("local[*]")
					.appName("Zingg" + "Junit")
					.getOrCreate();
			ctx = new JavaSparkContext(spark.sparkContext());
			JavaSparkContext.jarOfClass(BaseSparkTest.class);
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

}
