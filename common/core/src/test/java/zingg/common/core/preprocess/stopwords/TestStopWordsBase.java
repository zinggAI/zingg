package zingg.common.core.preprocess.stopwords;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;
import zingg.common.client.util.ColName;
import zingg.common.client.util.DFObjectUtil;
import zingg.common.core.context.Context;
import zingg.common.core.data.EventTestData;
import zingg.common.core.preprocess.data.StopWordsData;
import zingg.common.core.preprocess.model.PostStopWordProcess;
import zingg.common.core.preprocess.model.PriorStopWordProcess;
import zingg.common.core.preprocess.model.Statement;
import zingg.common.core.util.StopWordRemoverUtility;

public abstract class TestStopWordsBase<S, D, R, C, T> {

	public static final Log LOG = LogFactory.getLog(TestStopWordsBase.class);
	private final DFObjectUtil<S, D, R, C> dfObjectUtil;
	private final StopWordRemoverUtility<S, D, R, C, T> stopWordRemoverUtility;
	private final Context<S, D, R, C, T> context;


	public TestStopWordsBase(DFObjectUtil<S, D, R, C> dfObjectUtil, StopWordRemoverUtility<S, D, R, C, T> stopWordRemoverUtility, Context<S, D, R, C, T> context) {
		this.dfObjectUtil = dfObjectUtil;
		this.stopWordRemoverUtility = stopWordRemoverUtility;
		this.context = context;
	}

	@DisplayName ("Test Stop Words removal from Single column dataset")
	@Test
	public void testStopWordsSingleColumn() throws ZinggClientException, Exception {

		//check functionality of removeStopWordsFromDF - for a single column of data
			List<StopWordsRemover<S, D, R, C, T>> stopWordsRemovers = getStopWordsRemovers();
			String stopWords = "\\b(a|an|the|is|It|of|yes|no|I|has|have|you)\\b\\s?".toLowerCase();

			ZFrame<D, R, C> zFrameOriginal = dfObjectUtil.getDFFromObjectList(StopWordsData.getData1Original(), Statement.class);
			ZFrame<D, R, C> zFrameExpected = dfObjectUtil.getDFFromObjectList(StopWordsData.getData1Expected(), Statement.class);

			StopWordsRemover<S, D, R, C, T> stopWordsRemover = stopWordsRemovers.get(0);
			assertFalse(stopWordsRemover.isApplicable());
			stopWordsRemover.preprocess(zFrameOriginal);
			ZFrame<D, R, C> newZFrame = stopWordsRemover.removeStopWordsFromDF(zFrameOriginal,"statement",stopWords);
			
 			assertTrue(zFrameExpected.except(newZFrame).isEmpty());
			assertTrue(newZFrame.except(zFrameExpected).isEmpty());
	}

	@Test
	public void testRemoveStopWordsFromDataset() throws ZinggClientException, Exception {

		//check functionality of preprocess on dataset with header in csv as StopWord
			List<StopWordsRemover<S, D, R, C, T>> stopWordsRemovers = getStopWordsRemovers();

			ZFrame<D, R, C> zFrameOriginal = dfObjectUtil.getDFFromObjectList(StopWordsData.getData2Original(), PriorStopWordProcess.class);
			ZFrame<D, R, C> zFrameExpected = dfObjectUtil.getDFFromObjectList(StopWordsData.getData2Expected(), PriorStopWordProcess.class);

			StopWordsRemover<S, D, R, C, T> stopWordsRemover = stopWordsRemovers.get(1);
			assertTrue(stopWordsRemover.isApplicable());
			ZFrame<D, R, C> newZFrame = stopWordsRemover.preprocess(zFrameOriginal);
				
 			assertTrue(zFrameExpected.except(newZFrame).isEmpty());
			assertTrue(newZFrame.except(zFrameExpected).isEmpty());
	}

	@Test
	public void testStopWordColumnMissingFromStopWordFile() throws ZinggClientException, Exception {

			//check functionality of preprocess on dataset with header in csv as Header - dummy to ensure it is being ignored by default
			List<StopWordsRemover<S, D, R, C, T>> stopWordsRemovers = getStopWordsRemovers();

			ZFrame<D, R, C> zFrameOriginal = dfObjectUtil.getDFFromObjectList(StopWordsData.getData3Original(), PriorStopWordProcess.class);
			ZFrame<D, R, C> zFrameExpected = dfObjectUtil.getDFFromObjectList(StopWordsData.getData3Expected(), PriorStopWordProcess.class);

			StopWordsRemover<S, D, R, C, T> stopWordsRemover = stopWordsRemovers.get(2);
			assertTrue(stopWordsRemover.isApplicable());
 			ZFrame<D, R, C> newZFrame = stopWordsRemover.preprocess(zFrameOriginal);

 			assertTrue(zFrameExpected.except(newZFrame).isEmpty());
			assertTrue(newZFrame.except(zFrameExpected).isEmpty());
	}

