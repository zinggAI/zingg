package zingg.common.core.executor.validate;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import zingg.common.client.IArguments;
import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;
import zingg.common.client.util.ColName;
import zingg.common.core.executor.TrainMatcher;

public class TrainMatchValidator<S, D, R, C, T> extends ExecutorValidator<S, D, R, C, T> {

    public static final Log LOG = LogFactory.getLog(TrainMatchValidator.class);
	TrainerValidator<S, D, R, C, T> tv;
	MatcherValidator<S, D, R, C, T> mv;
	
	public TrainMatchValidator(TrainMatcher<S, D, R, C, T> executor) {
		super(executor);
		tv = new TrainerValidator<S, D, R, C, T>(executor.getTrainer());
		mv = new MatcherValidator<S, D, R, C, T>(executor.getMatcher());
	}
    
    @Override
	public void validateResults() throws ZinggClientException {
		tv.validateResults();
		mv.validateResults();	
	}

}
