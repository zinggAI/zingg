package zingg.common.core.executor;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;

public class TrainingDataFinderTester<S, D, R, C, T> extends ExecutorTester<S, D, R, C, T> {

	public static final Log LOG = LogFactory.getLog(TrainingDataFinderTester.class);
	
	public TrainingDataFinderTester(TrainingDataFinder<S, D, R, C, T> executor) {
		super(executor);
	}

	@Override
	public void validateResults() throws ZinggClientException {
		// check that unmarked data has at least 10 rows
		ZFrame<D, R, C> df = executor.getContext().getPipeUtil().read(false, false, executor.getContext().getPipeUtil().getTrainingDataUnmarkedPipe(executor.getArgs()));
		
		long trainingDataCount = df.count();
		assertTrue(trainingDataCount > 10);
		LOG.info("trainingDataCount : "+ trainingDataCount);				
	}

}
