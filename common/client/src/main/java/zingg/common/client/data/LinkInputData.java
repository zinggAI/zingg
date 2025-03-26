package zingg.common.client.data;

import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;

import java.util.List;

public class LinkInputData<D, R, C> extends AMultiInputData<D, R, C> {

    public LinkInputData(List<ZFrame<D, R, C>> inputs) throws ZinggClientException {
        super(inputs);
    }

    @Override
    protected void setInputs(List<ZFrame<D, R, C>> inputs) throws ZinggClientException {
        setLinkerInputs(inputs);
    }

    private void setLinkerInputs(List<ZFrame<D, R, C>> inputs) throws ZinggClientException {
        //TODO what if data contains > 2 pipes
        if (inputs.size() >= 2){
            setPrimaryInput(inputs.get(0));
            setSecondaryInput(inputs.get(1));
        } else {
            throw new ZinggClientException("Excepted at-least two inputs for linker");
        }
    }
}
