package zingg.documenter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.StructField;

import zingg.client.Arguments;
import zingg.client.ZinggClientException;

public class DataColDocumenter extends DocumenterBase {
	protected static String name = "zingg.DataColDocumenter";
	public static final Log LOG = LogFactory.getLog(DataColDocumenter.class);
	StopWordsDocumenter stopWordsDoc;
	
	public DataColDocumenter(SparkSession spark, Arguments args) {
		super(spark, args);
		stopWordsDoc = new StopWordsDocumenter(spark, args);
	}

	public void process(Dataset<Row> data) throws ZinggClientException {
		createStopWordsDocuments(data);
	}

	private void createStopWordsDocuments(Dataset<Row> data) throws ZinggClientException {
		if (!data.isEmpty()) {
			String columnsDir = args.getZinggDocDir();
			checkAndCreateDir(columnsDir);

			for (StructField field: data.schema().fields()) {
				stopWordsDoc.createStopWordsDocument(data, field.name(), columnsDir);
			}
		} else {
			LOG.info("No Stop Words document generated");
		}
 	}
}
