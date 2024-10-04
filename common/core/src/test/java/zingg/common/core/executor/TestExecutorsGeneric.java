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

	List<ExecutorTester<S, D, R, C, T>> executorTesterList = new ArrayList<ExecutorTester<S, D, R, C, T>>();

	public List<ExecutorTester<S, D, R, C, T>> getExecutors() throws ZinggClientException{
		executorTesterList.add(new ExecutorTester<S, D, R, C, T>(getTrainingDataFinder(), new TrainingDataFinderValidator<S, D, R, C, T>(getTrainingDataFinder())));
		executorTesterList.add(new ExecutorTester<S, D, R, C, T>(getLabeller(), new LabellerValidator<S, D, R, C, T>(getLabeller())));
		executorTesterList.add(new ExecutorTester<S, D, R, C, T>(getTrainingDataFinder(), new TrainingDataFinderValidator<S, D, R, C, T>(getTrainingDataFinder())));
		executorTesterList.add(new ExecutorTester<S, D, R, C, T>(getLabeller(), new LabellerValidator<S, D, R, C, T>(getLabeller())));
		executorTesterList.add(new ExecutorTester<S, D, R, C, T>(getTrainer(),getTrainerValidator(getTrainer())));
		executorTesterList.add(new ExecutorTester<S, D, R, C, T>(getMatcher(),new MatcherValidator(getMatcher())));
		//executorTesterList.add(new ExecutorTester<>(getLinker(),new LinkerValidator(getLinker())));
		return executorTesterList;
	}
	
	
	@Test
	public void testExecutors() throws ZinggClientException {	

		List<ExecutorTester<S, D, R, C, T>> executorTesterList = getExecutors();

		for (ExecutorTester<S, D, R, C, T> executorTester : executorTesterList) {
			executorTester.initAndExecute(args,session);
			executorTester.validateResults();
		}
		
	}
	

	public abstract void tearDown();	
	
	protected abstract TrainingDataFinder<S, D, R, C, T> getTrainingDataFinder() throws ZinggClientException;
	
	protected abstract Labeller<S, D, R, C, T> getLabeller() throws ZinggClientException;
	
	protected abstract Trainer<S, D, R, C, T> getTrainer() throws ZinggClientException;

	protected abstract TrainerValidator<S, D, R, C, T> getTrainerValidator(Trainer<S, D, R, C, T> trainer);

	protected abstract Matcher<S, D, R, C, T> getMatcher() throws ZinggClientException;	

	//protected abstract Linker<S, D, R, C, T> getLinker() throws ZinggClientException;	

}
