package zingg.preprocess;

import static org.apache.spark.sql.functions.col;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.Metadata;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import zingg.BaseSparkTest;
import zingg.client.FieldDefinition;
import zingg.client.util.ColName;
import zingg.util.DSUtil;

public class TestStopWords extends BaseSparkTest{

	public static final Log LOG = LogFactory.getLog(TestStopWords.class);

	@DisplayName ("Test Stop Words removal from Single column dataset")
	@Test
	public void testStopWordsSingleColumn() {
		try {
			StructType schema = new StructType(new StructField[] {
					new StructField("statement", DataTypes.StringType, false, Metadata.empty())
			});

			Dataset<Row> dataset = spark.createDataFrame(
					Arrays.asList(
							RowFactory.create("The zingg is a Spark application"),
							RowFactory.create("It is very popular in data Science"),
							RowFactory.create("It is written in Java and Scala"),
							RowFactory.create("Best of luck to zingg")),
					schema);

			String stopWords = "\\b(a|an|the|is|It|of|yes|no|I|has|have|you)\\b\\s?";

			Dataset<Row> datasetExpected = spark.createDataFrame(
				Arrays.asList(
						RowFactory.create("zingg spark application"),
						RowFactory.create("very popular in data science"),
						RowFactory.create("written in java and scala"),
						RowFactory.create("best luck to zingg")),
				schema);
			Dataset<Row> datasetWithoutStopWords = dataset.withColumn("statement",
					StopWords.removeStopWords(stopWords.toLowerCase()).apply(col("statement")));
 			assertTrue(datasetExpected.except(datasetWithoutStopWords).isEmpty());
			assertTrue(datasetWithoutStopWords.except(datasetExpected).isEmpty());
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
							RowFactory.create("20", "It is very popular in Data Science", "Three", "true indeed",
									"test"),
							RowFactory.create("30", "It is written in java and scala", "four", "", "test"),
							RowFactory.create("40", "Best of luck to zingg Mobile/T-Mobile", "Five", "thank you", "test")),
					schemaOriginal);

			Dataset<Row> datasetExpected = spark.createDataFrame(
				Arrays.asList(
						RowFactory.create("10", "zingg spark application", "two", "Yes. a good application", "test"),
						RowFactory.create("20", "very popular data science", "Three", "true indeed", "test"),
						RowFactory.create("30", "written java scala", "four", "", "test"),
						RowFactory.create("40", "best luck to zingg ", "Five", "thank you", "test")),
				schemaOriginal);
  			String stopWordsFileName = getClass().getResource("../../stopWords.csv").getFile();
 			FieldDefinition fd = new FieldDefinition();
			fd.setStopWords(stopWordsFileName);
			fd.setFieldName("field1");

			List<FieldDefinition> fieldDefinitionList = Arrays.asList(fd);
			args.setFieldDefinition(fieldDefinitionList);

 			Dataset<Row> newDataSet = StopWords.preprocessForStopWords(spark, args, original);
 			assertTrue(datasetExpected.except(newDataSet).isEmpty());
			assertTrue(newDataSet.except(datasetExpected).isEmpty());
		} catch (Throwable e) {
			fail("Unexpected exception " + e.getMessage());
		}
	}

	@Test
	public void testStopWordColumnMissingFromStopWordFile() {
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
							RowFactory.create("20", "It is very popular in Data Science", "Three", "true indeed",
									"test"),
							RowFactory.create("30", "It is written in java and scala", "four", "", "test"),
							RowFactory.create("40", "Best of luck to zingg Mobile/T-Mobile", "Five", "thank you", "test")),
					schemaOriginal);

			Dataset<Row> datasetExpected = spark.createDataFrame(
				Arrays.asList(
						RowFactory.create("10", "zingg spark application", "two", "Yes. a good application", "test"),
						RowFactory.create("20", "very popular data science", "Three", "true indeed", "test"),
						RowFactory.create("30", "written java scala", "four", "", "test"),
						RowFactory.create("40", "best luck to zingg ", "Five", "thank you", "test")),
				schemaOriginal);
  			String stopWordsFileName = getClass().getResource("../../stopWordsWithoutHeader.csv").getFile();
 			FieldDefinition fd = new FieldDefinition();
			fd.setStopWords(stopWordsFileName);
			fd.setFieldName("field1");

			List<FieldDefinition> fieldDefinitionList = Arrays.asList(fd);
			args.setFieldDefinition(fieldDefinitionList);

 			Dataset<Row> newDataSet = StopWords.preprocessForStopWords(spark, args, original);
 			assertTrue(datasetExpected.except(newDataSet).isEmpty());
			assertTrue(newDataSet.except(datasetExpected).isEmpty());
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

			Dataset<Row> newDataset = DSUtil.postprocess(actual, original);
			assertTrue(newDataset.select(ColName.ID_COL, "field1", "field2", "field3", ColName.SOURCE_COL).except(original).isEmpty());
			assertTrue(original.except(newDataset.select(ColName.ID_COL, "field1", "field2", "field3", ColName.SOURCE_COL)).isEmpty());
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

			Dataset<Row> newDataset = DSUtil.postprocessLinked(actual, original);
  			assertTrue(newDataset.select("field1", "field2", "field3", ColName.SOURCE_COL).except(original.drop(ColName.ID_COL)).isEmpty());
			assertTrue(original.drop(ColName.ID_COL).except(newDataset.select("field1", "field2", "field3", ColName.SOURCE_COL)).isEmpty());
		} catch (Throwable e) {
			fail("Unexpected exception " + e.getMessage());
		}
	}
}