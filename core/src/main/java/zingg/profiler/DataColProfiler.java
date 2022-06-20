package zingg.profiler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.StructField;

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

	private void createStopWordsDocuments(Dataset<Row> data) throws ZinggClientException {
		if (!data.isEmpty()) {
			String columnsDir = args.getZinggDocDir();
			checkAndCreateDir(columnsDir);

			for (StructField field: data.schema().fields()) {
				stopWordsProfile.createStopWordsDocument(data, field.name(), ctx);
			}
		} else {
			LOG.info("No Stop Words document generated");
		}
 	}
}
