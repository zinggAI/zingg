package zingg.common.core.block;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import zingg.common.client.IArguments;
import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;
import zingg.common.client.util.ColName;
import zingg.common.client.util.IModelHelper;
import zingg.common.core.util.BlockingTreeUtil;

public class Blocker<S,D,R,C,T> implements IBlocker<S,D,R,C,T> {

	private static final long serialVersionUID = 1L;
	private static final Log LOG = LogFactory.getLog(Blocker.class);

	private final BlockingTreeUtil<S,D,R,C,T> blockingTreeUtil;
	private final IArguments arguments;
	private final IModelHelper<D, R, C> modelHelper;

	public Blocker(BlockingTreeUtil<S,D,R,C,T> blockingTreeUtil, IArguments arguments, IModelHelper<D, R, C> modelHelper){
		this.blockingTreeUtil = blockingTreeUtil;
		this.arguments = arguments;
		this.modelHelper = modelHelper;
	}

	public ZFrame<D,R,C> getBlocked(ZFrame<D,R,C> testData, IArguments args, IModelHelper<D,R,C> imh) throws Exception, ZinggClientException {
		LOG.warn("Blocking model location is " + imh.getBlockingTreePipe(args));
		Tree<Canopy<R>> tree = blockingTreeUtil.readBlockingTree(args, imh);
		ZFrame<D,R,C> blocked = blockingTreeUtil.getBlockHashes(testData, tree);
        return blocked.repartition(args.getNumPartitions(), blocked.col(ColName.HASH_COL)).cache();
	}

	@Override
	public ZFrame<D,R,C> getBlocked(ZFrame<D,R,C> testData) throws Exception, ZinggClientException{
		LOG.warn("Blocking model location is " + modelHelper.getBlockingTreePipe(arguments));
		Tree<Canopy<R>> tree = blockingTreeUtil.readBlockingTree(arguments, modelHelper);
		ZFrame<D,R,C> blocked = blockingTreeUtil.getBlockHashes(testData, tree);
        return blocked.repartition(arguments.getNumPartitions(), blocked.col(ColName.HASH_COL)).cache();
	}


	@Override
	public ZFrame<D, R, C> process(ZFrame<D, R, C> data) throws ZinggClientException, Exception {
		return getBlocked(data);
	}
}
