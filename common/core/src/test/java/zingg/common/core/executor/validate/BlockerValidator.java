package zingg.common.core.executor.validate;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;
import zingg.common.client.util.ColName;
import zingg.common.core.executor.verifyblocking.IVerifyBlockingPipeUtil;
import zingg.common.core.executor.verifyblocking.VerifyBlocking;
import zingg.common.core.executor.verifyblocking.VerifyBlockingPipes;

public class BlockerValidator<S, D, R, C, T> extends ExecutorValidator<S, D, R, C, T> {

	public static final Log LOG = LogFactory.getLog(BlockerValidator.class);
	IVerifyBlockingPipeUtil verifyBlockingPipeUtil = new VerifyBlockingPipes<S,D,R,C>(executor.getContext().getPipeUtil(), ((VerifyBlocking<S, D, R, C, T>) executor).getTimestamp());
	
	public BlockerValidator(VerifyBlocking<S, D, R, C, T> executor) {
		super(executor);
	}

	@Override
	public void validateResults() throws ZinggClientException {
	
			ZFrame<D, R, C> df  = executor.getContext().getPipeUtil().read(false,false,verifyBlockingPipeUtil.getCountsPipe(executor.getArgs())); 
			ZFrame<D, R, C> topDf = df.select(ColName.HASH_COL,ColName.HASH_COUNTS_COL).limit(3);
			long blockCount = topDf.count();
			LOG.info("blockCount : " + blockCount);
			assertTrue(blockCount == 3);
			List<R> countsDf = topDf.collectAsList();
			int sumHash = 0;
			long sumCount = 0;
			for(R row: countsDf) {
			int hash = (int) df.getAsInt(row, ColName.HASH_COL);
			long count = (long) df.getAsLong(row, ColName.HASH_COUNTS_COL);
			sumHash += hash;
			sumCount += count;
			}
			assertTrue(sumHash == 11843 | sumHash == 11855);
			assertTrue(sumCount == 24 | sumCount == 16);
			

    }

}