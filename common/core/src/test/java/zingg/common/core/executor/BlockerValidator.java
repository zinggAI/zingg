package zingg.common.core.executor;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;
import zingg.common.core.executor.verifyblocking.VerifyBlocking;

public class BlockerValidator<S, D, R, C, T> extends ExecutorValidator<S, D, R, C, T> {

	public static final Log LOG = LogFactory.getLog(BlockerValidator.class);
	
	public BlockerValidator(VerifyBlocking<S, D, R, C, T> executor) {
		super(executor);
	}

	@Override
	public void validateResults() throws ZinggClientException {
	
			ZFrame<D, R, C> df  = null; 
			
			long blockCount = df.count();
			assertTrue(blockCount == 3);
			LOG.info("blockCount : " + blockCount);

    }

}