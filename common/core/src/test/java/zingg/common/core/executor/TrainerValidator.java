package zingg.common.core.executor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import zingg.common.client.IArguments;
import zingg.common.client.ZinggClientException;

public class TrainerValidator<S, D, R, C, T> extends ExecutorValidator<S, D, R, C, T> {

	public static final Log LOG = LogFactory.getLog(TrainerValidator.class);
	
	protected IArguments args;
	
	public TrainerValidator(Trainer<S, D, R, C, T> executor,IArguments args) {
		super(executor);
		this.args = args;
	}

	@Override
	public void validateResults() throws ZinggClientException {
		//doesnt do anything
		//TODO - add modele xistence checks
	}

}
