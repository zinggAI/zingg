package zingg.common.core.executor.blockingverifier;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;
import zingg.common.client.cols.ZidAndFieldDefSelector;
import zingg.common.client.options.ZinggOptions;
import zingg.common.client.util.ColName;
import zingg.common.core.block.Blocker;
import zingg.common.core.block.IBlocker;
import zingg.common.core.block.InputDataGetter;
import zingg.common.core.executor.ZinggBase;
import zingg.common.core.match.data.IDataGetter;

public abstract class VerifyBlocking<S,D,R,C,T> extends ZinggBase<S,D,R,C,T>{

	private static final long serialVersionUID = 1L;
	protected static String name = "zingg.common.core.executor.blockingverifier.VerifyBlocking";
	public static final Log LOG = LogFactory.getLog(VerifyBlocking.class);
    public long timestamp = System.currentTimeMillis();   
	public int noOfBlocks = 3 ;
	public double percentageOfBlockedRecords = 0.1 ;
	protected IVerifyBlockingPipes<S,D,R,C> verifyBlockingPipeUtil;
	protected IDataGetter<S, D, R, C> dataGetter;
	protected IBlocker<S,D,R,C,T> blocker;

	public VerifyBlocking() {
    	setZinggOption(ZinggOptions.VERIFY_BLOCKING);
    }

    @Override
    public void execute() throws ZinggClientException {
        try {
			setTimestamp(timestamp);
			ZFrame<D,R,C>  testDataOriginal = getTestData();
			testDataOriginal =  getFieldDefColumnsDS(testDataOriginal).cache();
			ZFrame<D,R,C> blocked = getBlockedData(testDataOriginal);
			LOG.info("Blocked");
			
			ZFrame<D,R,C> blockCounts = blocked.select(ColName.HASH_COL).groupByCount(ColName.HASH_COL, ColName.HASH_COUNTS_COL);
			
            getPipeUtil().write(blockCounts,getVerifyBlockingPipeUtil().getCountsPipe(args));	

			ZFrame<D,R,C> blockTopRec = blockCounts.select(ColName.HASH_COL,ColName.HASH_COUNTS_COL).sortDescending(ColName.HASH_COUNTS_COL).limit(noOfBlocks);
			blockTopRec = blockTopRec.cache();

			getBlockSamples(blocked, blockTopRec,verifyBlockingPipeUtil);
			
		} catch (Exception e) {
			if (LOG.isDebugEnabled()){
				e.printStackTrace();
			}
			throw new ZinggClientException(e.getMessage());
		}
    
    }


	public void getBlockSamples(ZFrame<D, R, C> blocked, ZFrame<D, R, C> blockTopRec, IVerifyBlockingPipes verifyBlockingPipeUtil) throws ZinggClientException {
		List<R> topRec = blockTopRec.collectAsList();

		for(R row: topRec) {
			int hash = (int) blockTopRec.getAsInt(row, ColName.HASH_COL);
			long count = (long) blockTopRec.getAsLong(row, ColName.HASH_COUNTS_COL);
			int sampleSize = Math.max(1, (int) Math.ceil(count * percentageOfBlockedRecords));
			ZFrame<D,R,C> matchingRecords = null;
			matchingRecords = blocked.filter(blocked.equalTo(ColName.HASH_COL,String.valueOf(hash))).limit(sampleSize);
			getPipeUtil().write(matchingRecords, getVerifyBlockingPipeUtil().getBlockSamplesPipe(args, ColName.BLOCK_SAMPLES + hash));
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

	public abstract IVerifyBlockingPipes<S,D,R,C> getVerifyBlockingPipeUtil();

    public void setVerifyBlockingPipeUtil(IVerifyBlockingPipes<S,D,R,C> verifyBlockingPipeUtil){
		this.verifyBlockingPipeUtil = verifyBlockingPipeUtil;
	}

	public ZFrame<D,R,C> getTestData() throws ZinggClientException{
		return getDataGetter().getData(args,getPipeUtil());
	}

	public IDataGetter<S,D,R,C> getDataGetter(){
		if (dataGetter == null){
			this.dataGetter = new InputDataGetter<S,D,R,C>(getPipeUtil());
		}
		return dataGetter;
	}

	public ZFrame<D,R,C> getBlockedData(ZFrame<D,R,C> testDataOriginal) throws ZinggClientException, Exception{
		return getBlocker().getBlocked(testDataOriginal,args, getModelHelper(),getBlockingTreeUtil());
		
	}

	public IBlocker<S,D,R,C,T> getBlocker(){
		if (blocker == null){
			this.blocker = new Blocker<S,D,R,C,T>(getBlockingTreeUtil());
		}
		return blocker;
	}

}




