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
import zingg.common.core.util.CaseNormalizerUtility;

public abstract class TestCaseNormalizer<S, D, R, C, T> {

    private final DFObjectUtil<S, D, R, C> dfObjectUtil;
    private final CaseNormalizerUtility<S, D, R, C, T> caseNormalizerUtility;
    private final Context<S, D, R, C, T> context;

    public TestCaseNormalizer(DFObjectUtil<S, D, R, C> dfObjectUtil, CaseNormalizerUtility<S, D, R, C, T> caseNormalizerUtility, Context<S, D, R, C, T> context) {
        this.dfObjectUtil = dfObjectUtil;
        this.caseNormalizerUtility = caseNormalizerUtility;
        this.context = context;
    }

    @Test
    public void testCaseNormalizationWhenAllFieldsString() throws Exception, ZinggClientException {

        List<List<FieldDefinition>> caseNormalizersFields = getCaseNormalizersFields();
        List<FieldDefinition> fieldDefinitions = caseNormalizersFields.get(0);

        ZFrame<D,R,C> inputDF = dfObjectUtil.getDFFromObjectList(EventTestData.getDataInputPreCaseNormalization(), InputDataModel.class);
        ZFrame<D,R,C> expectedDF = dfObjectUtil.getDFFromObjectList(EventTestData.getDataInputPostCaseNormalizationAllFields(), InputDataModel.class);

        ZFrame<D, R, C> caseNormalizedDF = getCaseNormalizedDF(getCaseNormalizer(context, fieldDefinitions), inputDF);

        assertTrue(caseNormalizedDF.except(expectedDF).isEmpty());
        assertTrue(expectedDF.except(caseNormalizedDF).isEmpty());
    }

    @Test
    public void testCaseNormalizationWhenSomeFieldsString() throws Exception, ZinggClientException {

        List<List<FieldDefinition>> caseNormalizersFields = getCaseNormalizersFields();
        List<FieldDefinition> fieldDefinitions = caseNormalizersFields.get(1);

        ZFrame<D,R,C> inputDF = dfObjectUtil.getDFFromObjectList(EventTestData.getDataInputPreCaseNormalization(), InputDataModel.class);
        ZFrame<D,R,C> expectedDF = dfObjectUtil.getDFFromObjectList(EventTestData.getDataInputPostCaseNormalizationField1(), InputDataModel.class);

        ZFrame<D, R, C> caseNormalizedDF = getCaseNormalizedDF(getCaseNormalizer(context, fieldDefinitions), inputDF);

        assertTrue(caseNormalizedDF.except(expectedDF).isEmpty());
        assertTrue(expectedDF.except(caseNormalizedDF).isEmpty());
    }

    @Test
    public void testCaseNormalizationWhenNoneFieldsString() throws Exception, ZinggClientException {

        List<List<FieldDefinition>> caseNormalizersFields = getCaseNormalizersFields();
        List<FieldDefinition> fieldDefinitions = caseNormalizersFields.get(2);

        ZFrame<D,R,C> inputDF = dfObjectUtil.getDFFromObjectList(EventTestData.getDataInputPreCaseNormalization(), InputDataModel.class);
        ZFrame<D,R,C> expectedDF = dfObjectUtil.getDFFromObjectList(EventTestData.getDataInputPostCaseNormalizationNone(), InputDataModel.class);

        ZFrame<D, R, C> caseNormalizedDF = getCaseNormalizedDF(getCaseNormalizer(context, fieldDefinitions), inputDF);

        assertTrue(caseNormalizedDF.except(expectedDF).isEmpty());
        assertTrue(expectedDF.except(caseNormalizedDF).isEmpty());
    }

    @Test
    public void testCaseNormalizationWhenNoneFieldsStringAndDONT_USEMatchType() throws Exception, ZinggClientException {

        List<List<FieldDefinition>> caseNormalizersFields = getCaseNormalizersFields();
        List<FieldDefinition> fieldDefinitions = caseNormalizersFields.get(3);

        ZFrame<D,R,C> inputDF = dfObjectUtil.getDFFromObjectList(EventTestData.getDataInputPreCaseNormalization(), InputDataModel.class);
        ZFrame<D,R,C> expectedDF = dfObjectUtil.getDFFromObjectList(EventTestData.getDataInputPostCaseNormalizationWhenMatchTypeDONT_USE(), InputDataModel.class);

        ZFrame<D, R, C> caseNormalizedDF = getCaseNormalizedDF(getCaseNormalizer(context, fieldDefinitions), inputDF);

        assertTrue(caseNormalizedDF.except(expectedDF).isEmpty());
        assertTrue(expectedDF.except(caseNormalizedDF).isEmpty());
    }

    private ZFrame<D, R, C> getCaseNormalizedDF(CaseNormalizer<S, D, R, C, T> caseNormalizer, ZFrame<D, R, C> inputDF) throws ZinggClientException {
        return caseNormalizer.preprocess(inputDF);
    }

    protected abstract CaseNormalizer<S, D, R, C, T> getCaseNormalizer(IContext<S, D, R, C, T> context, List<? extends FieldDefinition> fieldDefinitions);

    private List<List<FieldDefinition>> getCaseNormalizersFields() throws ZinggClientException {
		caseNormalizerUtility.buildCaseNormailzersFields();
		return caseNormalizerUtility.getCaseNormalizersFields();
	}
}
