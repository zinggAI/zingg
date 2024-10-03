package zingg.common.core.executor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import zingg.common.client.IArguments;

public abstract class TrainerValidator<S, D, R, C, T> extends ExecutorValidator<S, D, R, C, T> {

	public static final Log LOG = LogFactory.getLog(TrainerValidator.class);
	
	protected IArguments args;
	
	public TrainerValidator(Trainer<S, D, R, C, T> validator,IArguments args) {
		super(validator);
		this.args = args;
	}

}
