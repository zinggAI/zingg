package zingg.common.client.data;

import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;

import java.util.List;

public class LinkInputData<D, R, C> implements IData<D, R, C> {

    private ZFrame<D, R, C> primaryInput;
    private ZFrame<D, R, C> secondaryInput;

    public LinkInputData(List<ZFrame<D, R, C>> inputs) throws ZinggClientException {
        setLinkerInputs(inputs);
    }

    public void setPrimaryInput(ZFrame<D, R, C> primaryInput) {
        this.primaryInput = primaryInput;
    }

    public void setSecondaryInput(ZFrame<D, R, C> inputTwo) {
        this.secondaryInput = inputTwo;
    }

    public ZFrame<D, R, C> getPrimaryInput() {
        return this.primaryInput;
    }

    public ZFrame<D, R, C> getSecondaryInput() {
        return secondaryInput;
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

    @Override
    public ZFrame<D, R, C> getData() {
        return primaryInput.union(secondaryInput);
    }

    @Override
    public InputType getInputType() {
        return InputType.MULTI;
    }
}
