package zingg.common.core.executor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class TrainerTester<S, D, R, C, T> extends ExecutorTester<S, D, R, C, T> {

	public static final Log LOG = LogFactory.getLog(TrainerTester.class);
	
	public TrainerTester(Trainer<S, D, R, C, T> executor) {
		super(executor);
	}

	@Override
	public void validateResults() {
		LOG.info("train successful");
	}

}
