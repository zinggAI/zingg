package zingg;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import zingg.client.Arguments;
import zingg.client.ZinggClientException;
import zingg.client.ZinggOptions;

public class FindAndLabeller extends Labeller {
	protected static String name = "zingg.FindAndLabeller";
	public static final Log LOG = LogFactory.getLog(FindAndLabeller.class);

	private TrainingDataFinder finder;

	public FindAndLabeller() {
		setZinggOptions(ZinggOptions.FIND_AND_LABEL);
		finder = new TrainingDataFinder();
	}

	@Override
	public void init(Arguments args, String license)
			throws ZinggClientException {
		super.init(args, license);
		finder.copyContext(this);
	}

	@Override
	public void execute() throws ZinggClientException {
		finder.execute();
		super.execute();
	}
}
