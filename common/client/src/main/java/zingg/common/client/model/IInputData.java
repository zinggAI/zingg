package zingg.common.client.model;

import zingg.common.client.ZFrame;

public interface IInputData<D, R, C> {
    ZFrame<D, R, C> getTotalInput();
}
