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

public abstract class TestExecutorsGeneric<S, D, R, C, T> {

	public static final Log LOG = LogFactory.getLog(TestExecutorsGeneric.class);
	
	protected IArguments args;
	

	protected S session;
	
	public TestExecutorsGeneric() {
					
	}
	
	public TestExecutorsGeneric(S s) throws ZinggClientException, IOException {
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
			"findTrainingData");
		return configFile;
	}

	public abstract String getConfigFile();
	
	
	@Test
	public void testExecutors() throws ZinggClientException {	
		List<ExecutorTester<S, D, R, C, T>> executorTesterList = new ArrayList<ExecutorTester<S, D, R, C, T>>();

		TrainingDataFinderTester<S, D, R, C, T> tdft = new TrainingDataFinderTester<S, D, R, C, T>(getTrainingDataFinder());
		executorTesterList.add(tdft);
		
		LabellerTester<S, D, R, C, T> lt = new LabellerTester<S, D, R, C, T>(getLabeller());
		executorTesterList.add(lt);

		// training and labelling needed twice to get sufficient data
		TrainingDataFinderTester<S, D, R, C, T> tdft2 = new TrainingDataFinderTester<S, D, R, C, T>(getTrainingDataFinder());
		executorTesterList.add(tdft2);
		
		LabellerTester<S, D, R, C, T> lt2 = new LabellerTester<S, D, R, C, T>(getLabeller());
		executorTesterList.add(lt2);
	
		TrainerTester<S, D, R, C, T> tt = new TrainerTester<S, D, R, C, T>(getTrainer());
		executorTesterList.add(tt);

		MatcherTester<S, D, R, C, T> mt = new MatcherTester(getMatcher());
		executorTesterList.add(mt);
		
		testExecutors(executorTesterList);
	}
	
	
	public void testExecutors(List<ExecutorTester<S, D, R, C, T>> executorTesterList) throws ZinggClientException {
		for (ExecutorTester<S, D, R, C, T> executorTester : executorTesterList) {
			executorTester.execute();
			executorTester.validateResults();
		}
	}	

	public abstract void tearDown();	
	
	protected abstract TrainingDataFinder<S, D, R, C, T> getTrainingDataFinder() throws ZinggClientException;
	
	protected abstract Labeller<S, D, R, C, T> getLabeller() throws ZinggClientException;
	
	protected abstract Trainer<S, D, R, C, T> getTrainer() throws ZinggClientException;

	protected abstract Matcher<S, D, R, C, T> getMatcher() throws ZinggClientException;	
	
}
