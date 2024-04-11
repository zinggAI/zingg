package zingg.common.core.data.df;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import zingg.common.client.IArguments;
import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;
import zingg.common.client.util.ColName;
import zingg.common.core.block.Canopy;
import zingg.common.core.block.Tree;
import zingg.common.core.util.BlockingTreeUtil;

public class BlockedFrame<S, D, R, C, T> implements IZFrameProcessor<S, D, R, C, T> {

	protected ZFrame<D,R,C> originalDF;
	
	protected ZFrame<D,R,C> processedDF;
	
	protected IArguments args;
	
	protected BlockingTreeUtil<S, D, R, C, T> blockingTreeUtil;
	
	public static final Log LOG = LogFactory.getLog(BlockedFrame.class);   
	
	public BlockedFrame(ZFrame<D, R, C> originalDF, IArguments args, BlockingTreeUtil<S, D, R, C, T> blockingTreeUtil) throws Exception, ZinggClientException {
		super();
		this.originalDF = originalDF;
		this.args = args;
		this.blockingTreeUtil = blockingTreeUtil;
	}

	@Override
	public ZFrame<D, R, C> getOriginalDF() {
		return originalDF;
	}

	@Override
	public ZFrame<D, R, C> getProcessedDF() {
		return processedDF;
	}

	protected ZFrame<D, R, C> getBlocked() throws ZinggClientException {
		try {
			//testData = dropDuplicates(testData);
			
			Tree<Canopy<R>> tree = blockingTreeUtil.readBlockingTree(args);
			ZFrame<D, R, C> blocked = blockingTreeUtil.getBlockHashes(getOriginalDF(), tree);
			ZFrame<D, R, C> blocked1 = blocked.repartition(args.getNumPartitions(), blocked.col(ColName.HASH_COL));//.cache();
			return blocked1;
		} catch (ZinggClientException e) {
			throw e;
		} catch (Exception e) {
			throw new ZinggClientException(e);
		}
	}
	
	@Override
	public void process() throws ZinggClientException {
		this.processedDF = getBlocked();
	}

}
