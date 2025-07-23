package zingg.common.core.preprocess;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.Test;

import zingg.common.client.*;
import zingg.common.client.arguments.model.Arguments;
import zingg.common.client.arguments.model.IArguments;
import zingg.common.client.util.DFObjectUtil;
import zingg.common.core.context.Context;
import zingg.common.core.preprocess.data.PreprocessTestData;
import zingg.common.core.preprocess.model.PriorStopWordProcess;

public abstract class TestPreprocessors<S,D,R,C,T> {

    public static final Log LOG = LogFactory.getLog(TestPreprocessors.class);
	private final DFObjectUtil<S, D, R, C> dfObjectUtil;
	private final Context<S, D, R, C, T> context;

    public TestPreprocessors(DFObjectUtil<S, D, R, C> dfObjectUtil, Context<S, D, R, C, T> context) {
		this.dfObjectUtil = dfObjectUtil;
		this.context = context;
	}
    
    @Test
    public void testPreprocessorsFlow() throws ZinggClientException, Exception{
        IArguments args = new Arguments();

        List<FieldDefinition> fieldDefs = getFieldDefinitions();
        args.setFieldDefinition(fieldDefs);
        
        IPreprocessors<S,D,R,C,T> preprocessors = getPreprocessors(context);
        
        ZFrame<D,R,C> inputDF = dfObjectUtil.getDFFromObjectList(PreprocessTestData.getDataInputPreProcessed(), PriorStopWordProcess.class);
        ZFrame<D,R,C> expectedDF = dfObjectUtil.getDFFromObjectList(PreprocessTestData.getDataInputPostProcessed(), PriorStopWordProcess.class);

        preprocessors.setArgs(args);
        ZFrame<D,R,C> resultDF = preprocessors.preprocess(inputDF);
        
        assertTrue(resultDF.except(expectedDF).isEmpty());
        assertTrue(expectedDF.except(resultDF).isEmpty());
    
    }

    private List<FieldDefinition>  getFieldDefinitions() {
        /*
            only field1 and field3 will be lower cased and trimmed
        */
        List<FieldDefinition> fieldDefs = new ArrayList<FieldDefinition>();
        String stopWordsFileName1 = Objects.requireNonNull(TestPreprocessors.class.getResource("../../../../preProcess/stopwords/stopWords.csv")).getFile();
        FieldDefinition fieldDefinition1 = new FieldDefinition();
        fieldDefinition1.setStopWords(stopWordsFileName1);
        fieldDefinition1.setFieldName("field1");
        fieldDefinition1.setDataType("STRING");
        fieldDefinition1.setMatchType(List.of(MatchTypes.FUZZY));

        FieldDefinition fieldDefinition2 = new FieldDefinition();
        fieldDefinition2.setFieldName("field2");
        fieldDefinition2.setDataType("STRING");
        fieldDefinition2.setMatchType(List.of(MatchTypes.DONT_USE));

        FieldDefinition fieldDefinition3 = new FieldDefinition();
        fieldDefinition3.setFieldName("field3");
        fieldDefinition3.setDataType("STRING");
        fieldDefinition3.setMatchType(List.of(MatchTypes.FUZZY));

        fieldDefs.add(fieldDefinition1);
        fieldDefs.add(fieldDefinition2);
        fieldDefs.add(fieldDefinition3);

        return fieldDefs;
    }

    public abstract IPreprocessors<S,D,R,C,T> getPreprocessors(Context<S, D, R, C, T> context);

}
