package zingg.recommender;

import java.util.Arrays;

import static org.apache.spark.sql.functions.explode;
import static org.apache.spark.sql.functions.split;
import static org.apache.spark.sql.functions.sum;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

import zingg.client.Arguments;
import zingg.client.ZinggClientException;
import zingg.client.util.ColName;
import zingg.util.PipeUtil;

public class StopWordsRecommender {
	public static final Log LOG = LogFactory.getLog(StopWordsRecommender.class);
	protected SparkSession spark;
	protected Dataset<Row> data;
	JavaSparkContext ctx;
	public Arguments args;

	public StopWordsRecommender(SparkSession spark, JavaSparkContext ctx, Arguments args) {
		this.spark = spark;
		this.ctx = ctx;
		this.args = args;
	}

	public void process() throws ZinggClientException {
		LOG.info("Data recommender starts");

		try {
			data = PipeUtil.read(spark, false, false, args.getData());
		} catch (ZinggClientException e) {
			LOG.warn("No data has been found");
		}
		if (!data.isEmpty()) {
			createStopWordsDocuments(data, args.getColumn(), ctx);
		} else {
			LOG.info("No data recommendation generated");
		}
		LOG.info("Data recommender finishes");
	}

	public void createStopWordsDocuments(Dataset<Row> data, String fieldName, JavaSparkContext ctx) throws ZinggClientException {
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
 	}

	public Dataset<Row> findStopWords(Dataset<Row> data, String fieldName) {
		LOG.debug("Field: " + fieldName);
		if(!data.isEmpty()) {
			data = data.select(split(data.col(fieldName), "\\s+").as(ColName.COL_SPLIT));
			data = data.select(explode(data.col(ColName.COL_SPLIT)).as(ColName.COL_WORD));
			data = data.filter(data.col(ColName.COL_WORD).notEqual(""));
			data = data.groupBy(ColName.COL_WORD).count();
			long count = data.agg(sum(ColName.COL_COUNT)).collectAsList().get(0).getLong(0);
			double threshold = count * args.getStopWordsCutoff();
			data = data.filter(data.col(ColName.COL_COUNT).gt(threshold));
			data = data.coalesce(1);
		}
		return data;
	}
}