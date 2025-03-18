package zingg.common.client.data;

import zingg.common.client.ZFrame;

public interface IData<D, R, C> {
    ZFrame<D, R, C> getData();
    InputType getInputType();
}
