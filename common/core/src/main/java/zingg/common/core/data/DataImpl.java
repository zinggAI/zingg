package zingg.common.core.data;

import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;
import zingg.common.core.executor.processunit.IDataProcessor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/*
    This class can stand on its own,
    not required as abstract
 */
public class DataImpl<D, R, C> implements IData<D, R, C> {

    protected final List<ZFrame<D, R, C>> data;
    private String name;

    public DataImpl(List<ZFrame<D, R, C>> data) {
        this.data = data;
    }

    @SafeVarargs
    public DataImpl(ZFrame<D, R, C>... inputZFrames) {
        this.data = new ArrayList<>();
        this.data.addAll(Arrays.asList(inputZFrames));
    }

    public void addData(ZFrame<D, R, C> inputZFrame) {
        this.data.add(inputZFrame);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public ZFrame<D, R, C> getByIndex(int index) {
        ZFrame<D, R, C> indexedZFrame = null;
        if (data != null && index < data.size()) {
            indexedZFrame = data.get(index);
        }
        return indexedZFrame;
    }

    public List<ZFrame<D, R, C>> getData() {
        return this.data;
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
    public IData<D, R, C> cache() {
        List<ZFrame<D, R, C>> cachedZFrames = new ArrayList<>();
        for (ZFrame<D, R, C> zFrame : data) {
            cachedZFrames.add(zFrame.cache());
        }
        return new DataImpl<>(cachedZFrames);
    }

    @Override
    public IData<D, R, C> compute(IDataProcessor<D, R, C> dataProcessUnit) throws ZinggClientException, Exception {
        List<ZFrame<D, R, C>> computedZFrames = new ArrayList<>();
        for (ZFrame<D, R, C> zFrame : data) {
            computedZFrames.add(dataProcessUnit.process(zFrame));
        }
        return new DataImpl<D, R, C>(computedZFrames);
    }

}
