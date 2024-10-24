package zingg.common.core.executor;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;
import zingg.common.client.util.ColName;
import zingg.common.core.executor.verifyBlocking.VerifyBlocking;

public class BlockerValidator<S, D, R, C, T> extends ExecutorValidator<S, D, R, C, T> {

	public static final Log LOG = LogFactory.getLog(BlockerValidator.class);
	
	public BlockerValidator(VerifyBlocking<S, D, R, C, T> executor) {
		super(executor);
	}

	@Override
	public void validateResults() throws ZinggClientException {
			//test counts are same in test output and given set input which has only z_hash also based on example test
            
            //read output pipe - counts
            //input data has z_hash also as a column
            //find the counts in input data and arrange in desc order
            //asserts true on input and output
    }
}