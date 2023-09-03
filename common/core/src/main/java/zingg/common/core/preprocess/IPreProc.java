package zingg.common.core.preprocess;

import zingg.common.client.Arguments;
import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;

public interface IPreProc<S,D,R,C,T>  {

	public ZFrame<D,R,C> preprocess(S session, Arguments args, ZFrame<D,R,C> ds) throws ZinggClientException;
	
}
