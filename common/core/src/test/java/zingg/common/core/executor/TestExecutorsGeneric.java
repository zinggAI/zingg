package zingg.common.core.executor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import zingg.common.client.ClientOptions;
import zingg.common.client.IArguments;
import zingg.common.client.ZinggClientException;

public abstract class TestExecutorsGeneric<S, D, R, C, T> {

	public static final Log LOG = LogFactory.getLog(TestExecutorsGeneric.class);
	
	protected IArguments args;
	protected IArguments linkerArgs;
	protected S session;
	protected ClientOptions options;
	
	public TestExecutorsGeneric() {
					
	}
	
	public TestExecutorsGeneric(S s) throws ZinggClientException, IOException {
		init(s);					
	}

	public void init(S s) throws ZinggClientException, IOException {
		this.session = s;		
	}

	List<ExecutorTester<S, D, R, C, T>> executorTesterList = new ArrayList<ExecutorTester<S, D, R, C, T>>();

	public abstract List<ExecutorTester<S, D, R, C, T>> getExecutors() throws ZinggClientException, IOException;

	public abstract void tearDown();	
	

}
