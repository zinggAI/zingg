package zingg.common.client.model;

import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;

import java.util.List;

public class LinkInputData<D, R, C> extends AData<D, R, C> {

    private ZFrame<D, R, C> inputOne;
    private ZFrame<D, R, C> inputTwo;

    public LinkInputData(List<ZFrame<D, R, C>> inputs) throws ZinggClientException {
        super(inputs);
        setLinkerInputs(inputs);
    }

    public void setInputOne(ZFrame<D, R, C> inputOne) {
        this.inputOne = inputOne;
    }

    public void setInputTwo(ZFrame<D, R, C> inputTwo) {
        this.inputTwo = inputTwo;
    }

    public ZFrame<D, R, C> getInputOne() {
        return this.inputOne;
    }

    public ZFrame<D, R, C> getInputTwo() {
        return inputTwo;
    }

    private void setLinkerInputs(List<ZFrame<D, R, C>> inputs) throws ZinggClientException {
        //TODO what if data contains > 2 pipes
        if (inputs.size() >= 2){
            setInputOne(inputs.get(0));
            setInputTwo(inputs.get(1));
        } else {
            throw new ZinggClientException("Excepted at-least two inputs for linker");
        }
    }

}
