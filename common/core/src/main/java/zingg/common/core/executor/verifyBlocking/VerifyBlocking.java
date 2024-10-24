package zingg.common.core.executor.verifyblocking;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;
import zingg.common.client.cols.ZidAndFieldDefSelector;
import zingg.common.client.options.ZinggOptions;
import zingg.common.client.util.ColName;
import zingg.common.core.block.Blocker;
import zingg.common.core.block.InputDataGetter;
import zingg.common.core.executor.ZinggBase;

public abstract class VerifyBlocking<S,D,R,C,T> extends ZinggBase<S,D,R,C,T>{

	private static final long serialVersionUID = 1L;
	protected static String name = "zingg.VerifyBlocking";
	public static final Log LOG = LogFactory.getLog(VerifyBlocking.class);
    public long timestamp = System.currentTimeMillis();   
    VerifyBlockingPipes<S,D,R,C> getPipe = new VerifyBlockingPipes<S,D,R,C>();

	public VerifyBlocking() {
        setZinggOption(ZinggOptions.VERIFY_BLOCKING);
    }

    @Override
    public void execute() throws ZinggClientException {
        try {
			setTimestamp(timestamp);
			ZFrame<D,R,C>  testDataOriginal = new InputDataGetter<S,D,R,C>().getTestData(getPipeUtil(),args);
			testDataOriginal =  getFieldDefColumnsDS(testDataOriginal).cache();
			ZFrame<D,R,C> blocked = new Blocker<S,D,R,C,T>().getBlocked(testDataOriginal,args,getBlockingTreeUtil());
			LOG.info("Blocked");

			ZFrame<D,R,C> blockCounts = blocked.select(ColName.HASH_COL).groupByCount(ColName.HASH_COL, ColName.HASH_COL + "_count").sortDescending(ColName.HASH_COL + "_count");

            getPipeUtil().write(blockCounts, getPipe.getPipeForVerifyBlockingLocation(args, getPipeUtil(), timestamp, "counts"));	


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
			
			getPipeUtil().write(matchingRecords, getPipe.getPipeForVerifyBlockingLocation(args, getPipeUtil(),timestamp, "blockSamples/" + hash));
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




