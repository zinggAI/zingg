package zingg.common.core.filter;

import zingg.common.client.ZFrame;

public interface IFilter<D, R, C> {

	public ZFrame<D, R, C> filter(ZFrame<D, R, C> df);
	
}
