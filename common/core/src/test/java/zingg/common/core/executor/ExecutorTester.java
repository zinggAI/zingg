package zingg.common.core.executor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import zingg.common.client.ClientOptions;
import zingg.common.client.IArguments;
import zingg.common.client.ZinggClientException;

public class ExecutorTester<S, D, R, C, T>{

	public static final Log LOG = LogFactory.getLog(ExecutorTester.class);
	
	public ZinggBase<S, D, R, C, T> executor;
	public ExecutorValidator<S, D, R, C, T> validator;
	
	public ExecutorTester(ZinggBase<S, D, R, C, T> executor,ExecutorValidator<S, D, R, C, T> validator) {
		this.executor = executor;
		this.validator = validator;
	}
	
	public void initAndExecute(IArguments args, S session) throws ZinggClientException {
		executor.init(args,session, new ClientOptions());
		executor.execute();
	}
	
	public void validateResults() throws ZinggClientException {
		validator.validateResults();
	}	
	
}
