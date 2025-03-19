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
import zingg.common.core.preprocess.stopwords.data.StopWordsData;
import zingg.common.core.preprocess.stopwords.model.PostStopWordProcess;
import zingg.common.core.preprocess.stopwords.model.PriorStopWordProcess;
import zingg.common.core.preprocess.stopwords.model.Statement;
import zingg.common.core.util.AStopWordRemoverUtility;

public abstract class TestStopWordsBase<S, D, R, C, T> {

	public static final Log LOG = LogFactory.getLog(TestStopWordsBase.class);
	private final DFObjectUtil<S, D, R, C> dfObjectUtil;
	private final AStopWordRemoverUtility<S, D, R, C, T> stopWordRemoverUtility;
	private final Context<S, D, R, C, T> context;


	public TestStopWordsBase(DFObjectUtil<S, D, R, C> dfObjectUtil, AStopWordRemoverUtility<S, D, R, C, T> stopWordRemoverUtility, Context<S, D, R, C, T> context) {
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

			ZFrame<D, R, C> zFrameOriginal = dfObjectUtil.getDFFromObjectList(StopWordsData.getDataPreStopWordsSingleColumn(), Statement.class);
			ZFrame<D, R, C> zFrameExpected = dfObjectUtil.getDFFromObjectList(StopWordsData.getDataPostStopWordsSingleColumn(), Statement.class);

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

			ZFrame<D, R, C> zFrameOriginal = dfObjectUtil.getDFFromObjectList(StopWordsData.getDataPreStopWordsPreprocess(), PriorStopWordProcess.class);
			ZFrame<D, R, C> zFrameExpected = dfObjectUtil.getDFFromObjectList(StopWordsData.getDataPostStopWordsPreprocess(), PriorStopWordProcess.class);

			StopWordsRemover<S, D, R, C, T> stopWordsRemover = stopWordsRemovers.get(1);
			assertTrue(stopWordsRemover.isApplicable());
			ZFrame<D, R, C> newZFrame = stopWordsRemover.preprocess(zFrameOriginal);
				
 			assertTrue(zFrameExpected.except(newZFrame).isEmpty());
			assertTrue(newZFrame.except(zFrameExpected).isEmpty());
	}

	@Test
	public void testStopWordHeaderMissingFromStopWordFile() throws ZinggClientException, Exception {

			//check functionality of preprocess on dataset with header in csv as Header - dummy to ensure it is being ignored by default
			List<StopWordsRemover<S, D, R, C, T>> stopWordsRemovers = getStopWordsRemovers();

			ZFrame<D, R, C> zFrameOriginal = dfObjectUtil.getDFFromObjectList(StopWordsData.getDataPreStopWordsPreprocess(), PriorStopWordProcess.class);
			ZFrame<D, R, C> zFrameExpected = dfObjectUtil.getDFFromObjectList(StopWordsData.getDataPostStopWordsPreprocess(), PriorStopWordProcess.class);

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

			ZFrame<D, R, C> zFrameOriginal = dfObjectUtil.getDFFromObjectList(StopWordsData.getDataPreStopWordsPreprocess(), PriorStopWordProcess.class);
			ZFrame<D, R, C> zFrameExpected = dfObjectUtil.getDFFromObjectList(StopWordsData.getDataPostStopWordsPreprocess(), PriorStopWordProcess.class);

			StopWordsRemover<S, D, R, C, T> stopWordsRemover = stopWordsRemovers.get(3);
			assertTrue(stopWordsRemover.isApplicable());
 			ZFrame<D, R, C> newZFrame = stopWordsRemover.preprocess(zFrameOriginal);

 			assertTrue(zFrameExpected.except(newZFrame).isEmpty());
			assertTrue(newZFrame.except(zFrameExpected).isEmpty());
	}
	

	@Test
	public void testForOriginalDataAfterPostProcess() throws Exception {

			ZFrame<D, R, C> zFrameOriginal = dfObjectUtil.getDFFromObjectList(StopWordsData.getDataBeforePostProcessLinked(), PriorStopWordProcess.class);
			ZFrame<D, R, C> zFrameExpected = dfObjectUtil.getDFFromObjectList(StopWordsData.getDataAfterPostProcessLinked(), PostStopWordProcess.class);

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

			ZFrame<D, R, C> zFrameOriginal = dfObjectUtil.getDFFromObjectList(TestStopWordsData.getDataBeforePostProcessLinked(), PriorStopWordProcess.class);
			ZFrame<D, R, C> zFrameExpected = dfObjectUtil.getDFFromObjectList(TestStopWordsData.getDataAfterPostProcessLinked(), PostStopWordProcess.class);
			
			ZFrame<D, R, C> newZFrame = context.getDSUtil().postprocessLinked(zFrameExpected, zFrameOriginal);
			
			assertTrue(newZFrame.select("field1", "field2", "field3").except(zFrameOriginal.select("field1", "field2", "field3")).isEmpty());
			assertTrue(zFrameOriginal.select("field1", "field2", "field3").except(newZFrame.select("field1", "field2", "field3")).isEmpty());
	}
	*/

}
