package zingg.documenter;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.StructField;

import zingg.client.Arguments;
import zingg.client.ZFrame;
import zingg.client.ZinggClientException;

public class ModelColDocumenter<S,D,R,C,T> extends DocumenterBase<S,D,R,C,T> {
	protected static String name = "zingg.ModelColDocumenter";
	public static final Log LOG = LogFactory.getLog(ModelColDocumenter.class);

	private final String COLUMN_DOC_TEMPLATE = "columnDocTemplate.ftlh";
	private final String Z_COLUMN_TEMPLATE = "zColumnTemplate.ftlh";

	public ModelColDocumenter(S session, Arguments args) {
		super(session, args);
	}

	public void process( ZFrame<D,R,C>  data) throws ZinggClientException {
		createColumnDocuments(data);
	}

	private void createColumnDocuments( ZFrame<D,R,C>  data) throws ZinggClientException {
		LOG.info("Column Documents generation starts");
		if (!data.isEmpty()) {
			String columnsDir = args.getZinggDocDir();
			checkAndCreateDir(columnsDir);
			for (StructField field: data.schema().fields()) {
				prepareAndWriteColumnDocument(field.name(), columnsDir);
			}
		}
		LOG.info("Column Documents generation finishes");
	}

	private void prepareAndWriteColumnDocument(String fieldName, String columnsDir) throws ZinggClientException {
		Map<String, Object> root = new HashMap<String, Object>();
		root.put(TemplateFields.TITLE, fieldName);
		root.put(TemplateFields.MODEL_ID, args.getModelId());

		String filenameHTML = columnsDir + fieldName + ".html";
		if (isZColumn(fieldName)) {
			writeDocument(Z_COLUMN_TEMPLATE, root, filenameHTML);
		} else {
			writeDocument(COLUMN_DOC_TEMPLATE, root, filenameHTML);
		}
	}
}
