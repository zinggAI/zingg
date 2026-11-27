package zingg.common.core.executor;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import zingg.common.client.arguments.model.IArguments;
import zingg.common.client.ClientOptions;
import zingg.common.client.ZinggClientException;
import zingg.common.client.util.DFObjectUtil;
import zingg.common.core.executor.validate.ExecutorValidator;

public class ExecutorTester<S, D, R, C, T>{

	public static final Log LOG = LogFactory.getLog(ExecutorTester.class);
	
	public ZinggBase<S, D, R, C, T> executor;
	public ExecutorValidator<S, D, R, C, T> validator;
	protected IArguments args;
	protected String modelId;
	protected DFObjectUtil<S,D,R,C> dfObjectUtil;
	
	/**
	 * Args are built by the test (see TestArgumentsBuilder) and handed over ready to use;
	 * no config file is read.
	 */
	public ExecutorTester(ZinggBase<S, D, R, C, T> executor,ExecutorValidator<S, D, R, C, T> validator, IArguments args, String modelId, DFObjectUtil<S,D,R,C> dfObjectUtil) throws ZinggClientException, IOException {
		this.executor = executor;
		this.validator = validator;
		this.args = args;
		this.modelId = modelId;
		this.dfObjectUtil = dfObjectUtil;
	}

	/**
	 * Gets args ready for the run: stamps the model id, then lets the tester adjust them
	 * through updateArgs().
	 *
	 * The driver calls this before every execution (see TestExecutorsGeneric), so it is
	 * deliberately not called from the constructor - a constructor call would run before
	 * subclass fields are assigned.
	 */
	public void setupArgs() throws ZinggClientException, IOException{
		// every run gets its own model id
		this.args.setModelId(modelId);
		try {
			updateArgs(this.args);
		} catch (Exception e) {
			throw new ZinggClientException("Error while preparing args for the test run: ", e);
		}
	}

	/**
	 * Hook for testers that need to adjust args before the run, e.g. attach an in-memory
	 * dataset to a pipe. Does nothing by default.
	 */
	protected void updateArgs(IArguments args) throws Exception {
	}

	public void initAndExecute(S session) throws ZinggClientException {
		executor.init(args,session, new ClientOptions());
		executor.execute();
	}
	
	public void validateResults() throws ZinggClientException {
		validator.validateResults();
	}	
	
}
