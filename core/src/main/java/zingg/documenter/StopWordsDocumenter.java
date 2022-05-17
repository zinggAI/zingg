package zingg.documenter;

import static org.apache.spark.sql.functions.desc;
import static org.apache.spark.sql.functions.explode;
import static org.apache.spark.sql.functions.split;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

import zingg.client.Arguments;
import zingg.client.ZinggClientException;

public class StopWordsDocumenter extends DocumenterBase {
	protected static String name = "zingg.StopWordsDocumenter";
	public static final Log LOG = LogFactory.getLog(StopWordsDocumenter.class);
	private final String STOP_WORDS_CSV_TEMPLATE = "stopWordsCSVTemplate.ftl";

	public StopWordsDocumenter(SparkSession spark, Arguments args) {
		super(spark, args);
	}

	public Map<String, Object> addStopWords(Dataset<Row> data, String fieldName, Map<String, Object> params) throws ZinggClientException {
		LOG.debug("Field: " + fieldName);
		if(!data.isEmpty()) {
			data = data.select(split(data.col(fieldName), "\\s+").as("split"));
			data = data.select(explode(data.col("split")).as("word"));
			data = data.filter(data.col("word").notEqual(""));
			data = data.groupBy("word").count().orderBy(desc("count"));
			data = data.limit(Math.round(data.count()*args.getStopWordsCutoff()));
		}
		params.put("stopWords", data.collectAsList());
		
		writeStopWordsDocument(fieldName, params);

		return params;
	}

	public void writeStopWordsDocument(String fieldName, Map<String, Object> root) throws ZinggClientException {
		String stopWordsDir = args.getZinggDocDir() + "/stopWords/";
		checkAndCreateDir(stopWordsDir);
		String filenameCSV = stopWordsDir + fieldName + ".csv";
		writeDocument(STOP_WORDS_CSV_TEMPLATE, root, filenameCSV);
	}
}
