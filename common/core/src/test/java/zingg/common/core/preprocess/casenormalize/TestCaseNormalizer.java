package zingg.common.core.preprocess.casenormalize;

import org.junit.jupiter.api.Test;
import zingg.common.client.FieldDefinition;
import zingg.common.client.MatchTypes;
import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;
import zingg.common.client.util.DFObjectUtil;
import zingg.common.core.context.Context;
import zingg.common.core.context.IContext;
import zingg.common.core.data.EventTestData;
import zingg.common.core.model.InputDataModel;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

public abstract class TestCaseNormalizer<S, D, R, C, T> {

    private final DFObjectUtil<S, D, R, C> dfObjectUtil;
    private final Context<S, D, R, C, T> context;

    public TestCaseNormalizer(DFObjectUtil<S, D, R, C> dfObjectUtil, Context<S, D, R, C, T> context) {
        this.dfObjectUtil = dfObjectUtil;
        this.context = context;
    }

    @Test
    public void testCaseNormalizationWhenAllFieldsString() throws Exception, ZinggClientException {
        ZFrame<D,R,C> inputDF = dfObjectUtil.getDFFromObjectList(EventTestData.getDataInputPreCaseNormalization(), InputDataModel.class);
        ZFrame<D,R,C> expectedDF = dfObjectUtil.getDFFromObjectList(EventTestData.getDataInputPostCaseNormalizationAllFields(), InputDataModel.class);

        FieldDefinition fieldDefinition1 = new FieldDefinition();
        fieldDefinition1.setFieldName("field1");
        fieldDefinition1.setMatchType(List.of(MatchTypes.FUZZY));
        fieldDefinition1.setDataType("STRING");

        FieldDefinition fieldDefinition2 = new FieldDefinition();
        fieldDefinition2.setFieldName("field2");
        fieldDefinition2.setMatchType(List.of(MatchTypes.FUZZY));
        fieldDefinition2.setDataType("STRING");

        FieldDefinition fieldDefinition3 = new FieldDefinition();
        fieldDefinition3.setFieldName("field3");
        fieldDefinition3.setMatchType(List.of(MatchTypes.FUZZY));
        fieldDefinition3.setDataType("STRING");

        List<FieldDefinition> fieldDefinitions = new ArrayList<FieldDefinition>(List.of(fieldDefinition1, fieldDefinition2, fieldDefinition3));

        ZFrame<D, R, C> caseNormalizedDF = getCaseNormalizedDF(getCaseNormalizer(context, fieldDefinitions), inputDF);

        assertTrue(caseNormalizedDF.except(expectedDF).isEmpty());
        assertTrue(expectedDF.except(caseNormalizedDF).isEmpty());
    }

    @Test
    public void testCaseNormalizationWhenSomeFieldsString() throws Exception, ZinggClientException {
        ZFrame<D,R,C> inputDF = dfObjectUtil.getDFFromObjectList(EventTestData.getDataInputPreCaseNormalization(), InputDataModel.class);
        ZFrame<D,R,C> expectedDF = dfObjectUtil.getDFFromObjectList(EventTestData.getDataInputPostCaseNormalizationField1(), InputDataModel.class);

        List<FieldDefinition> fieldDefinitions = new ArrayList<FieldDefinition>();

        FieldDefinition fieldDefinition1 = new FieldDefinition();
        fieldDefinition1.setFieldName("field1");
        fieldDefinition1.setMatchType(List.of(MatchTypes.FUZZY));
        fieldDefinition1.setDataType("STRING");

        fieldDefinitions.add(fieldDefinition1);

        ZFrame<D, R, C> caseNormalizedDF = getCaseNormalizedDF(getCaseNormalizer(context, fieldDefinitions), inputDF);

        assertTrue(caseNormalizedDF.except(expectedDF).isEmpty());
        assertTrue(expectedDF.except(caseNormalizedDF).isEmpty());
    }

    @Test
    public void testCaseNormalizationWhenNoneFieldsString() throws Exception, ZinggClientException {
        ZFrame<D,R,C> inputDF = dfObjectUtil.getDFFromObjectList(EventTestData.getDataInputPreCaseNormalization(), InputDataModel.class);
        ZFrame<D,R,C> expectedDF = dfObjectUtil.getDFFromObjectList(EventTestData.getDataInputPostCaseNormalizationNone(), InputDataModel.class);

        List<FieldDefinition> fieldDefinitions = new ArrayList<FieldDefinition>();

        ZFrame<D, R, C> caseNormalizedDF = getCaseNormalizedDF(getCaseNormalizer(context, fieldDefinitions), inputDF);

        assertTrue(caseNormalizedDF.except(expectedDF).isEmpty());
        assertTrue(expectedDF.except(caseNormalizedDF).isEmpty());
    }

    @Test
    public void testCaseNormalizationWhenNoneFieldsStringAndDONT_USEMatchType() throws Exception, ZinggClientException {
        ZFrame<D,R,C> inputDF = dfObjectUtil.getDFFromObjectList(EventTestData.getDataInputPreCaseNormalization(), InputDataModel.class);
        ZFrame<D,R,C> expectedDF = dfObjectUtil.getDFFromObjectList(EventTestData.getDataInputPostCaseNormalizationWhenMatchTypeDONT_USE(), InputDataModel.class);

        FieldDefinition fieldDefinition1 = new FieldDefinition();
        fieldDefinition1.setFieldName("field1");
        fieldDefinition1.setMatchType(List.of(MatchTypes.DONT_USE));
        fieldDefinition1.setDataType("STRING");

        FieldDefinition fieldDefinition2 = new FieldDefinition();
        fieldDefinition2.setFieldName("field2");
        fieldDefinition2.setMatchType(List.of(MatchTypes.FUZZY));
        fieldDefinition2.setDataType("STRING");

        FieldDefinition fieldDefinition3 = new FieldDefinition();
        fieldDefinition3.setFieldName("field3");
        fieldDefinition3.setMatchType(List.of(MatchTypes.DONT_USE));
        fieldDefinition3.setDataType("STRING");

        List<FieldDefinition> fieldDefinitions = new ArrayList<FieldDefinition>(List.of(fieldDefinition1, fieldDefinition2, fieldDefinition3));
        ZFrame<D, R, C> caseNormalizedDF = getCaseNormalizedDF(getCaseNormalizer(context, fieldDefinitions), inputDF);

        assertTrue(caseNormalizedDF.except(expectedDF).isEmpty());
        assertTrue(expectedDF.except(caseNormalizedDF).isEmpty());
    }

    private ZFrame<D, R, C> getCaseNormalizedDF(CaseNormalizer<S, D, R, C, T> caseNormalizer, ZFrame<D, R, C> inputDF) throws ZinggClientException {
        return caseNormalizer.preprocess(inputDF);
    }

    protected abstract CaseNormalizer<S, D, R, C, T> getCaseNormalizer(IContext<S, D, R, C, T> context, List<? extends FieldDefinition> fieldDefinitions);
}
