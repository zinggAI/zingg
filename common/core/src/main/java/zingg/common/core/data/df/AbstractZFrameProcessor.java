package zingg.common.core.data.df;

import zingg.common.client.ZFrame;

public abstract class AbstractZFrameProcessor<D, R, C> implements IZFrameProcessor<D, R, C> {
	protected ZFrame<D,R,C> originalDF;
	
	protected ZFrame<D,R,C> processedDF;
	
	@Override
	public ZFrame<D, R, C> getOriginalDF() {
		return originalDF;
	}

	@Override
	public ZFrame<D, R, C> getProcessedDF() {
		return processedDF;
	}
	

}
