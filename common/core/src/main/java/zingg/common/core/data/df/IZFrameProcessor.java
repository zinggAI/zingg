package zingg.common.core.data.df;

import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;

public interface IZFrameProcessor<D, R, C> {

	public ZFrame<D,R,C> getOriginalDF();
	
	public ZFrame<D,R,C> getProcessedDF();
	
	public void process() throws ZinggClientException;
	
}
