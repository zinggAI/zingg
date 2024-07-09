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
import zingg.common.client.ArgumentsUtil;
import zingg.common.client.FieldDefinition;
import zingg.common.client.IArguments;
import zingg.common.client.MatchType;
import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;
import zingg.common.client.util.ColName;
import zingg.common.client.util.DFObjectUtil;
import zingg.common.core.context.Context;
import zingg.common.core.data.Constant;
import zingg.common.core.model.Event;
import zingg.common.core.model.Schema;
import zingg.common.core.model.SchemaActual;
import zingg.common.core.model.SchemaOriginal;
import zingg.spark.client.SparkFrame;
import zingg.spark.core.context.ZinggSparkContext;
import zingg.spark.core.executor.ZinggSparkTester;
import zingg.spark.core.preprocess.SparkStopWordsRemover;

public abstract class TestStopWords<S, D, R, C, T> {

	public static final Log LOG = LogFactory.getLog(TestStopWords.class);
	private final DFObjectUtil<S, D, R, C> dfObjectUtil;
	private final List<StopWordsRemover<S, D, R, C, T>> stopWordsRemovers;
	private final Context<S, D, R, C, T> context;

	public TestStopWords(DFObjectUtil<S, D, R, C> dfObjectUtil, List<StopWordsRemover<S, D, R, C, T>> stopWordsRemovers,
						 Context<S, D, R, C, T> context) {
		this.dfObjectUtil = dfObjectUtil;
		this.stopWordsRemovers = stopWordsRemovers;
		this.context = context;
	}

	@DisplayName ("Test Stop Words removal from Single column dataset")
	@Test
	public void testStopWordsSingleColumn() throws ZinggClientException, Exception {

			String stopWords = "\\b(a|an|the|is|It|of|yes|no|I|has|have|you)\\b\\s?".toLowerCase();

			ZFrame<D, R, C> zFrameOriginal = dfObjectUtil.getDFFromObjectList(Constant.getData1Original(), Schema.class);
			ZFrame<D, R, C> zFrameExpected = dfObjectUtil.getDFFromObjectList(Constant.getData1Expected(), Schema.class);

			StopWordsRemover<S, D, R, C, T> stopWordsRemover = stopWordsRemovers.get(0);
			
			stopWordsRemover.preprocessForStopWords(zFrameOriginal);
			ZFrame<D, R, C> newZFrame = stopWordsRemover.removeStopWordsFromDF(zFrameOriginal,"statement",stopWords);

 			assertTrue(zFrameExpected.except(newZFrame).isEmpty());
			assertTrue(newZFrame.except(zFrameExpected).isEmpty());
	}

	@Test
	public void testRemoveStopWordsFromDataset() throws ZinggClientException, Exception {

			ZFrame<D, R, C> zFrameOriginal = dfObjectUtil.getDFFromObjectList(Constant.getData2Original(), SchemaOriginal.class);
			ZFrame<D, R, C> zFrameExpected = dfObjectUtil.getDFFromObjectList(Constant.getData2Expected(), SchemaOriginal.class);

			StopWordsRemover<S, D, R, C, T> stopWordsRemover = stopWordsRemovers.get(1);
			ZFrame<D, R, C> newZFrame = stopWordsRemover.preprocessForStopWords(zFrameOriginal);
				
 			assertTrue(zFrameExpected.except(newZFrame).isEmpty());
			assertTrue(newZFrame.except(zFrameExpected).isEmpty());
	}

	@Test
	public void testStopWordColumnMissingFromStopWordFile() throws ZinggClientException, Exception {

			ZFrame<D, R, C> zFrameOriginal = dfObjectUtil.getDFFromObjectList(Constant.getData3Original(), SchemaOriginal.class);
			ZFrame<D, R, C> zFrameExpected = dfObjectUtil.getDFFromObjectList(Constant.getData3Expected(), SchemaOriginal.class);

			StopWordsRemover<S, D, R, C, T> stopWordsRemover = stopWordsRemovers.get(2);
 			ZFrame<D, R, C> newDataSet = stopWordsRemover.preprocessForStopWords(zFrameOriginal);

 			assertTrue(zFrameExpected.except(newDataSet).isEmpty());
			assertTrue(newDataSet.except(zFrameExpected).isEmpty());
	}
	

	@Test
	public void testForOriginalDataAfterPostProcess() throws Exception {

			ZFrame<D, R, C> zFrameOriginal = dfObjectUtil.getDFFromObjectList(Constant.getData4original(), SchemaOriginal.class);
			ZFrame<D, R, C> zFrameExpected = dfObjectUtil.getDFFromObjectList(Constant.getData4Expected(), SchemaActual.class);

			ZFrame<D, R, C> newZFrame = context.getDSUtil().postprocess(zFrameExpected, zFrameOriginal);

			assertTrue(newZFrame.select(ColName.ID_COL, "field1", "field2", "field3", ColName.SOURCE_COL).except(zFrameOriginal).isEmpty());
			assertTrue(zFrameOriginal.except(newZFrame.select(ColName.ID_COL, "field1", "field2", "field3", ColName.SOURCE_COL)).isEmpty());
	}

	@Test
	public void testOriginalDataAfterPostProcessLinked() throws Exception {

			ZFrame<D, R, C> zFrameOriginal = dfObjectUtil.getDFFromObjectList(Constant.getData5Original(), SchemaOriginal.class);
			ZFrame<D, R, C> zFrameExpected = dfObjectUtil.getDFFromObjectList(Constant.getData5Actual(), SchemaActual.class);
			
			ZFrame<D, R, C> newZFrame = context.getDSUtil().postprocessLinked(zFrameExpected, zFrameOriginal);
			
			assertTrue(newZFrame.select("field1", "field2", "field3").except(zFrameOriginal.select("field1", "field2", "field3")).isEmpty());
			assertTrue(zFrameOriginal.select("field1", "field2", "field3").except(newZFrame.select("field1", "field2", "field3")).isEmpty());
	}

}