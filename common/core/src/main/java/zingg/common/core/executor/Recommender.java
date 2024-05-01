package zingg.common.core.executor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import zingg.common.client.ZinggClientException;
import zingg.common.client.options.ZinggOptions;
import zingg.common.core.recommender.StopWordsRecommender;

public abstract class Recommender<S,D,R,C, T> extends ZinggBase<S,D,R,C,T> {

	protected static String name = "zingg.Recommender";
	public static final Log LOG = LogFactory.getLog(Recommender.class);

	public Recommender() {
		setZinggOption(ZinggOptions.RECOMMEND);
 	}

	public void execute() throws ZinggClientException {
		try {
			LOG.info("Recommender starts");

			//Recommendations out of data
			StopWordsRecommender<S,D,R,C, T> stopWordsRecommender = getStopWordsRecommender();
			//new StopWordsRecommender<S,D,R,C, T>(getContext(),args);
			stopWordsRecommender.process();

			LOG.info("Recommender finishes");
		} catch (Exception e) {
			e.printStackTrace();
			throw new ZinggClientException(e.getMessage());
		}
	}

	/**
	 * To be implemented by concrete implementation of Spark/Snow etc.
	 * @return
	 */
	public abstract StopWordsRecommender<S,D,R,C, T> getStopWordsRecommender();

}