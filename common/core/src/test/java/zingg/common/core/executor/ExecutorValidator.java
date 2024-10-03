package zingg.common.core.executor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import zingg.common.client.ZinggClientException;

public abstract class ExecutorValidator<S, D, R, C, T> {

    public static final Log LOG = LogFactory.getLog(ExecutorValidator.class);
	
	public ZinggBase<S,D, R, C, T> validator;
	
	public ExecutorValidator(ZinggBase<S, D, R, C, T> validator) {
		this.validator = validator;
	}

    public abstract void validateResults() throws ZinggClientException;
    
}