	@Test
	public void testStopWordMultipleColumnFromStopWordFile() throws ZinggClientException, Exception {

			//check functionality of preprocess on dataset with multiple columns in csv - check default is first column
			List<StopWordsRemover<S, D, R, C, T>> stopWordsRemovers = getStopWordsRemovers();

			ZFrame<D, R, C> zFrameOriginal = dfObjectUtil.getDFFromObjectList(EventTestData.getData3Original(), PriorStopWordProcess.class);
			ZFrame<D, R, C> zFrameExpected = dfObjectUtil.getDFFromObjectList(EventTestData.getData3Expected(), PriorStopWordProcess.class);

			StopWordsRemover<S, D, R, C, T> stopWordsRemover = stopWordsRemovers.get(3);
			assertTrue(stopWordsRemover.isApplicable());
 			ZFrame<D, R, C> newZFrame = stopWordsRemover.preprocess(zFrameOriginal);

 			assertTrue(zFrameExpected.except(newZFrame).isEmpty());
			assertTrue(newZFrame.except(zFrameExpected).isEmpty());
	}
	

	@Test
	public void testForOriginalDataAfterPostProcess() throws Exception {

			ZFrame<D, R, C> zFrameOriginal = dfObjectUtil.getDFFromObjectList(StopWordsData.getData4original(), PriorStopWordProcess.class);
			ZFrame<D, R, C> zFrameExpected = dfObjectUtil.getDFFromObjectList(StopWordsData.getData4Expected(), PostStopWordProcess.class);

			ZFrame<D, R, C> newZFrame = context.getDSUtil().postprocess(zFrameExpected, zFrameOriginal);

			assertTrue(newZFrame.select(ColName.ID_COL, "field1", "field2", "field3", ColName.SOURCE_COL).except(zFrameOriginal).isEmpty());
			assertTrue(zFrameOriginal.except(newZFrame.select(ColName.ID_COL, "field1", "field2", "field3", ColName.SOURCE_COL)).isEmpty());
	}

	private List<StopWordsRemover<S, D, R, C, T>> getStopWordsRemovers() throws ZinggClientException {
		stopWordRemoverUtility.buildStopWordRemovers();
		return stopWordRemoverUtility.getStopWordsRemovers();
	}

	/* 
	@Test
	public void testOriginalDataAfterPostProcessLinked() throws Exception {

			ZFrame<D, R, C> zFrameOriginal = dfObjectUtil.getDFFromObjectList(TestStopWordsData.getData5Original(), PriorStopWordProcess.class);
			ZFrame<D, R, C> zFrameExpected = dfObjectUtil.getDFFromObjectList(TestStopWordsData.getData5Actual(), PostStopWordProcess.class);
			
			ZFrame<D, R, C> newZFrame = context.getDSUtil().postprocessLinked(zFrameExpected, zFrameOriginal);
			
			assertTrue(newZFrame.select("field1", "field2", "field3").except(zFrameOriginal.select("field1", "field2", "field3")).isEmpty());
			assertTrue(zFrameOriginal.select("field1", "field2", "field3").except(newZFrame.select("field1", "field2", "field3")).isEmpty());
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

			Dataset<Row> original = sparkSession.createDataFrame(
					Arrays.asList(
							RowFactory.create("10", "The zingg is a spark application", "two",
									"Yes. a good application", "test"),
							RowFactory.create("20", "It is very popular in data science", "Three", "true indeed",
									"test"),
							RowFactory.create("30", "It is written in java and scala", "four", "", "test"),
							RowFactory.create("40", "Best of luck to zingg", "Five", "thank you", "test")),
					schemaOriginal);

			Dataset<Row> actual = sparkSession.createDataFrame(
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
			
			Dataset<Row> newDataset = ((SparkFrame)(new LinkOutputBuilder(zinggSparkContext.getDSUtil(), args).postprocessLinked(new SparkFrame(actual), new SparkFrame(original)))).df();
			
			System.out.println("testOriginalDataAfterPostprocessLinked newDataset :");
			newDataset.show(200);
			
			assertTrue(newDataset.select("field1", "field2", "field3").except(original.select("field1", "field2", "field3")).isEmpty());
			assertTrue(original.select("field1", "field2", "field3").except(newDataset.select("field1", "field2", "field3")).isEmpty());
	}
	*/

}
