package zingg.common.core.data.df;

import java.io.Serializable;

import zingg.common.client.ZFrame;

public interface IZFrameEnriched<D, R, C> extends Serializable{

	public ZFrame<D,R,C> getOriginalDF();
	
	public ZFrame<D,R,C> getProcessedDF();

}
