package zingg.common.core.block;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import zingg.common.client.IArguments;
import zingg.common.client.MatchType;
import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;
import zingg.common.client.cols.ZidAndFieldDefSelector;
import zingg.common.client.pipe.FilePipe;
import zingg.common.client.pipe.Pipe;
import zingg.common.client.util.ColName;
import zingg.common.core.executor.ZinggBase;

public class Blocker<S,D,R,C,T> extends ZinggBase<S,D,R,C,T>{

	private static final long serialVersionUID = 1L;
	protected static String name = "zingg.Blocker";
	public static final Log LOG = LogFactory.getLog(Blocker.class);
    public long timestamp = System.currentTimeMillis(); 
    
    public ZFrame<D,R,C>  getTestData() throws ZinggClientException{
        ZFrame<D,R,C>  data = getPipeUtil().read(true, true, args.getNumPartitions(), true, args.getData());
       return data;
   }

    public ZFrame<D,R,C> getBlocked(ZFrame<D,R,C> testData) throws Exception, ZinggClientException{
		LOG.debug("Blocking model file location is " + args.getBlockFile());
		Tree<Canopy<R>> tree = getBlockingTreeUtil().readBlockingTree(args);
		ZFrame<D,R,C> blocked = getBlockingTreeUtil().getBlockHashes(testData, tree);
		ZFrame<D,R,C> blocked1 = blocked.repartition(args.getNumPartitions(), blocked.col(ColName.HASH_COL)).cache();
		return blocked1;
	}

    public Pipe<D,R,C> getPipeForVerifyBlockingLocation(long timestamp, String type){
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
			setTimestamp(timestamp);

			ZFrame<D,R,C> blockCounts = blocked.select(ColName.HASH_COL).groupByCount(ColName.HASH_COL, ColName.HASH_COL + "_count").sortDescending(ColName.HASH_COL + "_count");

            getPipeUtil().write(blockCounts, getPipeForVerifyBlockingLocation(timestamp, "counts"));	


			ZFrame<D,R,C> blockTopRec = blockCounts.select(ColName.HASH_COL,ColName.HASH_COL + "_count").limit(3);

			getBlockSamples(blocked, blockTopRec);
			
		} catch (Exception e) {
			if (LOG.isDebugEnabled()){
				e.printStackTrace();
			}
			e.printStackTrace();
			throw new ZinggClientException(e.getMessage());
		}
    
    }

	public void getBlockSamples(ZFrame<D, R, C> blocked, ZFrame<D, R, C> blockTopRec) throws ZinggClientException {
		List<R> topRec = blockTopRec.collectAsList();
		List<R> dataRec = blocked.collectAsList();
		ZFrame<D,R,C> blockRecords = null;


		for(R row: topRec) {
			int hash = (int) blockTopRec.get(row, ColName.HASH_COL);
			long count = (long) blockTopRec.get(row, ColName.HASH_COL + "_count");
			int sampleSize = Math.max(1, (int) Math.ceil(count * 0.1));
			ZFrame<D,R,C> matchingRecords = null;

			for(R rows: dataRec)
			{
				int recHash = (int)blocked.get(rows,ColName.HASH_COL);
				if(hash == recHash){
					matchingRecords = blocked.select(blocked.getCols()).withColumn(ColName.HASH_COL,hash).limit(sampleSize);
				}
			}
			
			getPipeUtil().write(matchingRecords, getPipeForVerifyBlockingLocation(timestamp, "blockSamples/" + hash));
		}
		
	}

	public ZFrame<D, R, C> getFieldDefColumnsDS(ZFrame<D, R, C> testDataOriginal) {
		ZidAndFieldDefSelector zidAndFieldDefSelector = new ZidAndFieldDefSelector(args.getFieldDefinition());
		return testDataOriginal.select(zidAndFieldDefSelector.getCols());
        //return getDSUtil().getFieldDefColumnsDS(testDataOriginal, args, true);
	}

    public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

}




