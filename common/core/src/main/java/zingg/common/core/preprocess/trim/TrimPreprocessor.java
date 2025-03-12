package zingg.common.core.preprocess.trim;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import zingg.common.client.FieldDefinition;
import zingg.common.core.context.IContext;
import zingg.common.core.preprocess.MultiFieldPreprocessor;

public abstract class TrimPreprocessor<S,D,R,C,T> extends MultiFieldPreprocessor<S,D,R,C,T> {

    private static final long serialVersionUID = 1L;
    protected static String name = "zingg.common.core.preprocess.trim.TrimPreprocessor";
    public static final Log LOG = LogFactory.getLog(TrimPreprocessor.class);

    public TrimPreprocessor() {
        super();
    }

    public TrimPreprocessor(IContext<S, D, R, C, T> context, List<? extends FieldDefinition> fieldDefinitions) {
        super(context,fieldDefinitions);
    }
    
}
