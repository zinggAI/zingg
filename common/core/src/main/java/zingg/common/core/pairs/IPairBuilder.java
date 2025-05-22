package zingg.common.core.pairs;

import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;
import zingg.common.core.data.IData;

public interface IPairBuilder<S, D, R, C> {
	ZFrame<D, R, C> getPairs(IData<D, R, C> blocked, IData<D, R, C> bAll) throws Exception, ZinggClientException;
}
