package zingg.common.core.executor;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.AfterEach;

import zingg.common.client.ZinggClientException;
import zingg.common.core.executor.validate.LabellerValidator;
import zingg.common.core.executor.validate.LinkerValidator;
import zingg.common.core.executor.validate.MatcherValidator;
import zingg.common.core.executor.validate.TrainerValidator;
import zingg.common.core.executor.validate.TrainingDataFinderValidator;
import zingg.common.core.util.IPerformCleanUpUtil;
import zingg.common.core.util.TestType;

public abstract class TestExecutorsSingle<S, D, R, C, T> extends TestExecutorsGeneric<S, D, R, C, T> implements IPerformCleanUpUtil<S>{


	public static final Log LOG = LogFactory.getLog(TestExecutorsSingle.class);

	public TestExecutorsSingle(){

	}

	@Override
	public List<ExecutorTester<S, D, R, C, T>> getExecutors() throws ZinggClientException, IOException, InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchMethodException {
	  	
		getBaseExecutors();
		getAdditionalExecutors();

		return executorTesterList;
	}

	public void getBaseExecutors() throws ZinggClientException, IOException, InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchMethodException {

		TrainingDataFinder<S, D, R, C, T> tdf = getTrainingDataFinder();
    	Labeller<S, D, R, C, T> labeler = getLabeller();
		executorTesterList.add(new FtdAndLabelCombinedExecutorTester<S, D, R, C, T>(tdf, new TrainingDataFinderValidator<S, D, R, C, T>(tdf), getConfigFile(),
				labeler, new LabellerValidator<S, D, R, C, T>(labeler), getModelId(), getDFObjectUtil()));


		Trainer<S, D, R, C, T> trainer = getTrainer();
		executorTesterList.add(new ExecutorTester<S, D, R, C, T>(trainer,getTrainerValidator(trainer),getConfigFile(),getModelId(),getDFObjectUtil()));

	}

	public void getAdditionalExecutors() throws ZinggClientException, IOException, InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchMethodException {
		
		Matcher<S, D, R, C, T> matcher = getMatcher();
		executorTesterList.add(new ExecutorTester<S, D, R, C, T>(matcher,new MatcherValidator<S, D, R, C, T>(matcher),getConfigFile(),getModelId(),getDFObjectUtil()));

		Linker<S, D, R, C, T> linker = getLinker();
		executorTesterList.add(new ExecutorTester<S, D, R, C, T>(linker,new LinkerValidator<S, D, R, C, T>(linker),getLinkerConfigFile(),getModelId(),getDFObjectUtil()));
	
	}

	public abstract String getConfigFile();

	public abstract String getLinkerConfigFile();

    protected abstract TrainingDataFinder<S, D, R, C, T> getTrainingDataFinder() throws ZinggClientException;
	
	protected abstract Labeller<S, D, R, C, T> getLabeller() throws ZinggClientException;
	
	protected abstract Trainer<S, D, R, C, T> getTrainer() throws ZinggClientException;

	protected abstract TrainerValidator<S, D, R, C, T> getTrainerValidator(Trainer<S, D, R, C, T> trainer);
    
    protected abstract Matcher<S, D, R, C, T> getMatcher() throws ZinggClientException;	

	protected abstract Linker<S, D, R, C, T> getLinker() throws ZinggClientException;	

	@AfterEach
	public void cleanTestStateData() {
		performCleanup(TestType.SINGLE);
	}

}
