package zingg.common.core.block;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import zingg.common.client.IArguments;
import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;
import zingg.common.client.util.ColName;
import zingg.common.core.util.BlockingTreeUtil;

public class Blocker<S,D,R,C,T> {

    private static final long serialVersionUID = 1L;
	private static final Log LOG = LogFactory.getLog(Blocker.class);

	BlockingTreeUtil<S,D,R,C,T> blockingTree;

    public Blocker(BlockingTreeUtil<S,D,R,C,T> blockingTree){
		this.blockingTree = blockingTree;
    }

    public ZFrame<D,R,C> getBlocked(ZFrame<D,R,C> testData, IArguments args) throws Exception, ZinggClientException{
		LOG.warn("Blocking model location is " + args.getBlockFile());
		Tree<Canopy<R>> tree = blockingTree.readBlockingTree(args);
		ZFrame<D,R,C> blocked = blockingTree.getBlockHashes(testData, tree);
		ZFrame<D,R,C> blocked1 = blocked.repartition(args.getNumPartitions(), blocked.col(ColName.HASH_COL)).cache();
		return blocked1;
	}
    
}
