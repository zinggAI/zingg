package zingg.common.core.recommender;

import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import zingg.common.client.Arguments;
import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;
import zingg.common.client.ZinggClientException;
import zingg.common.client.util.ColName;
import zingg.common.core.Context;

public class StopWordsRecommender<S,D,R,C,T> {
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

	public ZFrame<D,R,C> findStopWords(ZFrame<D,R,C> data, String fieldName) {
		LOG.debug("Field: " + fieldName);
		
		System.out.println("inside findStopWords");
		
//		if(!data.isEmpty()) {
//			data = data.select(split(data.col(fieldName), "\\s+").as(ColName.COL_SPLIT));
//			data = data.select(explode(data.col(ColName.COL_SPLIT)).as(ColName.COL_WORD));
//			data = data.filter(data.col(ColName.COL_WORD).notEqual(""));
//			data = data.groupBy(ColName.COL_WORD).count().withColumnRenamed("count", ColName.COL_COUNT);
//			long count = data.agg(sum(ColName.COL_COUNT)).collectAsList().get(0).getLong(0);
//			double threshold = count * args.getStopWordsCutoff();
//			data = data.filter(data.col(ColName.COL_COUNT).gt(threshold));
//			data = data.coalesce(1);
//		}
		
		return data;
	}
}
