package zingg.common.core.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.Test;

import zingg.common.client.Arguments;
import zingg.common.client.FieldDefinition;
import zingg.common.client.IArguments;
import zingg.common.client.MatchType;
import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;
import zingg.common.client.pipe.Pipe;
import zingg.common.client.util.ColName;
import zingg.common.client.util.DFObjectUtil;
import zingg.common.client.util.IModelHelper;
import zingg.common.client.util.PipeUtilBase;
import zingg.common.core.context.Context;
import zingg.common.core.util.data.DSUtilData;
import zingg.common.core.util.model.FieldDefnForShowConciseData;
import zingg.common.core.util.model.TrainingData;
import zingg.common.core.util.model.TrainingSamplesData;

public abstract class TestDSUtil<S, D, R, C, T> {

    public static final Log LOG = LogFactory.getLog(TestDSUtil.class);
    protected DFObjectUtil<S, D, R, C> dfObjectUtil;
	protected Context<S, D, R, C, T> context;

	public TestDSUtil(){
	}
	
	public void initialize(DFObjectUtil<S, D, R, C> dfObjectUtil, Context<S, D, R, C, T> context) throws ZinggClientException {
		this.dfObjectUtil = dfObjectUtil;
		this.context = context;
	}

	public abstract List<String> getColNames(List<C> col);

	public abstract List<String> getExpectedColNames(List<String> col);
	

	@Test
	public void testGetFieldDefColumnsWhenShowConciseIsTrue() throws ZinggClientException, Exception {
		
		FieldDefinition def1 = new FieldDefinition();
		def1.setFieldName("field_fuzzy");
		def1.setDataType("string");
		def1.setMatchTypeInternal(MatchType.FUZZY);
		def1.setFields("field_fuzzy");

		FieldDefinition def2 = new FieldDefinition();
		def2.setFieldName("field_match_type_DONT_USE");
		def2.setDataType("string");
		def2.setMatchTypeInternal(MatchType.DONT_USE);
		def2.setFields("field_match_type_DONT_USE");

		FieldDefinition def3 = new FieldDefinition();
		def3.setFieldName("field_str_DONTspaceUSE");
		def3.setDataType("string");
		def3.setMatchTypeInternal(MatchType.getMatchType("DONT_USE"));
		def3.setFields("field_str_DONTspaceUSE");

		List<FieldDefinition> fieldDef = new ArrayList<FieldDefinition>();
		fieldDef.add(def1);
		fieldDef.add(def2);
		fieldDef.add(def3);

		IArguments args = null; 
		args = new Arguments();
		args.setFieldDefinition(fieldDef);

        ZFrame<D,R,C> ds = dfObjectUtil.getDFFromObjectList(DSUtilData.getFieldDefnDataForShowConcise(), FieldDefnForShowConciseData.class);

		List<String> expectedColumns = new ArrayList<String>();
		expectedColumns.add("field_fuzzy");
		expectedColumns.add(ColName.SOURCE_COL);
		List<String> expectedColumnsList = getExpectedColNames(expectedColumns);

		List<C> colList = context.getDSUtil().getFieldDefColumns(ds, args, false, true);
		List<String> expectedColList = getColNames(colList);

		assertIterableEquals(expectedColumnsList, expectedColList);
	}

	@Test
	public void testGetFieldDefColumnsWhenShowConciseIsFalse() throws ZinggClientException, Exception {
		FieldDefinition def1 = new FieldDefinition();
		def1.setFieldName("field_fuzzy");
		def1.setDataType("string");
		def1.setMatchTypeInternal(MatchType.FUZZY);
		def1.setFields("field_fuzzy");

		FieldDefinition def2 = new FieldDefinition();
		def2.setFieldName("field_match_type_DONT_USE");
		def2.setDataType("string");
		def2.setMatchTypeInternal(MatchType.DONT_USE);
		def2.setFields("field_match_type_DONT_USE");

		FieldDefinition def3 = new FieldDefinition();
		def3.setFieldName("field_str_DONTspaceUSE");
		def3.setDataType("string");
		def3.setMatchTypeInternal(MatchType.getMatchType("DONT_USE"));
		def3.setFields("field_str_DONTspaceUSE");

		List<FieldDefinition> fieldDef = new ArrayList<FieldDefinition>();
		fieldDef.add(def1);
		fieldDef.add(def2);
		fieldDef.add(def3);

		IArguments args = null; 
		args = new Arguments();
		args.setFieldDefinition(fieldDef);

		ZFrame<D,R,C> ds = dfObjectUtil.getDFFromObjectList(DSUtilData.getFieldDefnDataForShowConcise(), FieldDefnForShowConciseData.class);

		List<String> expectedColumnsTest2 = new ArrayList<String>();
		expectedColumnsTest2.add("field_fuzzy");
		expectedColumnsTest2.add("field_match_type_DONT_USE");
		expectedColumnsTest2.add("field_str_DONTspaceUSE");
		expectedColumnsTest2.add(ColName.SOURCE_COL);
		List<String> expectedColumnsTestList2 = getExpectedColNames(expectedColumnsTest2);

		List<C> colListTest2 = context.getDSUtil().getFieldDefColumns (ds, args, false, false);
		List<String> expectedColList2 = getColNames(colListTest2);

		assertIterableEquals(expectedColumnsTestList2, expectedColList2);

	}

