package zingg.common.core.executor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import zingg.common.client.Arguments;
import zingg.common.client.ZinggClientException;
import zingg.common.client.ZinggOptions;

public abstract class FindAndLabeller<S, D, R, C, T> extends Labeller<S, D, R, C, T> {
	private static final long serialVersionUID = 1L;
	protected static String name = "zingg.FindAndLabeller";
	public static final Log LOG = LogFactory.getLog(FindAndLabeller.class);

	protected TrainingDataFinder<S, D, R, C, T> finder;

	public FindAndLabeller() {
		setZinggOptions(ZinggOptions.FIND_AND_LABEL);
	}

	@Override
	public void init(Arguments args, String license) throws ZinggClientException {
		finder.init(args, license);
		super.init(args, license);
	}

	@Override
	public void execute() throws ZinggClientException {
		finder.execute();
		super.execute();
	}

}
