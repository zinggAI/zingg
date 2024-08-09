package zingg.common.core.executor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import zingg.common.client.IArguments;

public abstract class TrainerTester<S, D, R, C, T> extends ExecutorTester<S, D, R, C, T> {

	public static final Log LOG = LogFactory.getLog(TrainerTester.class);
	
	protected IArguments args;
	
	public TrainerTester(Trainer<S, D, R, C, T> executor,IArguments args) {
		super(executor);
		this.args = args;
	}

}