	@Test
	public void testGetTrainingDataWhenTrainingSamplesIsNull() throws Exception, ZinggClientException{
	
		FieldDefinition def1 = new FieldDefinition();
		def1.setFieldName("field1");
		def1.setDataType("string");
		def1.setMatchTypeInternal(MatchType.FUZZY);
		def1.setFields("field1");

		List<FieldDefinition> fieldDef = new ArrayList<FieldDefinition>();
		fieldDef.add(def1);

		IArguments args = null; 
		args = new Arguments();
		args.setFieldDefinition(fieldDef);
        args.setTrainingSamples(null);

		PipeUtilBase<S,D,R,C> pipeUtil = mock(PipeUtilBase.class);
		IModelHelper modelHelper = mock(IModelHelper.class);

		Pipe<D,R,C> p = new Pipe<>();
		when(modelHelper.getTrainingDataMarkedPipe(args)).thenReturn(p);

		ZFrame<D,R,C> trFile1 = dfObjectUtil.getDFFromObjectList(DSUtilData.getTrainingFile(), TrainingData.class);
		
		when(pipeUtil.read(false, false, p)).thenReturn(trFile1);

		//when training samples are null
		ZFrame<D,R,C> trainingData1 = context.getDSUtil().getTraining(pipeUtil, args, p);
		trFile1 = trFile1.drop(ColName.PREDICTION_COL);
		trFile1 = trFile1.drop(ColName.SCORE_COL);
		
		assertTrue(trainingData1.except(trFile1).isEmpty());
		assertTrue(trFile1.except(trainingData1).isEmpty());

	}

	@Test
	public void testGetTrainingDataWhenTrainingSamplesIsNotNull() throws Exception, ZinggClientException{
	
		FieldDefinition def1 = new FieldDefinition();
		def1.setFieldName("field1");
		def1.setDataType("string");
		def1.setMatchTypeInternal(MatchType.FUZZY);
		def1.setFields("field1");

		List<FieldDefinition> fieldDef = new ArrayList<FieldDefinition>();
		fieldDef.add(def1);

		IArguments args = null; 
		args = new Arguments();
		args.setFieldDefinition(fieldDef);

		//setting training samples
		Pipe[] trainingSamples = new Pipe[2];
		args.setTrainingSamples(trainingSamples);

		PipeUtilBase<S,D,R,C> pipeUtil = mock(PipeUtilBase.class);
		IModelHelper modelHelper = mock(IModelHelper.class);

		Pipe<D,R,C> p = new Pipe<>();
		when(modelHelper.getTrainingDataMarkedPipe(args)).thenReturn(p);

		ZFrame<D,R,C> trFile1 = dfObjectUtil.getDFFromObjectList(DSUtilData.getTrainingFile(), TrainingData.class);
		ZFrame<D,R,C> trSamples1 = dfObjectUtil.getDFFromObjectList(DSUtilData.getTrainingSamplesData(), TrainingSamplesData.class);
		
		when(pipeUtil.read(false, false, p)).thenReturn(trFile1);
		when(pipeUtil.read(true, false, args.getTrainingSamples())).thenReturn(trSamples1);

		ZFrame<D,R,C> trainingData1 = context.getDSUtil().getTraining(pipeUtil, args, p);
		trFile1 = trFile1.drop(ColName.PREDICTION_COL);
		trFile1 = trFile1.drop(ColName.SCORE_COL);
		ZFrame<D,R,C> expTrainingData = trFile1.unionByName(trSamples1, true);
		
		assertEquals(trainingData1.count(), 6);
		assertTrue(trainingData1.except(expTrainingData).isEmpty());
		assertTrue(expTrainingData.except(trainingData1).isEmpty());

	}

	@Test
	public void testGetTrainingDataWhenTrainingDataIsNull() throws Exception, ZinggClientException{
	
		FieldDefinition def1 = new FieldDefinition();
		def1.setFieldName("field1");
		def1.setDataType("string");
		def1.setMatchTypeInternal(MatchType.FUZZY);
		def1.setFields("field1");

		List<FieldDefinition> fieldDef = new ArrayList<FieldDefinition>();
		fieldDef.add(def1);

		IArguments args = null; 
		args = new Arguments();
		args.setFieldDefinition(fieldDef);

		//setting training samples
		Pipe[] trainingSamples = new Pipe[2];
		args.setTrainingSamples(trainingSamples);

		PipeUtilBase<S,D,R,C> pipeUtil = mock(PipeUtilBase.class);
		Pipe<D,R,C> p = new Pipe<>();
		ZFrame<D,R,C> trSamples1 = dfObjectUtil.getDFFromObjectList(DSUtilData.getTrainingSamplesData(), TrainingSamplesData.class);
		
		//training data is null 
		when(pipeUtil.read(false, false, p)).thenThrow(ZinggClientException.class);
		when(pipeUtil.read(true, false, args.getTrainingSamples())).thenReturn(trSamples1);

		ZFrame<D,R,C> trainingData1 = context.getDSUtil().getTraining(pipeUtil, args, p);
		
		assertEquals(trainingData1.count(), 2);
		assertTrue(trainingData1.except(trSamples1).isEmpty());
		assertTrue(trSamples1.except(trainingData1).isEmpty());
	
	
	}
}
    

