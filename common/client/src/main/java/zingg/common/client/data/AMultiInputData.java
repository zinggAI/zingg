package zingg.common.client.data;

import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;

import java.util.List;

public abstract class AMultiInputData<D, R, C> implements IData<D, R, C> {

    private ZFrame<D, R, C> primaryInput;
    private ZFrame<D, R, C> secondaryInput;

    public AMultiInputData(List<ZFrame<D, R, C>> inputs) throws ZinggClientException {
        setInputs(inputs);
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

    @Override
    public ZFrame<D, R, C> getData() {
        return primaryInput.union(secondaryInput);
    }

    @Override
    public InputType getInputType() {
        return InputType.MULTI;
    }

    protected abstract void setInputs(List<ZFrame<D, R, C>> inputs) throws ZinggClientException;
}
