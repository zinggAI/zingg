package zingg.common.core.preprocess;

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

			List<StopWordsRemover<S, D, R, C, T>> stopWordsRemovers = getStopWordsRemovers();
			String stopWords = "\\b(a|an|the|is|It|of|yes|no|I|has|have|you)\\b\\s?".toLowerCase();

			ZFrame<D, R, C> zFrameOriginal = dfObjectUtil.getDFFromObjectList(StopWordsData.getData1Original(), Statement.class);
			ZFrame<D, R, C> zFrameExpected = dfObjectUtil.getDFFromObjectList(StopWordsData.getData1Expected(), Statement.class);

			StopWordsRemover<S, D, R, C, T> stopWordsRemover = stopWordsRemovers.get(0);
			
			stopWordsRemover.preprocessForStopWords(zFrameOriginal);
			ZFrame<D, R, C> newZFrame = stopWordsRemover.removeStopWordsFromDF(zFrameOriginal,"statement",stopWords);

 			assertTrue(zFrameExpected.except(newZFrame).isEmpty());
			assertTrue(newZFrame.except(zFrameExpected).isEmpty());
	}

	@Test
	public void testRemoveStopWordsFromDataset() throws ZinggClientException, Exception {

			List<StopWordsRemover<S, D, R, C, T>> stopWordsRemovers = getStopWordsRemovers();

			ZFrame<D, R, C> zFrameOriginal = dfObjectUtil.getDFFromObjectList(StopWordsData.getData2Original(), PriorStopWordProcess.class);
			ZFrame<D, R, C> zFrameExpected = dfObjectUtil.getDFFromObjectList(StopWordsData.getData2Expected(), PriorStopWordProcess.class);

			StopWordsRemover<S, D, R, C, T> stopWordsRemover = stopWordsRemovers.get(1);
			ZFrame<D, R, C> newZFrame = stopWordsRemover.preprocessForStopWords(zFrameOriginal);
				
 			assertTrue(zFrameExpected.except(newZFrame).isEmpty());
			assertTrue(newZFrame.except(zFrameExpected).isEmpty());
	}

	@Test
	public void testStopWordColumnMissingFromStopWordFile() throws ZinggClientException, Exception {

			List<StopWordsRemover<S, D, R, C, T>> stopWordsRemovers = getStopWordsRemovers();

			ZFrame<D, R, C> zFrameOriginal = dfObjectUtil.getDFFromObjectList(StopWordsData.getData3Original(), PriorStopWordProcess.class);
			ZFrame<D, R, C> zFrameExpected = dfObjectUtil.getDFFromObjectList(StopWordsData.getData3Expected(), PriorStopWordProcess.class);

			StopWordsRemover<S, D, R, C, T> stopWordsRemover = stopWordsRemovers.get(2);
 			ZFrame<D, R, C> newZFrame = stopWordsRemover.preprocessForStopWords(zFrameOriginal);

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

	/* 
	@Test
	public void testOriginalDataAfterPostProcessLinked() throws Exception {

			ZFrame<D, R, C> zFrameOriginal = dfObjectUtil.getDFFromObjectList(TestStopWordsData.getData5Original(), PriorStopWordProcess.class);
			ZFrame<D, R, C> zFrameExpected = dfObjectUtil.getDFFromObjectList(TestStopWordsData.getData5Actual(), PostStopWordProcess.class);
			
			ZFrame<D, R, C> newZFrame = context.getDSUtil().postprocessLinked(zFrameExpected, zFrameOriginal);
			
			assertTrue(newZFrame.select("field1", "field2", "field3").except(zFrameOriginal.select("field1", "field2", "field3")).isEmpty());
			assertTrue(zFrameOriginal.select("field1", "field2", "field3").except(newZFrame.select("field1", "field2", "field3")).isEmpty());
	}
	*/

	private List<StopWordsRemover<S, D, R, C, T>> getStopWordsRemovers() throws ZinggClientException {
		stopWordRemoverUtility.buildStopWordRemovers();
		return stopWordRemoverUtility.getStopWordsRemovers();
	}

}