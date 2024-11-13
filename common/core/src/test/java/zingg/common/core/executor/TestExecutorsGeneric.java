package zingg.common.core.executor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.Test;

import zingg.common.client.ZinggClientException;

public abstract class TestExecutorsGeneric<S, D, R, C, T> {

	public static final Log LOG = LogFactory.getLog(TestExecutorsGeneric.class);

	protected S session;
	protected String modelId;
	protected List<ExecutorTester<S, D, R, C, T>> executorTesterList = new ArrayList<ExecutorTester<S, D, R, C, T>>();
	
	public TestExecutorsGeneric() {
					
	}
	
	public TestExecutorsGeneric(S s) throws ZinggClientException, IOException {
		init(s);					
	}

	public void init(S s) throws ZinggClientException, IOException {
		this.session = s;
		modelId = "j_"+System.currentTimeMillis();
	}

	

	public abstract List<ExecutorTester<S, D, R, C, T>> getExecutors() throws ZinggClientException, IOException;

	//public abstract void tearDown();	

	 @Test
	public void testExecutors() throws ZinggClientException, IOException {	

		List<ExecutorTester<S, D, R, C, T>> executorTesterList = getExecutors();

		for (ExecutorTester<S, D, R, C, T> executorTester : executorTesterList) {
			executorTester.setupArgs();
			executorTester.initAndExecute(session);
			executorTester.validateResults();
		}
		
	}
	

}
