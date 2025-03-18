package zingg.common.core.executor.blockingverifier;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;
import zingg.common.client.cols.ZidAndFieldDefSelector;
import zingg.common.client.data.BlockedData;
import zingg.common.client.data.IData;
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
			IData<D,R,C> testDataOriginal = getTestData();
			ZFrame<D, R, C> testDataOriginalDF =  getFieldDefColumnsDS(testDataOriginal.getData()).cache();
			BlockedData<D,R,C> blocked = getBlockedData(testDataOriginalDF);
			
			//get the no of counts per hash
			ZFrame<D,R,C> blockCounts = getBlockCounts(blocked.getData());
			getPipeUtil().write(blockCounts,getVerifyBlockingPipeUtil().getCountsPipe(args));
			//get the records associated with the top 3 hashes
			getBlockSamples(blocked.getData(), blockCounts,verifyBlockingPipeUtil);
			
		} catch (Exception e) {
			if (LOG.isDebugEnabled()){
				e.printStackTrace();
			}
			throw new ZinggClientException(e.getMessage());
		}
    
    }

	protected ZFrame<D, R, C> getBlockCounts(ZFrame<D, R, C> blocked) throws ZinggClientException{
		ZFrame<D,R,C> blockCounts = blocked.select(ColName.HASH_COL).groupByCount(ColName.HASH_COL, ColName.HASH_COUNTS_COL);
		return blockCounts;
	}

	protected int getNoOfBlocks(){
		return 3;
	}

	protected ZFrame<D,R,C> getTopRecordsDF(ZFrame<D,R,C> blockCounts){
		return blockCounts.select(ColName.HASH_COL,ColName.HASH_COUNTS_COL).sortDescending(ColName.HASH_COUNTS_COL).limit(getNoOfBlocks());
	}

	protected void getBlockSamples(ZFrame<D, R, C> blocked, ZFrame<D, R, C> blockCounts, IVerifyBlockingPipes<S,D,R,C> verifyBlockingPipeUtil) throws ZinggClientException {
		ZFrame<D,R,C> blockTopRec = getTopRecordsDF(blockCounts);
		List<R> topRec = blockTopRec.collectAsList();

		for(R row: topRec) {
			int hash = blockTopRec.getAsInt(row, ColName.HASH_COL);
			ZFrame<D,R,C> matchingRecords = getMatchingRecords(row, blockTopRec, blocked, hash);
			getPipeUtil().write(matchingRecords, getVerifyBlockingPipeUtil().getBlockSamplesPipe(args, ColName.BLOCK_SAMPLES + hash));
		}
		
	}

	protected ZFrame<D,R,C> getMatchingRecords(R row, ZFrame<D,R,C> blockTopRec, ZFrame<D,R,C> blocked, int hash){
		ZFrame<D,R,C> matchingRecords = null;
		matchingRecords = blocked.filter(blocked.equalTo(ColName.HASH_COL,String.valueOf(hash))).sample(false,0.1d);
		return matchingRecords;
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

	public IData<D, R, C> getTestData() throws ZinggClientException{
		return getDataGetter().getData(args,getPipeUtil());
	}

	public IDataGetter<S,D,R,C> getDataGetter(){
		if (dataGetter == null){
			this.dataGetter = new InputDataGetter<S,D,R,C>(getPipeUtil());
		}
		return dataGetter;
	}

	public BlockedData<D,R,C> getBlockedData(ZFrame<D,R,C> testDataOriginal) throws ZinggClientException, Exception{
		return getBlocker().getBlocked(testDataOriginal,args, getModelHelper(),getBlockingTreeUtil());
		
	}

	public IBlocker<S,D,R,C,T> getBlocker(){
		if (blocker == null){
			this.blocker = new Blocker<S,D,R,C,T>(getBlockingTreeUtil());
		}
		return blocker;
	}

}




