package zingg.common.client.data;

import zingg.common.client.ZFrame;

public class MatchInputData<D, R, C> extends GenericData<D, R, C> {

    public MatchInputData(ZFrame<D, R, C> data) {
        super(data);
    }
}
