package zingg.common.core.data.df.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import zingg.common.client.IArguments;
import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;

public class RepartitionDataController<D, R, C> implements IDataController<D, R, C> {

	protected IArguments args;
	
	protected int numPartitions;
	
	protected String partitionCol;
	
	public static final Log LOG = LogFactory.getLog(RepartitionDataController.class);   
	
	public RepartitionDataController(int numPartitions, String partitionCol) {
		this.numPartitions = numPartitions;
		this.partitionCol = partitionCol;	
	}

	@Override
	public ZFrame<D, R, C> process(ZFrame<D,R,C> originalDF) throws ZinggClientException {
		return originalDF.repartition(numPartitions,originalDF.col(partitionCol));
	}
	
}
