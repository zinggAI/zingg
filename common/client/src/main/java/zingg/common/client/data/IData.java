package zingg.common.client.data;

import zingg.common.client.ZFrame;

import java.util.List;

public interface IData<D, R, C> {
    List<ZFrame<D, R, C>> getData();
    InputType getInputType();
    long count();
}
