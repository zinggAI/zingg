package zingg.common.core.executor.processunit.impl;

import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;
import zingg.common.client.cols.ZidAndFieldDefSelector;
import zingg.common.core.executor.processunit.IDataProcessUnit;

public class ZidAndFieldSelectorUnit<D, R, C> implements IDataProcessUnit<D, R, C> {

    private final ZidAndFieldDefSelector zidAndFieldDefSelector;

    public ZidAndFieldSelectorUnit(ZidAndFieldDefSelector zidAndFieldDefSelector) {
        this.zidAndFieldDefSelector = zidAndFieldDefSelector;
    }

    @Override
    public ZFrame<D, R, C> process(ZFrame<D, R, C> data) throws ZinggClientException, Exception {
        return data.select(zidAndFieldDefSelector.getCols());
    }
}
