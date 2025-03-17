package zingg.common.client.model;

import zingg.common.client.ZFrame;

public class MatchInputData<D, R, C> implements IInputData<D, R, C> {

    private ZFrame<D, R, C> input;

    public MatchInputData(ZFrame<D, R, C> input) {
        this.input = input;
    }

    public void setInput(ZFrame<D, R, C> input) {
        this.input = input;
    }

    public ZFrame<D, R, C> getInput() {
        return this.input;
    }

    @Override
    public ZFrame<D, R, C> getTotalInput() {
        return this.input;
    }
}
