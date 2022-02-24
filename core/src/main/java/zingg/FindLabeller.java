package zingg;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import zingg.client.Arguments;
import zingg.client.ZinggClientException;
import zingg.client.ZinggOptions;

public class FindLabeller extends Labeller {
	protected static String name = "zingg.FindLabeller";
	public static final Log LOG = LogFactory.getLog(FindLabeller.class);

	private TrainingDataFinder finder;

	public FindLabeller() {
		setZinggOptions(ZinggOptions.FIND_LABEL);
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
