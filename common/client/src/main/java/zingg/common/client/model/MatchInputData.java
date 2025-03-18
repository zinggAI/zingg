package zingg.common.client.model;

import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;

import java.util.ArrayList;
import java.util.List;

public class MatchInputData<D, R, C> extends AData<D, R, C> {

    public MatchInputData(List<ZFrame<D, R, C>> inputs) {
        super(inputs);
    }

    public MatchInputData(ZFrame<D, R, C> input) {
        setInput(input);
    }

    public void setInput(ZFrame<D, R, C> input) {
        data = new ArrayList<ZFrame<D, R, C>>();
        data.add(input);
    }

    public ZFrame<D, R, C> getInput() throws ZinggClientException {
        return getCombinedData();
    }

}
