package zingg.common.core.data.df;

import zingg.common.client.ZFrame;

public interface IZFrameProcessor<S, D, R, C, T> {

	public ZFrame<D,R,C> getOriginalDF();
	
	public ZFrame<D,R,C> getProcessedDF();
	
}
