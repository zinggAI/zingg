package zingg.profiler;

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
import zingg.util.PipeUtil;

public class StopWordsProfiler{
	private static final String COL_COUNT = "count";
	private static final String COL_WORD = "word";
	private static final String COL_SPLIT = "split";
	public static final Log LOG = LogFactory.getLog(StopWordsProfiler.class);
	protected SparkSession spark;
	public Arguments args;

	public StopWordsProfiler(SparkSession spark, Arguments args) {
		this.spark = spark;
		this.args = args;
	}

	public void createStopWordsDocument(Dataset<Row> data, String fieldName, JavaSparkContext ctx) throws ZinggClientException {
		String filenameCSV = args.getStopWordsDir() + fieldName;
		data = findStopWords(data, fieldName);
		PipeUtil.write(data, args, ctx, PipeUtil.getStopWordsPipe(args, filenameCSV));
	}

	public Dataset<Row> findStopWords(Dataset<Row> data, String fieldName) {
		LOG.debug("Field: " + fieldName);
		if(!data.isEmpty()) {
			data = data.select(split(data.col(fieldName), "\\s+").as(COL_SPLIT));
			data = data.select(explode(data.col(COL_SPLIT)).as(COL_WORD));
			data = data.filter(data.col(COL_WORD).notEqual(""));
			data = data.groupBy(COL_WORD).count();
			//LOG.info("Approximate Count: " + DSUtil.approxCount(data,5000,0.90));
			long count = data.agg(sum(COL_COUNT)).collectAsList().get(0).getLong(0);
			double threshold = count * args.getStopWordsCutoff();
			data = data.filter(data.col(COL_COUNT).gt(threshold));
			data = data.coalesce(1);
		}
		return data;
	}
}
