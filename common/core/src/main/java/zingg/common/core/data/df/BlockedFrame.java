package zingg.common.core.data.df;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import zingg.common.client.IArguments;
import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;
import zingg.common.client.util.ColName;
import zingg.common.core.block.Canopy;
import zingg.common.core.block.Tree;
import zingg.common.core.context.Context;

public class BlockedFrame<S, D, R, C, T> implements IZFrameProcessor<S, D, R, C, T> {

	protected ZFrame<D,R,C> originalDF;
	
	protected ZFrame<D,R,C> processedDF;
	
	protected IArguments args;
	
	protected Context<S,D,R,C,T> context;
	
	public static final Log LOG = LogFactory.getLog(BlockedFrame.class);   
	
	public BlockedFrame(ZFrame<D, R, C> originalDF, IArguments args, Context<S,D,R,C,T> context) throws Exception, ZinggClientException {
		super();
		this.originalDF = originalDF;
		this.args = args;
		this.context = context;
		this.processedDF = getBlocked();
	}

	@Override
	public ZFrame<D, R, C> getOriginalDF() {
		return originalDF;
	}

	@Override
	public ZFrame<D, R, C> getProcessedDF() {
		return processedDF;
	}

	protected ZFrame<D, R, C> getBlocked() throws Exception, ZinggClientException {
		//testData = dropDuplicates(testData);
		Tree<Canopy<R>> tree = context.getBlockingTreeUtil().readBlockingTree(args);
		ZFrame<D, R, C> blocked = context.getBlockingTreeUtil().getBlockHashes(getOriginalDF(), tree);
		ZFrame<D, R, C> blocked1 = blocked.repartition(args.getNumPartitions(), blocked.col(ColName.HASH_COL));//.cache();
		return blocked1;
	}

}
