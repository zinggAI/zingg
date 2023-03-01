package zingg.recommender;

import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import zingg.client.Arguments;
import zingg.client.ZFrame;
import zingg.client.ZinggClientException;
import zingg.client.util.ColName;
import zingg.common.Context;

public abstract class StopWordsRecommender<S,D,R,C,T> {
	private static final String REGEX_WHITESPACE = "\\s+";
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
	
	protected ZFrame<D,R,C> filterOnThreshold(ZFrame<D,R,C> zDF, double threshold, String countColName) {
		return zDF.filter(zDF.gt(countColName,threshold)).coalesce(1);
	}

	protected double getThreshold(ZFrame<D,R,C> zDF, String countColName) {
		double totalCount = zDF.aggSum(countColName);
		double threshold = totalCount * args.getStopWordsCutoff();
		return threshold;		
	}

	protected ZFrame<D,R,C> addCountColumn(ZFrame<D,R,C> zDF, String fieldName, String resultColName) {
		return zDF.groupByCount(fieldName, resultColName);
	}

	protected ZFrame<D,R,C> convertToRows(ZFrame<D,R,C> zDF, String fieldName, String resultColName){
		return zDF.explode(fieldName, resultColName);
	}

	protected ZFrame<D,R,C> splitFieldOnWhiteSpace(ZFrame<D,R,C> zDF, String fieldName, String resultColName){
		return zDF.split(fieldName, REGEX_WHITESPACE, resultColName);
	}

	protected ZFrame<D,R,C> removeEmpty(ZFrame<D,R,C> zDF, String wordColName) {
		return zDF.filter(zDF.notEqual(wordColName,""));
	}

	public ZFrame<D,R,C> findStopWords(ZFrame<D,R,C> zDF, String fieldName) {
		LOG.debug("Field: " + fieldName);
		
		if(!zDF.isEmpty()) {
			zDF = splitFieldOnWhiteSpace(zDF, fieldName, ColName.COL_SPLIT);
			zDF = convertToRows(zDF,ColName.COL_SPLIT,ColName.COL_WORD);
			zDF = removeEmpty(zDF,ColName.COL_WORD);
			zDF = addCountColumn(zDF,ColName.COL_WORD, ColName.COL_COUNT);
			double threshold = getThreshold(zDF, ColName.COL_COUNT);
			zDF = filterOnThreshold(zDF, threshold, ColName.COL_COUNT);
		}
		
		return zDF;		

	}
}