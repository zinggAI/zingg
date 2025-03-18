package zingg.common.core.preprocess.casenormalize;

import java.util.List;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

import zingg.common.client.FieldDefinition;
import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;
import zingg.common.client.util.DFObjectUtil;
import zingg.common.core.context.Context;
import zingg.common.core.context.IContext;
import zingg.common.core.model.model.InputDataModel;
import zingg.common.core.preprocess.casenormalize.data.EventTestData;
import zingg.common.core.util.AMultiFieldPreprocessorUtility;


public abstract class TestCaseNormalizer<S, D, R, C, T> {

    private final DFObjectUtil<S, D, R, C> dfObjectUtil;
    private final AMultiFieldPreprocessorUtility<S, D, R, C, T> multiFieldPreprocessorUtility;
    private final Context<S, D, R, C, T> context;

    public TestCaseNormalizer(DFObjectUtil<S, D, R, C> dfObjectUtil, AMultiFieldPreprocessorUtility<S, D, R, C, T> multiFieldPreprocessorUtility, Context<S, D, R, C, T> context) throws ZinggClientException {
        this.dfObjectUtil = dfObjectUtil;
        this.multiFieldPreprocessorUtility = multiFieldPreprocessorUtility;
        this.context = context;
        multiFieldPreprocessorUtility.buildFieldDefinitions();
    }

    @Test
    public void testCaseNormalizationWhenAllFieldsString() throws Exception, ZinggClientException {

        List<FieldDefinition> fieldDefinitions = multiFieldPreprocessorUtility.getFieldDefinitionsWhenAllFieldsString();

        ZFrame<D,R,C> inputDF = dfObjectUtil.getDFFromObjectList(EventTestData.getDataInputPreCaseNormalization(), InputDataModel.class);
        ZFrame<D,R,C> expectedDF = dfObjectUtil.getDFFromObjectList(EventTestData.getDataInputPostCaseNormalizationAllFields(), InputDataModel.class);
        ZFrame<D, R, C> caseNormalizedDF = getCaseNormalizedDF(getCaseNormalizer(context, fieldDefinitions), inputDF);

        assertTrue(caseNormalizedDF.except(expectedDF).isEmpty());
        assertTrue(expectedDF.except(caseNormalizedDF).isEmpty());
    }

    @Test
    public void testCaseNormalizationWhenSomeFieldsString() throws Exception, ZinggClientException {

        List<FieldDefinition> fieldDefinitions = multiFieldPreprocessorUtility.getFieldDefinitionsWhenSomeFieldsString();
             
        ZFrame<D,R,C> inputDF = dfObjectUtil.getDFFromObjectList(EventTestData.getDataInputPreCaseNormalization(), InputDataModel.class);
        ZFrame<D,R,C> expectedDF = dfObjectUtil.getDFFromObjectList(EventTestData.getDataInputPostCaseNormalizationField1(), InputDataModel.class);
        ZFrame<D, R, C> caseNormalizedDF = getCaseNormalizedDF(getCaseNormalizer(context, fieldDefinitions), inputDF);
    
        assertTrue(caseNormalizedDF.except(expectedDF).isEmpty());
        assertTrue(expectedDF.except(caseNormalizedDF).isEmpty());
    }

    @Test
    public void testCaseNormalizationWhenNoneFieldsString() throws Exception, ZinggClientException {

        List<FieldDefinition> fieldDefinitions = multiFieldPreprocessorUtility.getFieldDefinitionsWhenNoneFieldsString();
        
        ZFrame<D,R,C> inputDF = dfObjectUtil.getDFFromObjectList(EventTestData.getDataInputPreCaseNormalization(), InputDataModel.class);
        ZFrame<D,R,C> expectedDF = dfObjectUtil.getDFFromObjectList(EventTestData.getDataInputPostCaseNormalizationNone(), InputDataModel.class);
        ZFrame<D, R, C> caseNormalizedDF = getCaseNormalizedDF(getCaseNormalizer(context, fieldDefinitions), inputDF);

        assertTrue(caseNormalizedDF.except(expectedDF).isEmpty());
        assertTrue(expectedDF.except(caseNormalizedDF).isEmpty());
    }

    @Test
    public void testCaseNormalizationWhenSingleFieldStringAndDont_UseMatchType() throws Exception, ZinggClientException {

        List<FieldDefinition> fieldDefinitions = multiFieldPreprocessorUtility.getFieldDefinitionsWhenSingleFieldStringAndDont_UseMatchType();

        ZFrame<D,R,C> inputDF = dfObjectUtil.getDFFromObjectList(EventTestData.getDataInputPreCaseNormalization(), InputDataModel.class);
        ZFrame<D,R,C> expectedDF = dfObjectUtil.getDFFromObjectList(EventTestData.getDataInputPostCaseNormalizationWhenMatchTypeDont_Use(), InputDataModel.class);
        ZFrame<D, R, C> caseNormalizedDF = getCaseNormalizedDF(getCaseNormalizer(context, fieldDefinitions), inputDF);

        assertTrue(caseNormalizedDF.except(expectedDF).isEmpty());
        assertTrue(expectedDF.except(caseNormalizedDF).isEmpty());
    }

    private ZFrame<D, R, C> getCaseNormalizedDF(CaseNormalizer<S, D, R, C, T> caseNormalizer, ZFrame<D, R, C> inputDF) throws ZinggClientException {
        caseNormalizer.init();
        return caseNormalizer.preprocess(inputDF);
    }

    protected abstract CaseNormalizer<S, D, R, C, T> getCaseNormalizer(IContext<S, D, R, C, T> context, List<? extends FieldDefinition> fieldDefinitions);

}
