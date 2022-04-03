package zingg.preprocess;

import static org.apache.spark.sql.functions.col;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.List;

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
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import zingg.client.Arguments;
import zingg.client.FieldDefinition;
import zingg.client.util.ColName;

public class TestStopWords {

	protected static Arguments args;
	protected static JavaSparkContext ctx;
	protected static SparkSession spark;

	public static final Log LOG = LogFactory.getLog(TestStopWords.class);

	@BeforeClass
	public static void setup() {
		try {
			spark = SparkSession
					.builder()
					.master("local")
					.appName("Zingg" + "Junit")
					.getOrCreate();
			ctx = new JavaSparkContext(spark.sparkContext());
			JavaSparkContext.jarOfClass(TestStopWords.class);
			args = new Arguments();
		} catch (Throwable e) {
			if (LOG.isDebugEnabled())
				e.printStackTrace();
			LOG.info("Problem in spark env setup");
		}
	}

	@AfterClass
	public static void teardown() {
		if (ctx != null)
			ctx.stop();
		if (spark != null)
			spark.stop();
	}

	@Test
	public void testRemoveStopWords() {
		try {
			StructType schema = new StructType(new StructField[] {
					new StructField("statement", DataTypes.StringType, false, Metadata.empty())
			});

			Dataset<Row> dataset = spark.createDataFrame(
					Arrays.asList(
							RowFactory.create("The zingg is a spark application"),
							RowFactory.create("It is very popular in data science"),
							RowFactory.create("It is written in java and scala"),
							RowFactory.create("Best of luck to zingg")),
					schema);

			List<String> stopWords = Arrays.asList("a", "an", "the", "is", "It", "of");

			Dataset<Row> datasetExpected = spark.createDataFrame(
				Arrays.asList(
						RowFactory.create("The zingg spark application"),
						RowFactory.create("very popular in data science"),
						RowFactory.create("written in java and scala"),
						RowFactory.create("Best luck to zingg")),
				schema);
			
			Dataset<Row> datasetWithoutStopWords = dataset.withColumn("statement",
					StopWords.removeStopWords(stopWords).apply(col("statement")));
 			assertTrue(datasetExpected.except(datasetWithoutStopWords).isEmpty());	
		} catch (Throwable e) {
			fail("Unexpected exception " + e.getMessage());
		}
	}

	@Test
	public void testRemoveStopWordsFromDataset() {
		try {
			StructType schemaOriginal = new StructType(new StructField[] {
					new StructField(ColName.ID_COL, DataTypes.StringType, false, Metadata.empty()),
					new StructField("field1", DataTypes.StringType, false, Metadata.empty()),
					new StructField("field2", DataTypes.StringType, false, Metadata.empty()),
					new StructField("field3", DataTypes.StringType, false, Metadata.empty()),
					new StructField(ColName.SOURCE_COL, DataTypes.StringType, false, Metadata.empty())
			});

			Dataset<Row> original = spark.createDataFrame(
					Arrays.asList(
							RowFactory.create("10", "The zingg is a spark application", "two",
									"Yes. a good application", "test"),
							RowFactory.create("20", "It is very popular in data science", "Three", "true indeed",
									"test"),
							RowFactory.create("30", "It is written in java and scala", "four", "", "test"),
							RowFactory.create("40", "Best of luck to zingg", "Five", "thank you", "test")),
					schemaOriginal);

			Dataset<Row> datasetExpected = spark.createDataFrame(
				Arrays.asList(
						RowFactory.create("10", "The zingg spark application", "two", "Yes. a good application", "test"),
						RowFactory.create("20", "very popular data science", "Three", "true indeed", "test"),
						RowFactory.create("30", "written java scala", "four", "", "test"),
						RowFactory.create("40", "Best luck to zingg", "Five", "thank you", "test")),
				schemaOriginal);

  			String stopWordsFileName = getClass().getResource("../../stopWords.csv").getFile();
 			FieldDefinition fd = new FieldDefinition();
			fd.setStopWords(stopWordsFileName);
			fd.setFieldName("field1");

			List<FieldDefinition> fieldDefinitionList = Arrays.asList(fd);
			args.setFieldDefinition(fieldDefinitionList);

 			Dataset<Row> newDataSet = StopWords.preprocessForStopWords(spark, args, original);
 			assertTrue(datasetExpected.except(newDataSet).isEmpty());
		} catch (Throwable e) {
			fail("Unexpected exception " + e.getMessage());
		}
	}

