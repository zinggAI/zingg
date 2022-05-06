package zingg.documenter;

import static org.apache.spark.sql.functions.desc;
import static org.apache.spark.sql.functions.explode;
import static org.apache.spark.sql.functions.split;

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
import zingg.client.util.ColName;
import zingg.util.PipeUtil;

public class ColumnDocumenter extends DocumenterBase {
	protected static String name = "zingg.ColumnDocumenter";
	public static final Log LOG = LogFactory.getLog(ColumnDocumenter.class);

	private final String CSV_TEMPLATE = "stopWordsCSV.ftlh";
	private final String HTML_TEMPLATE = "stopWordsHTML.ftlh";

	public ColumnDocumenter(SparkSession spark, Arguments args) {
		super(spark, args);
	}

	public void process() throws ZinggClientException {
		createColumnDocuments();
	}

	private void createColumnDocuments() throws ZinggClientException {
		LOG.info("Column Documents generation starts");

		Dataset<Row> data = PipeUtil.read(spark, false, false, args.getData());
		LOG.info("Read input data : " + data.count());

		String stopWordsDir = args.getZinggDocDir() + "/stopWords/";
		String columnsDir = args.getZinggDocDir();
		checkAndCreateDir(stopWordsDir);
		checkAndCreateDir(columnsDir);

		for (FieldDefinition field: args.getFieldDefinition()) {
			if ((field.getMatchType() == null || field.getMatchType().equals(MatchType.DONT_USE))) {
				prepareAndWriteColumnDocument(spark.emptyDataFrame(), field.fieldName, stopWordsDir, columnsDir);
				continue;
			}
			prepareAndWriteColumnDocument(data, field.fieldName, stopWordsDir, columnsDir);
 		}

		for (String col: getZColumnList()) {
			prepareAndWriteColumnDocument(spark.emptyDataFrame(), col, stopWordsDir, columnsDir);
		}

		LOG.info("Column Documents generation finishes");
	}
	private void prepareAndWriteColumnDocument(Dataset<Row> data, String fieldName, String stopWordsDir, String columnsDir) throws ZinggClientException {
		Map<String, Object> root = new HashMap<String, Object>();
		root.put(TemplateFields.TITLE, fieldName);
		root.put(TemplateFields.MODEL_ID, args.getModelId());		
		root = addStopWords(data, fieldName, root);

		String filenameCSV = stopWordsDir + fieldName + ".csv";
		String filenameHTML = columnsDir + fieldName + ".html";
		writeDocument(CSV_TEMPLATE, root, filenameCSV);
		writeDocument(HTML_TEMPLATE, root, filenameHTML);
	}


	public Map<String, Object> addStopWords(Dataset<Row> data, String fieldName, Map<String, Object> params) {
		LOG.debug("Field: " + fieldName);
		if(!data.isEmpty()) {
			data = data.select(split(data.col(fieldName), "\\s+").as("split"));
			data = data.select(explode(data.col("split")).as("word"));
			data = data.filter(data.col("word").notEqual(""));
			data = data.groupBy("word").count().orderBy(desc("count"));
			data = data.limit(Math.round(data.count()*args.getStopWordsCutoff()));
		}
		params.put("stopWords", data.collectAsList());
		
		return params;
	}
}