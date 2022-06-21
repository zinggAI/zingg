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

public class DataColProfiler extends ProfilerBase {
	protected static String name = "zingg.DataColProfiler";
	public static final Log LOG = LogFactory.getLog(DataColProfiler.class);
	StopWordsProfiler stopWordsProfile;
	JavaSparkContext ctx;
	
	public DataColProfiler(SparkSession spark, JavaSparkContext ctx, Arguments args) {
		super(spark, args);
		this.ctx = ctx;
		stopWordsProfile = new StopWordsProfiler(spark, args);
	}

	public void process(Dataset<Row> data) throws ZinggClientException {
		createStopWordsDocuments(data);
	}

	public void createStopWordsDocuments(Dataset<Row> data) throws ZinggClientException {
		if (!data.isEmpty()) {
			if (!args.getColumn().equals("")) {
				if(Arrays.asList(data.schema().fieldNames()).contains(args.getColumn())) {
					stopWordsProfile.createStopWordsDocument(data, args.getColumn(), ctx);
				} else {
					LOG.info("An invalid column name - " + args.getColumn() + " entered. Please provide valid column name.");
				}
			} else {
				LOG.info("Please provide '--column <columnName>' option at command line to generate stop words for that column.");
			}
		} else {
			LOG.info("No Stop Words document generated");
		}
 	}
}