	@Test
	public void testForOriginalDataAfterPostprocess() {

		try {
			StructType schemaActual = new StructType(new StructField[] {
					new StructField(ColName.CLUSTER_COLUMN, DataTypes.StringType, false, Metadata.empty()),
					new StructField(ColName.ID_COL, DataTypes.StringType, false, Metadata.empty()),
					new StructField(ColName.PREDICTION_COL, DataTypes.StringType, false, Metadata.empty()),
					new StructField(ColName.SCORE_COL, DataTypes.StringType, false, Metadata.empty()),
					new StructField(ColName.MATCH_FLAG_COL, DataTypes.StringType, false, Metadata.empty()),
					new StructField("field1", DataTypes.StringType, false, Metadata.empty()),
					new StructField("field2", DataTypes.StringType, false, Metadata.empty()),
					new StructField("field3", DataTypes.StringType, false, Metadata.empty()),
					new StructField(ColName.SOURCE_COL, DataTypes.StringType, false, Metadata.empty())
			});

			StructType schemaOriginal = new StructType(new StructField[] {
					new StructField(ColName.ID_COL, DataTypes.StringType, false, Metadata.empty()),
					new StructField("field1", DataTypes.StringType, false, Metadata.empty()),
					new StructField("field2", DataTypes.StringType, false, Metadata.empty()),
					new StructField("field3", DataTypes.StringType, false, Metadata.empty()),
					new StructField(ColName.SOURCE_COL, DataTypes.StringType, false, Metadata.empty())
			});

			Dataset<Row> original = spark.createDataFrame(
					Arrays.asList(
							RowFactory.create("10", "The zingg is a spark application", "two",
									"Yes. a good application", "test"),
							RowFactory.create("20", "It is very popular in data science", "Three", "true indeed",
									"test"),
							RowFactory.create("30", "It is written in java and scala", "four", "", "test"),
							RowFactory.create("40", "Best of luck to zingg", "Five", "thank you", "test")),
					schemaOriginal);

			Dataset<Row> actual = spark.createDataFrame(
					Arrays.asList(
							RowFactory.create("1648811730857:10", "10", "1.0", "0.555555", "-1",
									"The zingg spark application", "two", "Yes. good application", "test"),
							RowFactory.create("1648811730857:20", "20", "1.0", "1.0", "-1",
									"It very popular data science", "Three", "true indeed", "test"),
							RowFactory.create("1648811730857:30", "30", "1.0", "0.999995", "-1",
									"It written java scala", "four", "", "test"),
							RowFactory.create("1648811730857:40", "40", "1.0", "1.0", "-1", "Best luck zingg", "Five",
									"thank", "test")),
					schemaActual);

			Dataset<Row> newDataset = StopWords.postprocess(actual, original);
			assertTrue(newDataset.select(ColName.ID_COL, "field1", "field2", "field3", ColName.SOURCE_COL).except(original).isEmpty());
		} catch (Throwable e) {
			fail("Unexpected exception " + e.getMessage());
		}
	}

	@Test
	public void testOriginalDataAfterPostprocessLinked() {

		try {
			StructType schemaActual = new StructType(new StructField[] {
					new StructField(ColName.CLUSTER_COLUMN, DataTypes.StringType, false, Metadata.empty()),
					new StructField(ColName.ID_COL, DataTypes.StringType, false, Metadata.empty()),
					new StructField(ColName.PREDICTION_COL, DataTypes.StringType, false, Metadata.empty()),
					new StructField(ColName.SCORE_COL, DataTypes.StringType, false, Metadata.empty()),
					new StructField(ColName.MATCH_FLAG_COL, DataTypes.StringType, false, Metadata.empty()),
					new StructField("field1", DataTypes.StringType, false, Metadata.empty()),
					new StructField("field2", DataTypes.StringType, false, Metadata.empty()),
					new StructField("field3", DataTypes.StringType, false, Metadata.empty()),
					new StructField(ColName.SOURCE_COL, DataTypes.StringType, false, Metadata.empty())
			});

			StructType schemaOriginal = new StructType(new StructField[] {
					new StructField(ColName.ID_COL, DataTypes.StringType, false, Metadata.empty()),
					new StructField("field1", DataTypes.StringType, false, Metadata.empty()),
					new StructField("field2", DataTypes.StringType, false, Metadata.empty()),
					new StructField("field3", DataTypes.StringType, false, Metadata.empty()),
					new StructField(ColName.SOURCE_COL, DataTypes.StringType, false, Metadata.empty())
			});

			Dataset<Row> original = spark.createDataFrame(
					Arrays.asList(
							RowFactory.create("10", "The zingg is a spark application", "two",
									"Yes. a good application", "test"),
							RowFactory.create("20", "It is very popular in data science", "Three", "true indeed",
									"test"),
							RowFactory.create("30", "It is written in java and scala", "four", "", "test"),
							RowFactory.create("40", "Best of luck to zingg", "Five", "thank you", "test")),
					schemaOriginal);

			Dataset<Row> actual = spark.createDataFrame(
					Arrays.asList(
							RowFactory.create("1648811730857:10", "10", "1.0", "0.555555", "-1",
									"The zingg spark application", "two", "Yes. good application", "test"),
							RowFactory.create("1648811730857:20", "20", "1.0", "1.0", "-1",
									"It very popular data science", "Three", "true indeed", "test"),
							RowFactory.create("1648811730857:30", "30", "1.0", "0.999995", "-1",
									"It written java scala", "four", "", "test"),
							RowFactory.create("1648811730857:40", "40", "1.0", "1.0", "-1", "Best luck zingg", "Five",
									"thank", "test")),
					schemaActual);

			Dataset<Row> newDataset = StopWords.postprocessLinked(actual, original);
  			assertTrue(newDataset.select("field1", "field2", "field3", ColName.SOURCE_COL).except(original.drop(ColName.ID_COL)).isEmpty());
		} catch (Throwable e) {
			fail("Unexpected exception " + e.getMessage());
		}
	}
}