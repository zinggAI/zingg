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
	private final String Z_COLUMN_TEMPLATE = "zColumnTemplate.ftlh";

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
			if ((field.getMatchType() == null || field.getMatchType().contains(MatchType.DONT_USE))) {
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

		String filenameCSV = stopWordsDir + fieldName + ".csv";
		String filenameHTML = columnsDir + fieldName + ".html";
		if (isZColumn(fieldName)) {
			root.put(TemplateFields.BASE_MESSAGE, getColumnBaseContent(fieldName));
			writeDocument(Z_COLUMN_TEMPLATE, root, filenameHTML);
		} else {
			root = addStopWords(data, fieldName, root);
			writeDocument(CSV_TEMPLATE, root, filenameCSV);
			writeDocument(HTML_TEMPLATE, root, filenameHTML);
		}
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

	public String getColumnBaseContent (String col) {
		String message = "The field '" + col + "' is internally used by Zingg.";

		LOG.info("colName: " + col);
		if (col.equals(ColName.CLUSTER_COLUMN)) {
			message = "z_cluster - identifies a group of records which match or don't match with each other. For each group, z_cluster is unique. Member records of a group share the same z_cluster.";
		} else if (col.equals(ColName.PREDICTION_COL)) {
			message = "z_prediction - what Zingg thinks about this group/pair of records - 0 for not a match, 1 for a match.";
		} else if (col.equals(ColName.SCORE_COL)) {
			message = "z_score - the probability of a pair of records matching. The higher the score, the more likely they are a match.";
		} else if (col.equals(ColName.MATCH_FLAG_COL)) {
			message = "z_isMatch - this is the label provided by the user.";
		} else if (col.equals(ColName.ID_COL)) {
			message = "z_id - an internal id given by Zingg to uniquely identify the record.";
		} else if (col.equals(ColName.SOURCE_COL)) {
			message = "z_source - the source of data as set in the name property of the data in the Zingg configuration file.";
		}

		return message;
	}
}