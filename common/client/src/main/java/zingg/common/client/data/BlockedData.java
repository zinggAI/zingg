package zingg.common.client.data;

import zingg.common.client.ZFrame;

import java.util.List;

public class BlockedData<D, R, C> extends GenericData<D, R, C> {
    public BlockedData(List<ZFrame<D, R, C>> data) {
        super(data);
    }
}
