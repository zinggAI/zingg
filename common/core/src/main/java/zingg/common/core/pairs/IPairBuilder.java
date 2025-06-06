package zingg.common.core.pairs;

import zingg.common.client.ZFrame;

public interface IPairBuilder<S, D, R, C> {

	public ZFrame<D, R, C> getPairs(ZFrame<D,R,C>blocked, ZFrame<D,R,C>bAll) throws Exception;
	
}
