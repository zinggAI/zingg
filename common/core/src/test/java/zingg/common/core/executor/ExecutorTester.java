package zingg.common.core.executor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import zingg.common.client.ZinggClientException;

public abstract class ExecutorTester<S, D, R, C, T> {

	public static final Log LOG = LogFactory.getLog(ExecutorTester.class);
	
	public ZinggBaseCommon<S,D, R, C, T> executor;
	
	public ExecutorTester(ZinggBaseCommon<S, D, R, C, T> executor) {
		this.executor = executor;
	}
	
	public void execute() throws ZinggClientException {
		executor.execute();
	}
	
	public abstract void validateResults() throws ZinggClientException;	
	
}
