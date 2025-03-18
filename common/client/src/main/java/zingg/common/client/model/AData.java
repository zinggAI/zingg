package zingg.common.client.model;

import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;

import java.util.List;

public abstract class AData<D, R, C> {

    protected List<ZFrame<D, R, C>> data;

    public AData() {
    }

    public AData(List<ZFrame<D, R, C>> data) {
        this.data = data;
    }

    public ZFrame<D, R, C> getCombinedData() throws ZinggClientException {
        if (!data.isEmpty()) {
            ZFrame<D, R, C> combinedInput = data.get(0);

            for(int idx = 1; idx < data.size(); idx++){
                combinedInput = combinedInput.union(data.get(idx));
            }
            return combinedInput;
        }
        throw new ZinggClientException("No input data found");
    }
}
