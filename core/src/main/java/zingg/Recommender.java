package zingg;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import zingg.client.ZinggClientException;
import zingg.client.ZinggOptions;
import zingg.profiler.DataProfiler;

public class Recommender extends ZinggBase {

	protected static String name = "zingg.Recommender";
	public static final Log LOG = LogFactory.getLog(Recommender.class);

	public Recommender() {
		setZinggOptions(ZinggOptions.RECOMMEND);
 	}

	public void execute() throws ZinggClientException {
		try {
			LOG.info("Recommender starts");

			//profiling of data
			DataProfiler dataProfile = new DataProfiler(spark, ctx, args);
			dataProfile.process();

			LOG.info("Recommender finishes");
		} catch (Exception e) {
			e.printStackTrace();
			throw new ZinggClientException(e.getMessage());
		}
	}
}
