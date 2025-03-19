package zingg.common.core.pairs;

import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;
import zingg.common.client.data.BlockedData;
import zingg.common.client.data.IData;

public interface IPairBuilder<S, D, R, C> {

	ZFrame<D, R, C> getPairs(BlockedData<D, R, C>[] blocked, IData<D, R, C> bAll) throws Exception, ZinggClientException;
}
