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
	public int noOfBlocks = 3 ;
	public double percentageOfBlockedRecords = 0.1 ;
	public static final String hashColumn = ColName.HASH_COL;
	public static final String hashCountsColumn = ColName.HASH_COL + "_count";

	public VerifyBlocking() {
    	setZinggOption(ZinggOptions.VERIFY_BLOCKING);
    }

    @Override
    public void execute() throws ZinggClientException {
        try {
			setTimestamp(timestamp);
			ZFrame<D,R,C>  testDataOriginal = new InputDataGetter<S,D,R,C>(getPipeUtil()).getTestData(args);
			testDataOriginal =  getFieldDefColumnsDS(testDataOriginal).cache();
			ZFrame<D,R,C> blocked = new Blocker<S,D,R,C,T>(getBlockingTreeUtil()).getBlocked(testDataOriginal,args);
			LOG.info("Blocked");
			IVerifyBlockingPipes verifyBlockingPipe = new VerifyBlockingPipes<S,D,R,C>(getPipeUtil(), timestamp);
			ZFrame<D,R,C> blockCounts = blocked.select(hashColumn).groupByCount(hashColumn, hashCountsColumn).sortDescending(hashCountsColumn);

            getPipeUtil().write(blockCounts,verifyBlockingPipe.getCountsPipe(args));	

			ZFrame<D,R,C> blockTopRec = blockCounts.select(hashColumn,hashCountsColumn).limit(noOfBlocks);

			getBlockSamples(blocked, blockTopRec,verifyBlockingPipe);
			
		} catch (Exception e) {
			if (LOG.isDebugEnabled()){
				e.printStackTrace();
			}
			e.printStackTrace();
			throw new ZinggClientException(e.getMessage());
		}
    
    }


	public void getBlockSamples(ZFrame<D, R, C> blocked, ZFrame<D, R, C> blockTopRec, IVerifyBlockingPipes verifyBlockingPipe) throws ZinggClientException {
		List<R> topRec = blockTopRec.collectAsList();

		for(R row: topRec) {
			int hash = (int) blockTopRec.getAsInt(row, hashColumn);
			long count = (long) blockTopRec.getAsLong(row, hashCountsColumn);
			int sampleSize = Math.max(1, (int) Math.ceil(count * percentageOfBlockedRecords));
			ZFrame<D,R,C> matchingRecords = null;
			matchingRecords = blocked.filter(blocked.equalTo(hashColumn,String.valueOf(hash))).limit(sampleSize);
			getPipeUtil().write(matchingRecords, verifyBlockingPipe.getBlockSamplesPipe(args, "blockSamples/" + hash));
		}
		
	}

	public ZFrame<D, R, C> getFieldDefColumnsDS(ZFrame<D, R, C> testDataOriginal) {
		ZidAndFieldDefSelector zidAndFieldDefSelector = new ZidAndFieldDefSelector(args.getFieldDefinition());
		return testDataOriginal.select(zidAndFieldDefSelector.getCols());
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

    

}




