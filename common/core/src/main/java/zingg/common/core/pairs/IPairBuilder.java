package zingg.common.core.pairs;

import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;
import zingg.common.client.model.AData;

public interface IPairBuilder<S, D, R, C> {

	public ZFrame<D, R, C> getPairs(AData<D,R,C> blocked, AData<D,R,C> bAll) throws Exception, ZinggClientException;
	
}
