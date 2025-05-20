package zingg.common.client.data;

import zingg.common.client.ZFrame;

import java.util.List;

public class MatchInputData<D, R, C> extends GenericData<D, R, C> {
    public MatchInputData(List<ZFrame<D, R, C>> data) {
        super(data);
    }
}
