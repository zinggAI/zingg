package zingg.documenter;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

import zingg.client.Arguments;
import zingg.client.FieldDefinition;
import zingg.client.MatchType;
import zingg.client.ZinggClientException;
import zingg.util.PipeUtil;

public class ColumnDocumenter extends DocumenterBase {
	protected static String name = "zingg.ColumnDocumenter";
	public static final Log LOG = LogFactory.getLog(ColumnDocumenter.class);

	private final String COLUMN_DOC_TEMPLATE = "columnDocTemplate.ftlh";
	private final String Z_COLUMN_TEMPLATE = "zColumnTemplate.ftlh";
	protected StopWordsDocumenter stopWordsDoc;

	public ColumnDocumenter(SparkSession spark, Arguments args) {
		super(spark, args);
		stopWordsDoc = new StopWordsDocumenter(spark, args);
	}

	public void process() throws ZinggClientException {
		createColumnDocuments();
	}

	private void createColumnDocuments() throws ZinggClientException {
		LOG.info("Column Documents generation starts");

		Dataset<Row> data = PipeUtil.read(spark, false, false, args.getData());
		LOG.info("Read input data : " + data.count());

		String columnsDir = args.getZinggDocDir();
		checkAndCreateDir(columnsDir);

		for (FieldDefinition field: args.getFieldDefinition()) {
			if ((field.getMatchType() == null || field.getMatchType().equals(MatchType.DONT_USE))) {
				prepareAndWriteColumnDocument(spark.emptyDataFrame(), field.fieldName, columnsDir);
				continue;
			}
			prepareAndWriteColumnDocument(data, field.fieldName, columnsDir);
 		}

		for (String col: getZColumnList()) {
			prepareAndWriteColumnDocument(spark.emptyDataFrame(), col, columnsDir);
		}

		LOG.info("Column Documents generation finishes");
	}

	private void prepareAndWriteColumnDocument(Dataset<Row> data, String fieldName, String columnsDir) throws ZinggClientException {
		Map<String, Object> root = new HashMap<String, Object>();
		root.put(TemplateFields.TITLE, fieldName);
		root.put(TemplateFields.MODEL_ID, args.getModelId());		

		String filenameHTML = columnsDir + fieldName + ".html";
		if (isZColumn(fieldName)) {
			writeDocument(Z_COLUMN_TEMPLATE, root, filenameHTML);
		} else {
			root = stopWordsDoc.addStopWords(data, fieldName, root);
			writeDocument(COLUMN_DOC_TEMPLATE, root, filenameHTML);
		}
	}
}
