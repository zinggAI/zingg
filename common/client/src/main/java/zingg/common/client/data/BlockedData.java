package zingg.common.client.data;

import zingg.common.client.ZFrame;

public class BlockedData<D, R, C> extends GenericData<D, R, C> {
    public BlockedData(ZFrame<D, R, C> data) {
        super(data);
    }
}
