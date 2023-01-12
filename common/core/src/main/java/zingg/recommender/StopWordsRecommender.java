package zingg.recommender;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import zingg.client.Arguments;
import zingg.client.ZFrame;
import zingg.client.ZinggClientException;

public class StopWordsRecommender<S,D,R,C,T> {
	public static final Log LOG = LogFactory.getLog(StopWordsRecommender.class);
	protected S spark;
	protected ZFrame<D,R,C> data;
	//JavaSparkContext ctx;
	public Arguments args;

	public StopWordsRecommender(S spark,Arguments args) {
		this.spark = spark;
		//this.ctx = ctx;
		this.args = args;
	}

	public void process() throws ZinggClientException {
		/*
		LOG.info("Data recommender starts");

		try {
			data = getPipeUtil().read(false, false, args.getData());
		} catch (ZinggClientException e) {
			LOG.warn("No data has been found");
		}
		if (!data.isEmpty()) {
			createStopWordsDocuments(data, args.getColumn(), ctx);
		} else {
			LOG.info("No data recommendation generated");
		}
		LOG.info("Data recommender finishes");
		*/
	}

	public void createStopWordsDocuments(ZFrame<D,R,C> data, String fieldName) throws ZinggClientException {
		/*
		if (!data.isEmpty()) {
			if (args.getColumn() != null) {
				if(Arrays.asList(data.schema().fieldNames()).contains(args.getColumn())) {
					String filenameCSV = args.getStopWordsDir() + fieldName;
					data = findStopWords(data, fieldName);
					PipeUtil.write(data, args, ctx, PipeUtil.getStopWordsPipe(args, filenameCSV));
				} else {
					LOG.info("An invalid column name - " + args.getColumn() + " entered. Please provide valid column name.");
				}
			} else {
				LOG.info("Please provide '--column <columnName>' option at command line to generate stop words for that column.");
			}
		} else {
			LOG.info("No stopwords generated");
		}
		*/
 	}

	public ZFrame<D,R,C> findStopWords(ZFrame<D,R,C> data, String fieldName) {
		LOG.debug("Field: " + fieldName);
		/* 
		if(!data.isEmpty()) {
			data = data.select(split(data.col(fieldName), "\\s+").as(ColName.COL_SPLIT));
			data = data.select(explode(data.col(ColName.COL_SPLIT)).as(ColName.COL_WORD));
			data = data.filter(data.col(ColName.COL_WORD).notEqual(""));
			data = data.groupBy(ColName.COL_WORD).count().withColumnRenamed("count", ColName.COL_COUNT);
			long count = data.agg(sum(ColName.COL_COUNT)).collectAsList().get(0).getLong(0);
			double threshold = count * args.getStopWordsCutoff();
			data = data.filter(data.col(ColName.COL_COUNT).gt(threshold));
			data = data.coalesce(1);
		}
		*/
		return data;
	}
}