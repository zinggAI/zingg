package zingg.common.client.data;

import zingg.common.client.ZFrame;

public abstract class AGenericData<D, R, C> implements IData<D, R, C> {

    protected ZFrame<D, R, C> data;

    public AGenericData(ZFrame<D, R, C> data) {
        this.data = data;
    }

    public void setData(ZFrame<D, R, C> data) {
        this.data = data;
    }

    public ZFrame<D, R, C> getData() {
        return this.data;
    }

    @Override
    public InputType getInputType() {
        return InputType.SINGLE;
    }
}
