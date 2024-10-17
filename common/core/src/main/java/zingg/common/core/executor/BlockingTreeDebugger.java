package zingg.common.core.executor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;
import zingg.common.client.options.ZinggOptions;
import zingg.common.client.util.ColName;
import zingg.common.core.block.Blocker;
import zingg.common.core.block.Canopy;
import zingg.common.core.block.Tree;

public abstract class BlockingTreeDebugger<S,D,R,C,T> extends Matcher<S,D,R,C,T> {

    private static final long serialVersionUID = 1L;
	protected static String name = "zingg.BlockingTreeDebugger";
	public static final Log LOG = LogFactory.getLog(BlockingTreeDebugger.class);    
	
    public BlockingTreeDebugger() {
        setZinggOption(ZinggOptions.VERIFY_BLOCKING);
    }

    public ZFrame<D,R,C>  getBlocked( ZFrame<D,R,C>  testData) throws Exception, ZinggClientException{
		//for verifying blocking
		Blocker<S,D,R,C,T> blocker = new Blocker<S,D,R,C,T>();
        blocker.getBlocked(getBlockingTreeUtil());
		LOG.debug("Blocking model file location is " + args.getBlockFile());
		Tree<Canopy<R>> tree = getBlockingTreeUtil().readBlockingTree(args);
		ZFrame<D,R,C> blocked = getBlockingTreeUtil().getBlockHashes(testData, tree);		
		ZFrame<D,R,C> blocked1 = blocked.repartition(args.getNumPartitions(), blocked.col(ColName.HASH_COL)).cache();
		return blocked1;
	}
    
}
