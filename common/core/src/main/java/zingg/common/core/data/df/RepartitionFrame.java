package zingg.common.core.data.df;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import zingg.common.client.IArguments;
import zingg.common.client.ZFrame;

public class RepartitionFrame<D, R, C> extends AbstractZFrameProcessor<D, R, C> {

	protected IArguments args;
	
	protected int numPartitions;
	
	protected String partitionCol;
	
	public static final Log LOG = LogFactory.getLog(RepartitionFrame.class);   
	
	public RepartitionFrame(ZFrame<D, R, C> originalDF, int numPartitions, String partitionCol) {
		super();
		this.originalDF = originalDF;
		this.numPartitions = numPartitions;
		this.partitionCol = partitionCol;	
	}

	@Override
	public void process() {
		this.processedDF = getOriginalDF().repartition(numPartitions,getOriginalDF().col(partitionCol));		
	}
	
}
