package zingg.common.core.executor;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.Test;

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

		TrainingDataFinder<S, D, R, C, T> trainingDataFinder = getTrainingDataFinder();
		trainingDataFinder.init(args,session,options);
		TrainingDataFinderTester<S, D, R, C, T> tdft = new TrainingDataFinderTester<S, D, R, C, T>(trainingDataFinder);
		executorTesterList.add(tdft);
		
		Labeller<S, D, R, C, T> labeller = getLabeller();
		labeller.init(args,session,options);
		LabellerTester<S, D, R, C, T> lt = new LabellerTester<S, D, R, C, T>(labeller);
		executorTesterList.add(lt);

		// training and labelling needed twice to get sufficient data
		TrainingDataFinder<S, D, R, C, T> trainingDataFinder2 = getTrainingDataFinder();
		trainingDataFinder2.init(args,session,options);
		TrainingDataFinderTester<S, D, R, C, T> tdft2 = new TrainingDataFinderTester<S, D, R, C, T>(trainingDataFinder2);
		executorTesterList.add(tdft2);
		
		Labeller<S, D, R, C, T> labeller2 = getLabeller();
		labeller2.init(args,session,options);
		LabellerTester<S, D, R, C, T> lt2 = new LabellerTester<S, D, R, C, T>(labeller2);
		executorTesterList.add(lt2);
	
		Trainer<S, D, R, C, T> trainer = getTrainer();
		trainer.init(args,session,options);
		TrainerTester<S, D, R, C, T> tt = getTrainerTester(trainer);
		executorTesterList.add(tt);

		Matcher<S, D, R, C, T> matcher = getMatcher();
		matcher.init(args,session,options);
		MatcherTester<S, D, R, C, T> mt = new MatcherTester(matcher);
		executorTesterList.add(mt);
		
		testExecutors(executorTesterList);
	}

	protected abstract TrainerTester<S, D, R, C, T> getTrainerTester(Trainer<S, D, R, C, T> trainer);
	
	
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
