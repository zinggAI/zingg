package zingg.common.core.pairs;

import zingg.common.client.ZFrame;
import zingg.common.client.model.IInputData;

public interface IPairBuilder<S, D, R, C> {

	public ZFrame<D, R, C> getPairs(IInputData<D,R,C> blocked, IInputData<D,R,C>bAll) throws Exception;
	
}
