package zingg.common.client.model;

import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;

import java.util.List;

public class BlockedData<D, R, C> extends MatchInputData<D, R, C> {

    public BlockedData(List<ZFrame<D, R, C>> inputs) {
        super(inputs);
    }

    public BlockedData(ZFrame<D, R, C> input) {
        super(input);
    }

    public ZFrame<D, R, C> getBlockedRecords() throws ZinggClientException {
        return getInput();
    }
}
