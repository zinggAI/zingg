package zingg.common.core.executor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import zingg.common.client.ArgumentsUtil;
import zingg.common.client.ClientOptions;
import zingg.common.client.IArguments;
import zingg.common.client.ZinggClientException;

public abstract class TestExecutorsGeneric<S, D, R, C, T> {

	public static final Log LOG = LogFactory.getLog(TestExecutorsGeneric.class);
	
	protected IArguments args;
	protected S session;
	protected ClientOptions options;
	
	public TestExecutorsGeneric() {
					
	}
	
	public TestExecutorsGeneric(S s) throws ZinggClientException, IOException {
		init(s);					
	}

	public void init(S s) throws ZinggClientException, IOException {
		this.session = s;
		// set up args
		setupArgs();	
		setupLinkerArgs();				
	}

	public String setupArgs() throws ZinggClientException, IOException {
		String configFile = getClass().getClassLoader().getResource(getConfigFile()).getFile();
		args = new ArgumentsUtil().createArgumentsFromJSON(
			configFile, 
			"findTrainingData");
		return configFile;
	}

	public String setupLinkerArgs() throws ZinggClientException, IOException {
		String configFile = getClass().getClassLoader().getResource(getConfigFile()).getFile();
		args = new ArgumentsUtil().createArgumentsFromJSON(
			configFile, 
			"link");
		return configFile;
	}

	List<ExecutorTester<S, D, R, C, T>> executorTesterList = new ArrayList<ExecutorTester<S, D, R, C, T>>();

	public abstract List<ExecutorTester<S, D, R, C, T>> getExecutors() throws ZinggClientException;

	public abstract String getConfigFile();

	public abstract String getLinkerConfigFile();

	public abstract void tearDown();	
	

}
