package zingg.common.core.preprocess;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import zingg.common.client.FieldDefinition;
import zingg.common.client.ZFrame;
import zingg.common.core.ZinggException;
import zingg.common.core.context.IContext;

public abstract class MultiFieldPreprocessor<S,D,R,C,T> implements IMultiFieldPreprocessor<S,D,R,C,T> {

    private static final long serialVersionUID = 1L;
    private static final String STRING_TYPE = "string";
    protected List<String> relevantFields = new ArrayList<String>();
    protected static String name = "zingg.common.core.preprocess.MultiFieldPreprocessor";
    public static final Log LOG = LogFactory.getLog(MultiFieldPreprocessor.class);

    private IContext<S, D, R, C, T> context;
    private List<? extends FieldDefinition> fieldDefinitions;

    public MultiFieldPreprocessor() {
        super();
    }

    public MultiFieldPreprocessor(IContext<S, D, R, C, T> context, List<? extends FieldDefinition> fieldDefinitions) {
        this.context = context;
        this.fieldDefinitions = fieldDefinitions;
    }

    @Override
    public void setContext(IContext<S, D, R, C, T> c) {
        this.context = c;
    }

    @Override
    public void init() {
        relevantFields = getRelevantFields(STRING_TYPE);
    }

    @Override
    public IContext<S, D, R, C, T> getContext() {
        return this.context;
    }

    @Override
    public boolean isApplicable() {
        return (relevantFields.size()!= 0);
    }

    @Override
    public ZFrame<D, R, C> preprocess(ZFrame<D, R, C> df) {
        try {
            if(isApplicable()){
                LOG.debug("Applying preprocessor on input dataframe");
                return applyPreprocessor(df, relevantFields);
            }
        } catch (Exception exception) {
            LOG.warn("Error occurred while performing multifield preprocessor" + exception);
            throw new ZinggException("Error occurred while performing multifield preprocessor");
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
    
}
