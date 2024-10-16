package zingg.common.core.executor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import zingg.common.client.IArguments;
import zingg.common.client.ZinggClientException;
import zingg.common.client.options.ZinggOptions;

public abstract class TrainMatcher<S,D,R,C,T> extends ZinggBase<S,D,R,C,T>{

	private static final long serialVersionUID = 1L;
	protected static String name = "zingg.TrainMatcher";
	public static final Log LOG = LogFactory.getLog(TrainMatcher.class); 
	
	protected Trainer<S,D,R,C,T> trainer;
	protected Matcher<S,D,R,C,T> matcher;
	
    public TrainMatcher() {
        setZinggOption(ZinggOptions.TRAIN_MATCH);		
    }

	@Override
	public void init(IArguments args, S s)
        throws ZinggClientException {
			trainer.init(args,s);
			matcher.init(args,s);
			super.init(args,s);			
	}

	@Override
    public void execute() throws ZinggClientException {
		trainer.execute();
		matcher.execute();
	}

	public Trainer<S, D, R, C, T> getTrainer() {
		return trainer;
	}

	public void setTrainer(Trainer<S, D, R, C, T> trainer) {
		this.trainer = trainer;
	}

	public Matcher<S, D, R, C, T> getMatcher() {
		return matcher;
	}

	public void setMatcher(Matcher<S, D, R, C, T> matcher) {
		this.matcher = matcher;
	}

	
	    
}
