package zingg.common.core.preprocess.trim;

import java.util.List;

import zingg.common.client.FieldDefinition;
import zingg.common.client.util.DFObjectUtil;
import zingg.common.core.context.Context;
import zingg.common.core.context.IContext;

public abstract class TestTrimPreprocess<S, D, R, C, T> {

    private final DFObjectUtil<S, D, R, C> dfObjectUtil;
    private final Context<S, D, R, C, T> context;

    public TestTrimPreprocess(DFObjectUtil<S, D, R, C> dfObjectUtil, Context<S, D, R, C, T> context) {
        this.dfObjectUtil = dfObjectUtil;
        this.context = context;
    }

    
    
    protected abstract TrimPreprocessor<S, D, R, C, T> getTrimPreprocessor(IContext<S, D, R, C, T> context, List<? extends FieldDefinition> fieldDefinitions);
}
