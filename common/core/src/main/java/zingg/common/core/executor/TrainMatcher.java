package zingg.common.core.executor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import zingg.common.client.Arguments;
import zingg.common.client.ZinggClientException;
import zingg.common.client.ZinggOptions;
import zingg.common.client.license.IZinggLicense;

public abstract class TrainMatcher<S,D,R,C,T> extends ZinggBase<S,D,R,C,T>{

	private static final long serialVersionUID = 1L;
	protected static String name = "zingg.TrainMatcher";
	public static final Log LOG = LogFactory.getLog(TrainMatcher.class); 
	
	protected Trainer<S,D,R,C,T> trainer;
	protected Matcher<S,D,R,C,T> matcher;
	
    public TrainMatcher() {
        setZinggOptions(ZinggOptions.TRAIN_MATCH);		
    }

	@Override
	public void init(Arguments args, IZinggLicense license)
        throws ZinggClientException {
			trainer.init(args, license);
			matcher.init(args, license);
			super.init(args, license);			
	}

	@Override
    public void execute() throws ZinggClientException {
		trainer.execute();
		matcher.execute();
	}
	    
}
