package zingg.common.core.preprocess.trim;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import zingg.common.client.FieldDefinition;
import zingg.common.client.MatchTypes;
import zingg.common.client.ZFrame;
import zingg.common.core.context.IContext;
import zingg.common.core.preprocess.IMultiFieldPreprocessor;

public abstract class TrimPreprocessor<S,D,R,C,T> implements IMultiFieldPreprocessor<S,D,R,C,T> {

    private static final long serialVersionUID = 1L;
    private static final String STRING_TYPE = "string";
    protected static String name = "zingg.common.core.preprocess.trim.TrimPreprocessor";
    public static final Log LOG = LogFactory.getLog(TrimPreprocessor.class);

    private IContext<S, D, R, C, T> context;
    private List<? extends FieldDefinition> fieldDefinitions;

    public TrimPreprocessor() {
        super();
    }

    public TrimPreprocessor(IContext<S, D, R, C, T> context, List<? extends FieldDefinition> fieldDefinitions) {
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
    public ZFrame<D, R, C> preprocess(ZFrame<D, R, C> df) {
        try {
            LOG.info("Applying trim preprocess on input dataframe");
            List<String> relevantFields = getRelevantFields(STRING_TYPE);
            return applyTrimPreprocess(df, relevantFields);
        } catch (Exception exception) {
            LOG.warn("Error occurred while performing trim preprocess, skipping it, " + exception);
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

    protected abstract ZFrame<D, R, C> applyTrimPreprocess(ZFrame<D, R, C> incomingDataFrame, List<String> relevantFields);
    
}
