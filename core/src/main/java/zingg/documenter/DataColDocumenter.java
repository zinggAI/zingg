package zingg.documenter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

import zingg.client.Arguments;
import zingg.client.ZinggClientException;

public class DataColDocumenter extends DocumenterBase {
	protected static String name = "zingg.DataColDocumenter";
	public static final Log LOG = LogFactory.getLog(DataColDocumenter.class);
	
	public DataColDocumenter(SparkSession spark, Arguments args) {
		super(spark, args);
	}

	public void process(Dataset<Row> data) throws ZinggClientException {
	}

}