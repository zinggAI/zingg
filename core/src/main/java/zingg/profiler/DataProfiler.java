package zingg.profiler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

import zingg.client.Arguments;
import zingg.client.ZinggClientException;
import zingg.util.PipeUtil;

public class DataProfiler extends ProfilerBase {
	protected static String name = "zingg.DataProfiler";

	public static final Log LOG = LogFactory.getLog(DataProfiler.class);
	private DataColProfiler dataColProfiler;
	protected Dataset<Row> data;

	public DataProfiler(SparkSession spark, JavaSparkContext ctx, Arguments args) {
		super(spark, args);
		dataColProfiler = new DataColProfiler(spark, ctx, args);
	}
	
	public void process() throws ZinggClientException {
		LOG.info("Data profiling starts");

		try {
			data = PipeUtil.read(spark, false, false, args.getData());
		} catch (ZinggClientException e) {
			LOG.warn("No data has been found");
		}
		if (!data.isEmpty()) {
			dataColProfiler.process(data);
		} else {
			LOG.info("No data profile generated");
		}
		LOG.info("Data profiling finishes");
	}
}
