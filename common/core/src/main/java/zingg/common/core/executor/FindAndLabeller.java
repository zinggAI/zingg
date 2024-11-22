package zingg.common.core.executor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import zingg.common.client.ClientOptions;
import zingg.common.client.IZArgs;
import zingg.common.client.ZinggClientException;
import zingg.common.client.options.ZinggOptions;

public abstract class FindAndLabeller<S, D, R, C, T> extends ZinggBase<S,D,R,C,T> {
	private static final long serialVersionUID = 1L;
	protected static String name = "zingg.FindAndLabeller";
	public static final Log LOG = LogFactory.getLog(FindAndLabeller.class);

	protected TrainingDataFinder<S, D, R, C, T> finder;
	protected Labeller<S, D, R, C, T> labeller;

	public FindAndLabeller() {
		setZinggOption(ZinggOptions.FIND_AND_LABEL);
	}

	@Override
	public void init(IZArgs args, S s, ClientOptions options) throws ZinggClientException {
		finder.init(args,s,options);
		labeller.init(args,s,options);
		super.init(args,s,options);
	}

	@Override
	public void execute() throws ZinggClientException {
		finder.execute();
		labeller.execute();
	}

	public void setLabeller(Labeller<S, D, R, C, T> labeller){
		this.labeller = labeller;
	}

	public Labeller<S, D, R, C, T> getLabeller(){
		return this.labeller;
	}

	public TrainingDataFinder<S, D, R, C, T> getFinder() {
		return finder;
	}

	public void setFinder(TrainingDataFinder<S, D, R, C, T> finder) {
		this.finder = finder;
	}

	

}
