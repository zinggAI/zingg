package zingg.common.core.executor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;
import zingg.common.client.options.ZinggOptions;
import zingg.common.client.util.ColName;
import zingg.common.core.block.Canopy;
import zingg.common.core.block.Tree;
import zingg.common.core.sink.TableOutput;
import zingg.common.core.util.Analytics;
import zingg.common.core.util.Metric;

public class Blocker<S,D,R,C,T> extends ZinggBase<S,D,R,C,T> {

    private static final long serialVersionUID = 1L;
	protected static String name = "zingg.Blocker";
	public static final Log LOG = LogFactory.getLog(Blocker.class);    
	
    public Blocker() {
        setZinggOption(ZinggOptions.DEBUG_BLOCKING);
    }

    

    public ZFrame<D,R,C>  getBlocked( ZFrame<D,R,C>  testData) throws Exception, ZinggClientException{
		LOG.debug("Blocking model file location is " + args.getBlockFile());
		Tree<Canopy<R>> tree = getBlockingTreeUtil().readBlockingTree(args);
		ZFrame<D,R,C> blocked = getBlockingTreeUtil().getBlockHashes(testData, tree);		
		ZFrame<D,R,C> blocked1 = blocked.repartition(args.getNumPartitions(), blocked.col(ColName.HASH_COL)).cache();
		return blocked1;
	}
    
    @Override
    public void execute() throws ZinggClientException {
        try {
			
			
		} catch (Exception e) {
			if (LOG.isDebugEnabled()) e.printStackTrace();
			e.printStackTrace();
			throw new ZinggClientException(e.getMessage());
		}
    }

    public void writeOutput( ZFrame<D,R,C>  blocked,  ZFrame<D,R,C>  dupesActual) throws ZinggClientException {
		try{
		//input dupes are pairs
		///pick ones according to the threshold by user
		
			
		//all clusters consolidated in one place
		if (args.getOutput() != null) {
			ZFrame<D, R, C> graphWithScores = getOutput(blocked, dupesActual);
			getPipeUtil().write(blocked.select(ColName.HASH_COL).groupByCount(ColName.HASH_COL, ColName.HASH_COL + "_count"), getPipeForDebugBlockingLocation(getTimestamp()));
		}
		}
		catch(Exception e) {
			e.printStackTrace(); 
		}
		
	}
}
