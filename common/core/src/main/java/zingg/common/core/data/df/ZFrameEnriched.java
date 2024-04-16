package zingg.common.core.data.df;

import zingg.common.client.ZFrame;

public class ZFrameEnriched<D, R, C> implements IZFrameEnriched<D, R, C> {
	private static final long serialVersionUID = 1L;

	protected ZFrame<D,R,C> originalDF;
	
	protected ZFrame<D,R,C> processedDF;

	public ZFrameEnriched(ZFrame<D, R, C> originalDF) {
		this.originalDF = originalDF;
	}

	public ZFrameEnriched(ZFrame<D, R, C> originalDF, ZFrame<D, R, C> processedDF) {
		this.originalDF = originalDF;
		this.processedDF = processedDF;
	}

	@Override
	public ZFrame<D, R, C> getOriginalDF() {
		return originalDF;
	}

	@Override
	public ZFrame<D, R, C> getProcessedDF() {
		return processedDF;
	}
	
	public void setProcessedDF(ZFrame<D, R, C> processedDF) {
		this.processedDF = processedDF;
	}

}
