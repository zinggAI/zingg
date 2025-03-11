package zingg.common.core.executor;

import java.io.IOException;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.AfterEach;

import zingg.common.client.ZinggClientException;
import zingg.common.core.ZinggException;
import zingg.common.core.executor.validate.FindAndLabelValidator;
import zingg.common.core.executor.validate.TrainMatchValidator;
import zingg.common.core.util.ICleanUpUtil;
import zingg.common.core.util.IPerformCleanUpUtil;
import zingg.common.core.util.TestType;

public abstract class TestExecutorsCompound<S, D, R, C, T> extends TestExecutorsGeneric<S, D, R, C, T> implements IPerformCleanUpUtil<S>{

	public static final Log LOG = LogFactory.getLog(TestExecutorsCompound.class);
	
	public TestExecutorsCompound() {	
	}

	@Override
	public List<ExecutorTester<S, D, R, C, T>> getExecutors() throws ZinggClientException, IOException{
		FindAndLabeller<S, D, R, C, T> findAndLabel = getFindAndLabeller();
		FindAndLabelValidator<S, D, R, C, T> falValidator = new FindAndLabelValidator<S, D, R, C, T>(findAndLabel);
		ExecutorTester<S, D, R, C, T> et = new FindAndLabellerExecutorTester<>(findAndLabel, falValidator,getConfigFile(),getModelId(),getDFObjectUtil());
		executorTesterList.add(et);
		TrainMatcher<S, D, R, C, T> trainMatch = getTrainMatcher();
		executorTesterList.add(new ExecutorTester<S, D, R, C, T>(trainMatch,getTrainMatchValidator(trainMatch), getConfigFile(),getModelId(),getDFObjectUtil()));
		return executorTesterList;
	}
	
	public abstract String getConfigFile();

	protected abstract FindAndLabeller<S, D, R, C, T> getFindAndLabeller() throws ZinggClientException;

	protected abstract TrainMatchValidator<S, D, R, C, T> getTrainMatchValidator(TrainMatcher<S, D, R, C, T> trainMatch);

	protected abstract TrainMatcher<S, D, R, C, T> getTrainMatcher() throws ZinggClientException;

	@AfterEach
	public void cleanTestStateData() {
		performCleanup(TestType.COMPOUND);
	}

}