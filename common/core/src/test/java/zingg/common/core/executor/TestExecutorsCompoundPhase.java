package zingg.common.core.executor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.Test;

import zingg.common.client.ArgumentsUtil;
import zingg.common.client.IArguments;
import zingg.common.client.ZinggClientException;

public abstract class TestExecutorsCompoundPhase<S, D, R, C, T> {
    
    public static final Log LOG = LogFactory.getLog(TestExecutorsCompoundPhase.class);
	
	protected IArguments args;
	protected S session;
	
	public TestExecutorsCompoundPhase() {
					
	}
	
	public TestExecutorsCompoundPhase(S s) throws ZinggClientException, IOException {
		init(s);					
	}

	public void init(S s) throws ZinggClientException, IOException {
		this.session = s;
		// set up args
		setupArgs();					
	}

	public String setupArgs() throws ZinggClientException, IOException {
		String configFile = getClass().getClassLoader().getResource(getConfigFile()).getFile();
		args = new ArgumentsUtil().createArgumentsFromJSON(
			configFile, 
			"findAndLabel");
		return configFile;
	}

	public abstract String getConfigFile();

	@Test
	public void testExecutors() throws ZinggClientException {	
		List<ExecutorTester<S, D, R, C, T>> executorTesterList = new ArrayList<ExecutorTester<S, D, R, C, T>>();

		FindAndLabeller<S, D, R, C, T> findAndLabel = getFindAndLabeller();
		findAndLabel.init(args,session);
		FindAndLabelTester<S, D, R, C, T> fal = new FindAndLabelTester<S, D, R, C, T>(findAndLabel);
		executorTesterList.add(fal);

		FindAndLabeller<S, D, R, C, T> findAndLabel2 = getFindAndLabeller();
		findAndLabel2.init(args,session);
		FindAndLabelTester<S, D, R, C, T> fal2 = new FindAndLabelTester<S, D, R, C, T>(findAndLabel2);
		executorTesterList.add(fal2);
		
		TrainMatcher<S, D, R, C, T> trainMatch = getTrainMatcher();
		trainMatch.init(args, session);
		TrainMatchTester tm = getTrainMatchTester(trainMatch);
		executorTesterList.add(tm);

		testExecutors(executorTesterList);
	}

	protected abstract TrainMatchTester<S, D, R, C, T> getTrainMatchTester(TrainMatcher<S, D, R, C, T> trainMatch);
	
	
	public void testExecutors(List<ExecutorTester<S, D, R, C, T>> executorTesterList) throws ZinggClientException {
		for (ExecutorTester<S, D, R, C, T> executorTester : executorTesterList) {
			executorTester.execute();
			executorTester.validateResults();
		}
	}	

	public abstract void tearDown();

	protected abstract FindAndLabeller<S, D, R, C, T> getFindAndLabeller() throws ZinggClientException;

	protected abstract TrainMatcher<S, D, R, C, T> getTrainMatcher() throws ZinggClientException;

}
