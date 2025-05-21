package zingg.common.core.executor.processunit.impl;

import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;
import zingg.common.core.executor.processunit.IDataProcessUnit;
import zingg.common.core.preprocess.IPreprocessors;

public class PreprocessorProcessingUnit<S, D, R, C, T> implements IDataProcessUnit<D, R, C> {

    private final IPreprocessors<S, D, R, C, T> preprocessors;

    public PreprocessorProcessingUnit(IPreprocessors<S, D, R, C, T> preprocessors) {
        this.preprocessors = preprocessors;
    }

    @Override
    public ZFrame<D, R, C> process(ZFrame<D, R, C> data) throws ZinggClientException, Exception {
        return preprocessors.preprocess(data);
    }
}
