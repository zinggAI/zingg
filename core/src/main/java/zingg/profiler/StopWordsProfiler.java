package zingg.profiler;

import static org.apache.spark.sql.functions.desc;
import static org.apache.spark.sql.functions.explode;
import static org.apache.spark.sql.functions.split;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

import zingg.client.Arguments;
import zingg.client.ZinggClientException;
import zingg.util.PipeUtil;

public class StopWordsProfiler extends ProfilerBase {
	protected static String name = "zingg.StopWordsProfiler";
	public static final Log LOG = LogFactory.getLog(StopWordsProfiler.class);
 
	public StopWordsProfiler(SparkSession spark, Arguments args) {
		super(spark, args);
		checkAndCreateDir(getStopWordsDir());
	}

	public void process(Dataset<Row> data) throws ZinggClientException {
		//createStopWordsDocuments(data);
	}

	public void createStopWordsDocument(Dataset<Row> data, String fieldName, JavaSparkContext ctx) throws ZinggClientException {
		String filenameCSV = getStopWordsDir() + fieldName;
		data = findStopWords(data, fieldName);
		PipeUtil.write(data, args, ctx, PipeUtil.getStopWordsPipe(args, filenameCSV));
	}

	private Dataset<Row> findStopWords(Dataset<Row> data, String fieldName) {
		LOG.debug("Field: " + fieldName);
		if(!data.isEmpty()) {
			data = data.select(split(data.col(fieldName), "\\s+").as("split"));
			data = data.select(explode(data.col("split")).as("word"));
			data = data.filter(data.col("word").notEqual(""));
			data = data.groupBy("word").count().orderBy(desc("count"));
			data = data.limit(Math.round(data.count()*args.getStopWordsCutoff()));
		}
		return data;
	}

	public String getStopWordsDir() {
		return args.getZinggBaseModelDir() + "/stopWords/";
	}
}
