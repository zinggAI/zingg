package zingg.common.core.block;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import scala.Serializable;
import zingg.common.client.IArguments;
import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;
import zingg.common.client.cols.ZidAndFieldDefSelector;
import zingg.common.client.pipe.FilePipe;
import zingg.common.client.pipe.Pipe;
import zingg.common.client.util.ColName;
import zingg.common.core.executor.ZinggBase;
import zingg.common.core.sink.TableOutput;
import zingg.common.core.util.Analytics;
import zingg.common.core.util.BlockingTreeUtil;
import zingg.common.core.util.Metric;

public class Blocker<S,D,R,C,T> extends ZinggBase<S,D,R,C,T>{

	public static final Log LOG = LogFactory.getLog(Blocker.class);
    public long timestamp; 
    
    public ZFrame<D,R,C>  getTestData() throws ZinggClientException{
        ZFrame<D,R,C>  data = getPipeUtil().read(true, true, args.getNumPartitions(), true, args.getData());
       return data;
   }

    public ZFrame<D,R,C>  getBlocked(ZFrame<D,R,C> testData) throws Exception, ZinggClientException{
		LOG.debug("Blocking model file location is " + args.getBlockFile());
		Tree<Canopy<R>> tree = getBlockingTreeUtil().readBlockingTree(args);
		ZFrame<D,R,C> blocked = getBlockingTreeUtil().getBlockHashes(testData, tree);		
		ZFrame<D,R,C> blocked1 = blocked.repartition(args.getNumPartitions(), blocked.col(ColName.HASH_COL)).cache();
		return blocked1;
	}

    public Pipe<D,R,C> getPipeForDebugBlockingLocation(long timestamp, String type){
        Pipe<D, R, C> p = new Pipe<D, R, C>();
		p.setFormat(Pipe.FORMAT_PARQUET);
		p.setProp(FilePipe.LOCATION, getName(args,timestamp,type));
		p = getPipeUtil().setOverwriteMode(p);
		return p;
    }    

    private String getName(IArguments args, long timestamp, String type){
        return args.getZinggModelDir() + "/blocks/" + timestamp + "/" + type;
    }
    

    @Override
    public void execute() throws ZinggClientException {
        try {
			
			ZFrame<D,R,C>  testDataOriginal = getTestData();
			testDataOriginal =  getFieldDefColumnsDS(testDataOriginal).cache();
			ZFrame<D,R,C> blocked = getBlocked(testDataOriginal);
			LOG.info("Blocked");
			
            getPipeUtil().write(blocked.select(ColName.HASH_COL).groupByCount(ColName.HASH_COL, ColName.HASH_COL + "_count").sortDescending(ColName.HASH_COL + "_count"), getPipeForDebugBlockingLocation(timestamp, "counts"));	

            blocked.limit(3);

            getPipeUtil().write(blocked.sample(false, 0.1), getPipeForDebugBlockingLocation(timestamp, "blockSamples"));	
			
		} catch (Exception e) {
			if (LOG.isDebugEnabled()) e.printStackTrace();
			e.printStackTrace();
			throw new ZinggClientException(e.getMessage());
		}
    
    }

	public ZFrame<D, R, C> getFieldDefColumnsDS(ZFrame<D, R, C> testDataOriginal) {
		ZidAndFieldDefSelector zidAndFieldDefSelector = new ZidAndFieldDefSelector(args.getFieldDefinition());
		return testDataOriginal.select(zidAndFieldDefSelector.getCols());
//		return getDSUtil().getFieldDefColumnsDS(testDataOriginal, args, true);
	}

    public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

}




