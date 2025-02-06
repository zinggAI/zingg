package zingg.common.core.preprocess.caseNormalize;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import zingg.common.client.FieldDefinition;
import zingg.common.client.MatchTypes;
import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;
import zingg.common.core.context.IContext;
import zingg.common.core.preprocess.IMultiFieldPreprocessor;

import java.util.ArrayList;
import java.util.List;

public abstract class CaseNormalizer<S,D,R,C,T> implements IMultiFieldPreprocessor<S,D,R,C,T> {

    private static final long serialVersionUID = 1L;
    private static final String STRING_TYPE = "string";
    protected static String name = "zingg.common.core.preprocess.caseNormalize.CaseNormalizer";
    public static final Log LOG = LogFactory.getLog(CaseNormalizer.class);

    private IContext<S, D, R, C, T> context;
    private List<? extends FieldDefinition> fieldDefinitions;

    public CaseNormalizer() {
        super();
    }

    public CaseNormalizer(IContext<S, D, R, C, T> context, List<? extends FieldDefinition> fieldDefinitions) {
        this.context = context;
        this.fieldDefinitions = fieldDefinitions;
    }

    @Override
    public void setContext(IContext<S, D, R, C, T> c) {
        this.context = c;
    }

    @Override
    public void init() {

    }

    @Override
    public IContext<S, D, R, C, T> getContext() {
        return this.context;
    }

    @Override
    public boolean isApplicable() {
        return true;
    }

    @Override
    public ZFrame<D, R, C> preprocess(ZFrame<D, R, C> df) throws ZinggClientException {
        try {
            LOG.info("Applying case normalization on input dataframe");
            List<String> relevantFields = getRelevantFields();
            return applyCaseNormalizer(df, relevantFields);
        } catch (Exception exception) {
            LOG.warn("Error occurred while performing case normalization, skipping it");
        }
        return df;
    }

    @Override
    public void setFieldDefinitions(List<? extends FieldDefinition> fieldDefinitions) {
        this.fieldDefinitions = fieldDefinitions;
    }

    @Override
    public List<? extends FieldDefinition> getFieldDefinitions() {
        return this.fieldDefinitions;
    }

    private List<String> getRelevantFields() {
        List<String> stringFields = new ArrayList<>();
        for (FieldDefinition fieldDefinition : fieldDefinitions) {
            if (fieldDefinition.dataType != null && fieldDefinition.matchType != null &&
                    fieldDefinition.dataType.equalsIgnoreCase(STRING_TYPE) && !fieldDefinition.matchType.contains(MatchTypes.DONT_USE)) {
                stringFields.add(fieldDefinition.fieldName);
            }
        }
        return stringFields;
    }

    protected abstract ZFrame<D, R, C> applyCaseNormalizer(ZFrame<D, R, C> incomingDataFrame, List<String> relevantFields);
}
