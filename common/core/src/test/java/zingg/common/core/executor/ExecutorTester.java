package zingg.common.core.executor;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import zingg.common.client.IArguments;
import zingg.common.client.ArgumentsUtil;
import zingg.common.client.ClientOptions;
import zingg.common.client.ZinggClientException;

public class ExecutorTester<S, D, R, C, T>{

	public static final Log LOG = LogFactory.getLog(ExecutorTester.class);
	
	public ZinggBase<S, D, R, C, T> executor;
	public ExecutorValidator<S, D, R, C, T> validator;
	protected IArguments args;
	protected String configFile;
	protected String phase;
	
	public ExecutorTester(ZinggBase<S, D, R, C, T> executor,ExecutorValidator<S, D, R, C, T> validator, IArguments args, String configFile, String phase) throws ZinggClientException, IOException {
		this.executor = executor;
		this.validator = validator;
		this.args = setupArgs(configFile, phase);
		this.configFile = setupConfigFile(configFile, phase);
	}

	public IArguments setupArgs(String configFile, String phase) throws ZinggClientException, IOException {
		args = new ArgumentsUtil().createArgumentsFromJSON(getClass().getClassLoader().getResource(configFile).getFile(), phase);
		return args;
	}

	public String setupConfigFile(String configFile, String phase) throws ZinggClientException, IOException {
		String config = getClass().getClassLoader().getResource(configFile).getFile();
		args = new ArgumentsUtil().createArgumentsFromJSON(config, phase);
		return config;
	}

	public void initAndExecute(IArguments args, S session, ClientOptions c) throws ZinggClientException {
		executor.init(args,session, c);
		executor.execute();
	}
	
	public void validateResults() throws ZinggClientException {
		validator.validateResults();
	}	
	
}
