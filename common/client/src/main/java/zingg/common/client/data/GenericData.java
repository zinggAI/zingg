package zingg.common.client.data;

import zingg.common.client.ZFrame;

import java.util.List;

/*
    This class can stand on its own,
    not required as abstract
 */
public class GenericData<D, R, C> implements IData<D, R, C> {

    protected List<ZFrame<D, R, C>> data;

    public GenericData(List<ZFrame<D, R, C>> data) {
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

}
