package zingg.common.core.data;

import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;
import zingg.common.core.executor.processunit.IDataProcessUnit;

import java.util.ArrayList;
import java.util.List;

/*
    This class can stand on its own,
    not required as abstract
 */
public class IDataImpl<D, R, C> implements IData<D, R, C> {

    protected List<ZFrame<D, R, C>> data;

    public IDataImpl(List<ZFrame<D, R, C>> data) {
        this.data = data;
    }

    public void setData(List<ZFrame<D, R, C>> data) {
        this.data = data;
    }

    public List<ZFrame<D, R, C>> getData() {
        return this.data;
    }

    @Override
    public InputType getInputType() {
        return InputType.SINGLE;
    }

    @Override
    public long count() {
        long count = 0;
        for (ZFrame<D, R, C> zFrame : data) {
            count += zFrame.count();;
        }
        return count;
    }

    @Override
    public IData<D, R, C> compute(IDataProcessUnit<D, R, C> dataProcessUnit) throws ZinggClientException, Exception {
        List<ZFrame<D, R, C>> computedZFrames = new ArrayList<>();
        for (ZFrame<D, R, C> zFrame : data) {
            computedZFrames.add(dataProcessUnit.process(zFrame));
        }
        return new IDataImpl<>(computedZFrames);
    }

}
