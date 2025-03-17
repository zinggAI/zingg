package zingg.common.client.model;

import zingg.common.client.ZFrame;

public class LinkInputData<D, R, C> implements IInputData<D, R, C> {

    private ZFrame<D, R, C> inputOne;
    private ZFrame<D, R, C> inputTwo;

    public LinkInputData(ZFrame<D, R, C> inputOne, ZFrame<D, R, C> inputTwo) {
        this.inputOne = inputOne;
        this.inputTwo = inputTwo;
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

    @Override
    public ZFrame<D, R, C> getTotalInput() {
        return inputOne.unionAll(inputTwo);
    }
}
