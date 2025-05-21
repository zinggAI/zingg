package zingg.common.core.executor.processunit.impl;

import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;
import zingg.common.client.cols.ISelectedCols;
import zingg.common.core.executor.processunit.IDataProcessUnit;

public class ColsSelectorUnit<D, R, C> implements IDataProcessUnit<D, R, C> {

    private final ISelectedCols colsSelector;

    public ColsSelectorUnit(ISelectedCols colsSelector) {
        this.colsSelector = colsSelector;
    }

    @Override
    public ZFrame<D, R, C> process(ZFrame<D, R, C> data) throws ZinggClientException, Exception {
        return data.select(colsSelector.getCols());
    }
}
