package zingg.common.core.executor;

import java.io.IOException;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.Test;

import zingg.common.client.ClientOptions;
import zingg.common.client.ZinggClientException;

public abstract class TestExecutorsCompound<S, D, R, C, T> extends TestExecutorsGeneric<S, D, R, C, T> {

	public static final Log LOG = LogFactory.getLog(TestExecutorsCompound.class);
	
	public TestExecutorsCompound() {	
	}

	@Override
	public List<ExecutorTester<S, D, R, C, T>> getExecutors() throws ZinggClientException, IOException{
		FindAndLabeller<S, D, R, C, T> findAndLabel = getFindAndLabeller();
		FindAndLabelValidator<S, D, R, C, T> falValidator = new FindAndLabelValidator<S, D, R, C, T>(findAndLabel);
		ExecutorTester<S, D, R, C, T> et = new ExecutorTester<S, D, R, C, T>(findAndLabel, falValidator, args, getConfigFile(), "findAndLabel");
		executorTesterList.add(et);
		executorTesterList.add(et);
		executorTesterList.add(new ExecutorTester<S, D, R, C, T>(getTrainMatcher(),getTrainMatchValidator(getTrainMatcher()), args, getConfigFile(), "trainMatch"));
		return executorTesterList;
	}

	@Test
	public void testExecutors() throws ZinggClientException, IOException {	

		List<ExecutorTester<S, D, R, C, T>> executorTesterList = getExecutors();

		for (ExecutorTester<S, D, R, C, T> executorTester : executorTesterList) {
			executorTester.initAndExecute(args,session, new ClientOptions());
			executorTester.validateResults();
		}
		
	}
	
	public abstract String getConfigFile();

	protected abstract FindAndLabeller<S, D, R, C, T> getFindAndLabeller() throws ZinggClientException;

	protected abstract TrainMatchValidator<S, D, R, C, T> getTrainMatchValidator(TrainMatcher<S, D, R, C, T> trainMatch);

	protected abstract TrainMatcher<S, D, R, C, T> getTrainMatcher() throws ZinggClientException;

}