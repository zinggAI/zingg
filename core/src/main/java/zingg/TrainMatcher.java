package zingg;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import zingg.client.Arguments;
import zingg.client.ZinggClientException;
import zingg.client.ZinggOptions;

public abstract class TrainMatcher<S,D,R,C,T1,T2> extends Matcher<S,D,R,C,T1,T2>{

	protected static String name = "zingg.TrainMatcher";
	public static final Log LOG = LogFactory.getLog(TrainMatcher.class); 
	
	private Trainer<S,D,R,C,T1,T2> trainer;

    public TrainMatcher() {
        setZinggOptions(ZinggOptions.TRAIN_MATCH);		
    }

	@Override
	public void init(Arguments args, String license)
        throws ZinggClientException {
			super.init(args, license);			
	}

	@Override
    public void execute() throws ZinggClientException {
		trainer.execute();
		super.execute();
	}


	
	    
}
