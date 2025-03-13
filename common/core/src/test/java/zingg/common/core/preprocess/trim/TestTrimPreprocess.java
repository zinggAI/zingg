package zingg.common.core.preprocess.trim;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import zingg.common.client.FieldDefinition;
import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;
import zingg.common.client.util.DFObjectUtil;
import zingg.common.core.context.Context;
import zingg.common.core.context.IContext;
import zingg.common.core.model.model.InputDataModel;
import zingg.common.core.preprocess.trim.data.TrimTestData;
import zingg.common.core.util.MultiFieldPreprocessorUtility;

public abstract class TestTrimPreprocess<S, D, R, C, T> {

    private final DFObjectUtil<S, D, R, C> dfObjectUtil;
    private final MultiFieldPreprocessorUtility<S, D, R, C, T> multiFieldPreprocessorUtility;
    private final Context<S, D, R, C, T> context;

    public TestTrimPreprocess(DFObjectUtil<S, D, R, C> dfObjectUtil, MultiFieldPreprocessorUtility<S, D, R, C, T> multiFieldPreprocessorUtility, Context<S, D, R, C, T> context) throws ZinggClientException {
        this.dfObjectUtil = dfObjectUtil;
        this.multiFieldPreprocessorUtility = multiFieldPreprocessorUtility;
        this.context = context;
        multiFieldPreprocessorUtility.buildFieldDefinitions();
    }

    @Test
    public void testTrimPreprocessWhenAllFieldsString() throws Exception, ZinggClientException {

        List<FieldDefinition> fieldDefinitions = multiFieldPreprocessorUtility.getFieldDefinitionsWhenAllFieldsString();

        ZFrame<D,R,C> inputDF = dfObjectUtil.getDFFromObjectList(TrimTestData.getDataInputPreTrim(), InputDataModel.class);
        ZFrame<D,R,C> expectedDF = dfObjectUtil.getDFFromObjectList(TrimTestData.getDataInputPostTrimOnAllFields(), InputDataModel.class);
        ZFrame<D, R, C> trimmedDF = getCaseNormalizedAndTrimmedDF(getTrimPreprocessor(context, fieldDefinitions), inputDF);

        assertTrue(trimmedDF.except(expectedDF).isEmpty());
        assertTrue(expectedDF.except(trimmedDF).isEmpty());
    }

    @Test
    public void testTrimPreprocessWhenSomeFieldsString() throws Exception, ZinggClientException {

        List<FieldDefinition> fieldDefinitions = multiFieldPreprocessorUtility.getFieldDefinitionsWhenSomeFieldsString();

        ZFrame<D,R,C> inputDF = dfObjectUtil.getDFFromObjectList(TrimTestData.getDataInputPreTrim(), InputDataModel.class);
        ZFrame<D,R,C> expectedDF = dfObjectUtil.getDFFromObjectList(TrimTestData.getDataInputPostTrimOnField1(), InputDataModel.class);
        ZFrame<D, R, C> trimmedDF = getCaseNormalizedAndTrimmedDF(getTrimPreprocessor(context, fieldDefinitions), inputDF);

        assertTrue(trimmedDF.except(expectedDF).isEmpty());
        assertTrue(expectedDF.except(trimmedDF).isEmpty());
    }

    @Test
    public void testTrimPreprocessWhenNoneFieldsString() throws Exception, ZinggClientException {

        List<FieldDefinition> fieldDefinitions = multiFieldPreprocessorUtility.getFieldDefinitionsWhenNoneFieldsString();

        ZFrame<D,R,C> inputDF = dfObjectUtil.getDFFromObjectList(TrimTestData.getDataInputPreTrim(), InputDataModel.class);
        ZFrame<D,R,C> expectedDF = dfObjectUtil.getDFFromObjectList(TrimTestData.getDataInputPostTrimOnNone(), InputDataModel.class);
        ZFrame<D, R, C> trimmedDF = getCaseNormalizedAndTrimmedDF(getTrimPreprocessor(context, fieldDefinitions), inputDF);

        assertTrue(trimmedDF.except(expectedDF).isEmpty());
        assertTrue(expectedDF.except(trimmedDF).isEmpty());
    }

    @Test
    public void testTrimPreprocessWhenSingleFieldStringAndDont_UseMatchType() throws Exception, ZinggClientException {

        List<FieldDefinition> fieldDefinitions = multiFieldPreprocessorUtility.getFieldDefinitionsWhenSingleFieldStringAndDont_UseMatchType();

        ZFrame<D,R,C> inputDF = dfObjectUtil.getDFFromObjectList(TrimTestData.getDataInputPreTrim(), InputDataModel.class);
        ZFrame<D,R,C> expectedDF = dfObjectUtil.getDFFromObjectList(TrimTestData.getDataInputPostTrimWhenMatchTypeDont_Use(), InputDataModel.class);
        ZFrame<D, R, C> trimmedDF = getCaseNormalizedAndTrimmedDF(getTrimPreprocessor(context, fieldDefinitions), inputDF);

        assertTrue(trimmedDF.except(expectedDF).isEmpty());
        assertTrue(expectedDF.except(trimmedDF).isEmpty());
    }

    private ZFrame<D, R, C> getCaseNormalizedAndTrimmedDF(TrimPreprocessor<S, D, R, C, T> trimPreprocessor, ZFrame<D, R, C> inputDF) throws ZinggClientException {
        return trimPreprocessor.preprocess(inputDF);
    }
    
    protected abstract TrimPreprocessor<S, D, R, C, T> getTrimPreprocessor(IContext<S, D, R, C, T> context, List<? extends FieldDefinition> fieldDefinitions);

}
