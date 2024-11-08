package zingg.common.core.executor;

import java.io.IOException;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.Test;

import zingg.common.client.ClientOptions;
import zingg.common.client.ZinggClientException;
import zingg.common.core.executor.validate.BlockerValidator;
import zingg.common.core.executor.validate.LabellerValidator;
import zingg.common.core.executor.validate.LinkerValidator;
import zingg.common.core.executor.validate.MatcherValidator;
import zingg.common.core.executor.validate.TrainerValidator;
import zingg.common.core.executor.validate.TrainingDataFinderValidator;
import zingg.common.core.executor.verifyblocking.VerifyBlocking;

public abstract class TestSingleExecutors<S, D, R, C, T> extends TestExecutorsGeneric<S, D, R, C, T> {


	public static final Log LOG = LogFactory.getLog(TestSingleExecutors.class);

	public TestSingleExecutors(){

	}

	@Override
	public List<ExecutorTester<S, D, R, C, T>> getExecutors() throws ZinggClientException, IOException{
	    TrainingDataFinder<S, D, R, C, T> tdf = getTrainingDataFinder();
		executorTesterList.add(new ExecutorTester<S, D, R, C, T>(tdf, new TrainingDataFinderValidator<S, D, R, C, T>(tdf),getConfigFile()));

		Labeller<S, D, R, C, T> labeler = getLabeller();
		executorTesterList.add(new ExecutorTester<S, D, R, C, T>(labeler, new LabellerValidator<S, D, R, C, T>(labeler),getConfigFile()));

		executorTesterList.add(new ExecutorTester<S, D, R, C, T>(tdf, new TrainingDataFinderValidator<S, D, R, C, T>(tdf),getConfigFile()));
		executorTesterList.add(new ExecutorTester<S, D, R, C, T>(labeler, new LabellerValidator<S, D, R, C, T>(labeler),getConfigFile()));

		executorTesterList.add(new ExecutorTester<S, D, R, C, T>(tdf, new TrainingDataFinderValidator<S, D, R, C, T>(tdf),getConfigFile()));
		executorTesterList.add(new ExecutorTester<S, D, R, C, T>(labeler, new LabellerValidator<S, D, R, C, T>(labeler),getConfigFile()));

		Trainer<S, D, R, C, T> trainer = getTrainer();
		executorTesterList.add(new ExecutorTester<S, D, R, C, T>(trainer,getTrainerValidator(trainer),getConfigFile()));

        VerifyBlocking<S, D, R, C, T> verifyBlocker = getVerifyBlocker();
        executorTesterList.add(new ExecutorTester<S, D, R, C, T>(verifyBlocker, new BlockerValidator<S, D, R, C, T>(verifyBlocker),getConfigFile()));

		Matcher<S, D, R, C, T> matcher = getMatcher();
		executorTesterList.add(new ExecutorTester<S, D, R, C, T>(matcher,new MatcherValidator<S, D, R, C, T>(matcher),getConfigFile()));

		Linker<S, D, R, C, T> linker = getLinker();
		//executorTesterList.add(new LinkerExecutorTester<S, D, R, C, T>(linker,new LinkerValidator<S, D, R, C, T>(linker),getLinkerConfigFile()));

		return executorTesterList;
	}

   

	public abstract String getConfigFile();

	public abstract String getLinkerConfigFile();

    protected abstract TrainingDataFinder<S, D, R, C, T> getTrainingDataFinder() throws ZinggClientException;
	
	protected abstract Labeller<S, D, R, C, T> getLabeller() throws ZinggClientException;
	
	protected abstract Trainer<S, D, R, C, T> getTrainer() throws ZinggClientException;

	protected abstract TrainerValidator<S, D, R, C, T> getTrainerValidator(Trainer<S, D, R, C, T> trainer);

	protected abstract VerifyBlocking<S, D, R, C, T> getVerifyBlocker() throws ZinggClientException;
    
    protected abstract Matcher<S, D, R, C, T> getMatcher() throws ZinggClientException;	

	protected abstract Linker<S, D, R, C, T> getLinker() throws ZinggClientException;	

    
}
