package zingg.common.core.executor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import zingg.common.client.Arguments;
import zingg.common.client.ZinggClientException;
import zingg.common.client.ZinggOptions;

public abstract class FindAndLabeller<S, D, R, C, T> extends ZinggBase<S, D, R, C, T> {
	private static final long serialVersionUID = 1L;
	protected static String name = "zingg.FindAndLabeller";
	public static final Log LOG = LogFactory.getLog(FindAndLabeller.class);

	protected TrainingDataFinder<S, D, R, C, T> finder;
	protected Labeller<S, D, R, C, T> labeller;

	public FindAndLabeller() {
		setZinggOptions(ZinggOptions.FIND_AND_LABEL);
	}

	@Override
	public void init(Arguments args) throws ZinggClientException {
		finder.init(args);
		labeller.init(args);
		super.init(args);
	}

	@Override
	public void execute() throws ZinggClientException {
		finder.execute();
		labeller.execute();
	}

}
