package zingg.common.core.executor;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import zingg.common.client.IArguments;
import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;
import zingg.common.client.util.ColName;

public class TrainMatchValidator<S, D, R, C, T> extends ExecutorValidator<S, D, R, C, T> {

    public static final Log LOG = LogFactory.getLog(TrainMatchValidator.class);

    protected IArguments args;
	TrainerValidator<S, D, R, C, T> tv;
	MatcherValidator<S, D, R, C, T> mv;
	
	public TrainMatchValidator(TrainMatcher<S, D, R, C, T> executor, IArguments args) {
		super(executor);
        this.args = args;
		tv = new TrainerValidator<S, D, R, C, T>(executor.getTrainer(), args);
		mv = new MatcherValidator<S, D, R, C, T>(executor.getMatcher());
	}
    
    @Override
	public void validateResults() throws ZinggClientException {
		tv.validateResults();
		mv.validateResults();	
	}

}
