package zingg.common.core.preprocess;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
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

import zingg.common.client.Arguments;
import zingg.common.client.FieldDefinition;
import zingg.common.client.MatchType;
import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;
import zingg.common.client.util.ColName;
import zingg.spark.client.SparkFrame;
import zingg.spark.core.executor.ZinggSparkTester;
import zingg.spark.core.preprocess.SparkStopWordsRemover;

public class TestStopWords extends ZinggSparkTester{

	public static final Log LOG = LogFactory.getLog(TestStopWords.class);

	@DisplayName ("Test Stop Words removal from Single column dataset")
	@Test
	public void testStopWordsSingleColumn() throws ZinggClientException {
		
			StructType schema = new StructType(new StructField[] {
					new StructField("statement", DataTypes.StringType, false, Metadata.empty())
			});

			Dataset<Row> datasetOriginal = spark.createDataFrame(
					Arrays.asList(
							RowFactory.create("The zingg is a Spark application"),
							RowFactory.create("It is very popular in data Science"),
							RowFactory.create("It is written in Java and Scala"),
							RowFactory.create("Best of luck to zingg")),
					schema);

			String stopWords = "\\b(a|an|the|is|It|of|yes|no|I|has|have|you)\\b\\s?".toLowerCase();

			Dataset<Row> datasetExpected = spark.createDataFrame(
				Arrays.asList(
						RowFactory.create("zingg spark application"),
						RowFactory.create("very popular in data science"),
						RowFactory.create("written in java and scala"),
						RowFactory.create("best luck to zingg")),
				schema);
			
			List<FieldDefinition> fdList = new ArrayList<>(4);
			
			ArrayList<MatchType> matchTypelistFuzzy = new ArrayList<MatchType>();
			matchTypelistFuzzy.add(MatchType.FUZZY);

			FieldDefinition eventFD = new FieldDefinition();
			eventFD.setDataType("string");
			eventFD.setFieldName("statement");
			eventFD.setMatchType(matchTypelistFuzzy);
			fdList.add(eventFD);

			Arguments stmtArgs = new Arguments();
			stmtArgs.setFieldDefinition(fdList);
			
			StopWordsRemover stopWordsObj = new SparkStopWordsRemover();
			System.out.println("datasetOriginal.show() : ");
			datasetOriginal.show();
			SparkFrame datasetWithoutStopWords = (SparkFrame)stopWordsObj.removeStopWordsFromDF(zsCTX.getSession(), new SparkFrame(datasetOriginal),"statement",stopWords);
			System.out.println("datasetWithoutStopWords.show() : ");
			datasetWithoutStopWords.show();
			
 			assertTrue(datasetExpected.except(datasetWithoutStopWords.df()).isEmpty());
			assertTrue(datasetWithoutStopWords.df().except(datasetExpected).isEmpty());
	}

	@Test
	public void testRemoveStopWordsFromDataset() throws ZinggClientException {
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
  			String stopWordsFileName = getClass().getResource("../../../../preProcess/stopWords.csv").getFile();
 			FieldDefinition fd = new FieldDefinition();
			fd.setStopWords(stopWordsFileName);
			fd.setFieldName("field1");

			List<FieldDefinition> fieldDefinitionList = Arrays.asList(fd);
			args.setFieldDefinition(fieldDefinitionList);

 			Dataset<Row> newDataSet = ((SparkFrame)(zsCTX.getPreprocUtil().preprocess(args,new SparkFrame(original)))).df();
 			assertTrue(datasetExpected.except(newDataSet).isEmpty());
			assertTrue(newDataSet.except(datasetExpected).isEmpty());
	}

	@Test
	public void testStopWordColumnMissingFromStopWordFile() throws ZinggClientException {
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
  			String stopWordsFileName = getClass().getResource("../../../../preProcess/stopWordsWithoutHeader.csv").getFile();
 			FieldDefinition fd = new FieldDefinition();
			fd.setStopWords(stopWordsFileName);
			fd.setFieldName("field1");

			List<FieldDefinition> fieldDefinitionList = Arrays.asList(fd);
			args.setFieldDefinition(fieldDefinitionList);
			
			System.out.println("testStopWordColumnMissingFromStopWordFile : orginal ");			
			original.show(200);
 			Dataset<Row> newDataSet = ((SparkFrame)(zsCTX.getPreprocUtil().preprocess(args,new SparkFrame(original)))).df();
 			System.out.println("testStopWordColumnMissingFromStopWordFile : newDataSet ");		
 			newDataSet.show(200);
 			System.out.println("testStopWordColumnMissingFromStopWordFile : datasetExpected ");	
 			datasetExpected.show(200);
 			assertTrue(datasetExpected.except(newDataSet).isEmpty());
			assertTrue(newDataSet.except(datasetExpected).isEmpty());
	}
	

	@Test
	public void testForOriginalDataAfterPostprocess() {
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

			Dataset<Row> newDataset = ((SparkFrame)(zsCTX.getDSUtil().postprocess(new SparkFrame(actual), new SparkFrame(original)))).df();
			assertTrue(newDataset.select(ColName.ID_COL, "field1", "field2", "field3", ColName.SOURCE_COL).except(original).isEmpty());
			assertTrue(original.except(newDataset.select(ColName.ID_COL, "field1", "field2", "field3", ColName.SOURCE_COL)).isEmpty());
	}

	@Test
	public void testOriginalDataAfterPostprocessLinked() {
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
			
			System.out.println("testOriginalDataAfterPostprocessLinked original :");
			original.show(200);
			
			Dataset<Row> newDataset = ((SparkFrame)(zsCTX.getDSUtil().postprocessLinked(new SparkFrame(actual), new SparkFrame(original)))).df();
			
			System.out.println("testOriginalDataAfterPostprocessLinked newDataset :");
			newDataset.show(200);
			
			assertTrue(newDataset.select("field1", "field2", "field3").except(original.select("field1", "field2", "field3")).isEmpty());
			assertTrue(original.select("field1", "field2", "field3").except(newDataset.select("field1", "field2", "field3")).isEmpty());
	}
}