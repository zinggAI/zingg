package zingg.common.core.preprocess;

import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;

public interface IPreProcessor<S,D,R,C,T> {

	public ZFrame<D, R, C> preprocess(ZFrame<D, R, C> ds) throws ZinggClientException;
	
}
