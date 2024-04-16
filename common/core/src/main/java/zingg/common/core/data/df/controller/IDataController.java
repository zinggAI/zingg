package zingg.common.core.data.df.controller;

import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;

public interface IDataController<D,R,C> {

	public ZFrame<D,R,C> process(ZFrame<D,R,C> originalDF) throws ZinggClientException;	
}
