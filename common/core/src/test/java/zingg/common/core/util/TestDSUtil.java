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
import zingg.common.core.util.data.TestDSUtilData;
import zingg.common.core.util.model.TestShowConciseData;
import zingg.common.core.util.model.TestTrainingData;
import zingg.common.core.util.model.TrainingSamplesData;

public abstract class TestDSUtil<S, D, R, C, T> {

    public static final Log LOG = LogFactory.getLog(TestDSUtil.class);
    protected final DFObjectUtil<S, D, R, C> dfObjectUtil;
	protected final Context<S, D, R, C, T> context;

	public TestDSUtil(DFObjectUtil<S, D, R, C> dfObjectUtil, Context<S, D, R, C, T> context) throws ZinggClientException {
		this.dfObjectUtil = dfObjectUtil;
		this.context = context;
	}
	

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

        ZFrame<D,R,C> ds = dfObjectUtil.getDFFromObjectList(TestDSUtilData.getFieldDefnDataForShowConcise(), TestShowConciseData.class);

		List<String> expectedColumns = new ArrayList<String>();
		expectedColumns.add("field_fuzzy");
		expectedColumns.add(ColName.SOURCE_COL);

		List<C> colList = context.getDSUtil().getFieldDefColumns(ds, args, false, true);
		List<String> expectedColList = new ArrayList<String>();
		for (int i = 0; i < colList.size(); i++) {
			String s = colList.get(i).toString();
			expectedColList.add(i,s);
		};

		assertIterableEquals(expectedColumns, expectedColList);
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

		ZFrame<D,R,C> ds = dfObjectUtil.getDFFromObjectList(TestDSUtilData.getFieldDefnDataForShowConcise(), TestShowConciseData.class);

		List<String> expectedColumnsTest2 = new ArrayList<String>();
		expectedColumnsTest2.add("field_fuzzy");
		expectedColumnsTest2.add("field_match_type_DONT_USE");
		expectedColumnsTest2.add("field_str_DONTspaceUSE");
		expectedColumnsTest2.add(ColName.SOURCE_COL);

		List<C> colListTest2 = context.getDSUtil().getFieldDefColumns (ds, args, false, false);
		List<String> expectedColList2 = new ArrayList<String>();
		for (int i = 0; i < colListTest2.size(); i++) {
			String s = colListTest2.get(i).toString();
			expectedColList2.add(i,s);
		};

		assertIterableEquals(expectedColumnsTest2, expectedColList2);

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

		ZFrame<D,R,C> trFile1 = dfObjectUtil.getDFFromObjectList(TestDSUtilData.getTrainingFile(), TestTrainingData.class);
		
		when(pipeUtil.read(false, false, p)).thenReturn(trFile1);

		//when training samples are null
		ZFrame<D,R,C> trainingData1 = context.getDSUtil().getTraining(pipeUtil, args, p);
		trFile1 = trFile1.drop(ColName.PREDICTION_COL);
		trFile1 = trFile1.drop(ColName.SCORE_COL);
		trainingData1.show();
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

		ZFrame<D,R,C> trFile1 = dfObjectUtil.getDFFromObjectList(TestDSUtilData.getTrainingFile(), TestTrainingData.class);
		ZFrame<D,R,C> trSamples1 = dfObjectUtil.getDFFromObjectList(TestDSUtilData.getTrainingSamplesData(), TrainingSamplesData.class);
		
		when(pipeUtil.read(false, false, p)).thenReturn(trFile1);
		when(pipeUtil.read(true, false, args.getTrainingSamples())).thenReturn(trSamples1);

		ZFrame<D,R,C> trainingData1 = context.getDSUtil().getTraining(pipeUtil, args, p);
		trFile1 = trFile1.drop(ColName.PREDICTION_COL);
		trFile1 = trFile1.drop(ColName.SCORE_COL);
		ZFrame<D,R,C> expTrainingData = trFile1.unionByName(trSamples1, true);
		trainingData1.show();
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
		ZFrame<D,R,C> trSamples1 = dfObjectUtil.getDFFromObjectList(TestDSUtilData.getTrainingSamplesData(), TrainingSamplesData.class);
		
		//training data is null 
		when(pipeUtil.read(false, false, p)).thenThrow(ZinggClientException.class);
		when(pipeUtil.read(true, false, args.getTrainingSamples())).thenReturn(trSamples1);

		ZFrame<D,R,C> trainingData1 = context.getDSUtil().getTraining(pipeUtil, args, p);
		trainingData1.show();
		assertEquals(trainingData1.count(), 2);
		assertTrue(trainingData1.except(trSamples1).isEmpty());
		assertTrue(trSamples1.except(trainingData1).isEmpty());
	
	
	}
}
    

