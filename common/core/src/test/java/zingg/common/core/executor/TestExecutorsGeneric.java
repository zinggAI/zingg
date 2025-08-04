package zingg.common.core.executor;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.Test;

import zingg.common.client.ZinggClientException;
import zingg.common.client.util.DFObjectUtil;

public abstract class TestExecutorsGeneric<S, D, R, C, T> {

	public static final Log LOG = LogFactory.getLog(TestExecutorsGeneric.class);
	private String modelId;
	protected S session;
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

	protected abstract DFObjectUtil<S,D,R,C> getDFObjectUtil();

	public abstract List<ExecutorTester<S, D, R, C, T>> getExecutors() throws ZinggClientException, IOException, InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchMethodException;

	//public abstract void tearDown();	

	 @Test
	public void testExecutors() throws ZinggClientException, IOException {
		 try {
			 List<ExecutorTester<S, D, R, C, T>> executorTesterList = getExecutors();
			 for (ExecutorTester<S, D, R, C, T> executorTester : executorTesterList) {
				 executorTester.setupArgs();
				 executorTester.initAndExecute(session);
				 executorTester.validateResults();
			 }
		 } catch (Throwable throwable) {
			throwable.printStackTrace();
			throw new ZinggClientException("Exception occurred while running one or more test executors, " + throwable.getMessage());
		 }

	}

	//model id getter
	public String getModelId() {
		 return this.modelId;
	}
}
