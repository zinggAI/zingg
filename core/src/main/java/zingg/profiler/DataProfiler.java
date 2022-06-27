package zingg.profiler;

import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

import zingg.client.Arguments;
import zingg.client.ZinggClientException;
import zingg.util.PipeUtil;

public class DataProfiler {

	public static final Log LOG = LogFactory.getLog(DataProfiler.class);
	StopWordsProfiler stopWordsProfile;
	protected Dataset<Row> data;
	protected SparkSession spark;
	JavaSparkContext ctx;
	public Arguments args;

	public DataProfiler(SparkSession spark, JavaSparkContext ctx, Arguments args) {
		this.spark = spark;
		this.ctx = ctx;
		this.args = args;
		stopWordsProfile = new StopWordsProfiler(spark, args);
	}

	public void process() throws ZinggClientException {
		LOG.info("Data profiling starts");

		try {
			data = PipeUtil.read(spark, false, false, args.getData());
		} catch (ZinggClientException e) {
			LOG.warn("No data has been found");
		}
		if (!data.isEmpty()) {
			createStopWordsDocuments(data);
		} else {
			LOG.info("No data profile generated");
		}
		LOG.info("Data profiling finishes");
	}

	public void createStopWordsDocuments(Dataset<Row> data) throws ZinggClientException {
		if (!data.isEmpty()) {
			if (args.getColumn() != null) {
				if(Arrays.asList(data.schema().fieldNames()).contains(args.getColumn())) {
					stopWordsProfile.createStopWordsDocument(data, args.getColumn(), ctx);
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
}
