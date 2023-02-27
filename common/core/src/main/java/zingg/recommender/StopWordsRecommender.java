package zingg.recommender;

import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import zingg.client.Arguments;
import zingg.client.ZFrame;
import zingg.client.ZinggClientException;
import zingg.common.Context;

public abstract class StopWordsRecommender<S,D,R,C,T> {
	public static final Log LOG = LogFactory.getLog(StopWordsRecommender.class);
	protected Context<S,D,R,C,T> context;
	protected ZFrame<D,R,C> data;
	public Arguments args;

	public StopWordsRecommender(Context<S,D,R,C,T> context,Arguments args) {
		this.context = context;
		this.args = args;
	}

	public void process() throws ZinggClientException {
		
		LOG.info("Data recommender starts");

		try {
			data = context.getPipeUtil().read(false, false, args.getData());
		} catch (ZinggClientException e) {
			LOG.warn("No data has been found");
		}
		if (!data.isEmpty()) {
			createStopWordsDocuments(data, args.getColumn());
		} else {
			LOG.info("No data recommendation generated");
		}
		LOG.info("Data recommender finishes");
		
	}

	public void createStopWordsDocuments(ZFrame<D,R,C> data, String fieldName) throws ZinggClientException {
		
		if (!data.isEmpty()) {
			if (args.getColumn() != null) {
				if(Arrays.asList(data.columns()).contains(args.getColumn())) {
					String filenameCSV = args.getStopWordsDir() + fieldName;
					data = findStopWords(data, fieldName);
					context.getPipeUtil().write(data, args, context.getPipeUtil().getStopWordsPipe(args, filenameCSV));
				} else {
					LOG.info("An invalid column name - " + args.getColumn() + " entered. Please provide valid column name.");
				}
			} else {
				LOG.info("Please provide '--column <columnName>' option at command line to generate stop words for that column.");
			}
		} else {
			LOG.info("No stopwords generated");
		}
		
 	}

	public abstract ZFrame<D,R,C> findStopWords(ZFrame<D,R,C> data, String fieldName);
}