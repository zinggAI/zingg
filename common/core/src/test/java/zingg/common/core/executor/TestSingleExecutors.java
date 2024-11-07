package zingg.common.core.executor;

import java.io.IOException;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.Test;

import zingg.common.client.ClientOptions;
import zingg.common.client.ZinggClientException;
import zingg.common.core.executor.verifyblocking.VerifyBlocking;

public abstract class TestSingleExecutors<S, D, R, C, T> extends TestExecutorsGeneric<S, D, R, C, T> {


	public static final Log LOG = LogFactory.getLog(TestSingleExecutors.class);

	public TestSingleExecutors(){

	}

	@Override
	public List<ExecutorTester<S, D, R, C, T>> getExecutors() throws ZinggClientException, IOException{
	    TrainingDataFinder<S, D, R, C, T> tdf = getTrainingDataFinder();
		executorTesterList.add(new ExecutorTester<S, D, R, C, T>(tdf, new TrainingDataFinderValidator<S, D, R, C, T>(tdf),args,getConfigFile(),"findTrainingData"));

		Labeller<S, D, R, C, T> labeler = getLabeller();
		executorTesterList.add(new ExecutorTester<S, D, R, C, T>(labeler, new LabellerValidator<S, D, R, C, T>(labeler),args,getConfigFile(),"label"));

		executorTesterList.add(new ExecutorTester<S, D, R, C, T>(tdf, new TrainingDataFinderValidator<S, D, R, C, T>(tdf),args,getConfigFile(),"findTrainingData"));
		executorTesterList.add(new ExecutorTester<S, D, R, C, T>(labeler, new LabellerValidator<S, D, R, C, T>(labeler),args,getConfigFile(),"label"));

		executorTesterList.add(new ExecutorTester<S, D, R, C, T>(tdf, new TrainingDataFinderValidator<S, D, R, C, T>(tdf),args,getConfigFile(),"findTrainingData"));
		executorTesterList.add(new ExecutorTester<S, D, R, C, T>(labeler, new LabellerValidator<S, D, R, C, T>(labeler),args,getConfigFile(),"label"));

		Trainer<S, D, R, C, T> trainer = getTrainer();
		executorTesterList.add(new ExecutorTester<S, D, R, C, T>(trainer,getTrainerValidator(trainer),args,getConfigFile(),"train"));

        VerifyBlocking<S, D, R, C, T> verifyBlocker = getVerifyBlocker();
        executorTesterList.add(new ExecutorTester<S, D, R, C, T>(verifyBlocker, new BlockerValidator<S, D, R, C, T>(verifyBlocker),args,getConfigFile(),"verifyBlocking"));

		Matcher<S, D, R, C, T> matcher = getMatcher();
		executorTesterList.add(new ExecutorTester<S, D, R, C, T>(matcher,new MatcherValidator<S, D, R, C, T>(matcher),args,getConfigFile(),"match"));

		Linker<S, D, R, C, T> linker = getLinker();
		executorTesterList.add(new ExecutorTester<S, D, R, C, T>(linker,new LinkerValidator<S, D, R, C, T>(linker),linkerArgs,getLinkerConfigFile(),"link"));

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

	public abstract String getLinkerConfigFile();

    protected abstract TrainingDataFinder<S, D, R, C, T> getTrainingDataFinder() throws ZinggClientException;
	
	protected abstract Labeller<S, D, R, C, T> getLabeller() throws ZinggClientException;
	
	protected abstract Trainer<S, D, R, C, T> getTrainer() throws ZinggClientException;

	protected abstract TrainerValidator<S, D, R, C, T> getTrainerValidator(Trainer<S, D, R, C, T> trainer);

	protected abstract VerifyBlocking<S, D, R, C, T> getVerifyBlocker() throws ZinggClientException;
    
    protected abstract Matcher<S, D, R, C, T> getMatcher() throws ZinggClientException;	

	protected abstract Linker<S, D, R, C, T> getLinker() throws ZinggClientException;	

    
}
