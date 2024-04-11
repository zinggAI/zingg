package zingg.common.core.data.df;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import zingg.common.client.IArguments;
import zingg.common.client.ZFrame;
import zingg.common.client.util.ColName;

public class RepartitionFrame<S, D, R, C, T> implements IZFrameProcessor<S, D, R, C, T> {

	protected ZFrame<D,R,C> originalDF;
	
	protected ZFrame<D,R,C> processedDF;
	
	protected IArguments args;
	
	public static final Log LOG = LogFactory.getLog(RepartitionFrame.class);   
	
	public RepartitionFrame(ZFrame<D, R, C> originalDF, IArguments args) {
		super();
		this.originalDF = originalDF;
		this.args = args;
		this.processedDF = getOriginalDF().repartition(args.getNumPartitions(),getOriginalDF().col(ColName.ID_COL));		
	}

	@Override
	public ZFrame<D, R, C> getOriginalDF() {
		return originalDF;
	}

	@Override
	public ZFrame<D, R, C> getProcessedDF() {
		return processedDF;
	}

}
