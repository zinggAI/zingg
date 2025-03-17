package zingg.common.core.executor;

import zingg.common.client.IArguments;
import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;
import zingg.common.client.model.LinkInputData;
import zingg.common.client.util.PipeUtilBase;
import zingg.common.core.match.data.IDataGetter;

public class LinkDataGetter<S, D, R, C> implements IDataGetter<S, D, R, C> {

    @Override
    public LinkInputData<D, R, C> getData(IArguments arg, PipeUtilBase<S, D, R, C> p) throws ZinggClientException {
        ZFrame<D, R, C> sourceOneInput = p.read(true, true, arg.getNumPartitions(), true, arg.getData()[0]);
        ZFrame<D, R, C> sourceTwoInput = p.read(true, true, arg.getNumPartitions(), true, arg.getData()[1]);

        return new LinkInputData<D, R, C>(sourceOneInput, sourceTwoInput);
    }
}
