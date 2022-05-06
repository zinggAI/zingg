package zingg.documenter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

import zingg.client.Arguments;
import zingg.client.ZinggClientException;
import zingg.client.util.ColName;
import zingg.util.PipeUtil;

public class ModelDocumenter extends DocumenterBase {

	protected static String name = "zingg.ModelDocumenter";
	public static final Log LOG = LogFactory.getLog(ModelDocumenter.class);

	private final String MODEL_TEMPLATE = "model.ftlh";

	public ModelDocumenter(SparkSession spark, Arguments args) {
		super(spark, args);
	}

	public void process() throws ZinggClientException {
		try {
			LOG.info("Model document generation starts");

			Dataset<Row> markedRecords = spark.emptyDataFrame();
			try {
				markedRecords = PipeUtil.read(spark, false, false, PipeUtil.getTrainingDataMarkedPipe(args));
			} catch (Exception e) {
				LOG.warn("No marked record has been found");
			}

			markedRecords = markedRecords.cache();
			/* Create a data-model */
			Map<String, Object> root = new HashMap<String, Object>();
			root.put(TemplateFields.MODEL_ID, args.getModelId());

			if (!markedRecords.isEmpty()) {
				root.put(TemplateFields.CLUSTERS, markedRecords.collectAsList());
				root.put(TemplateFields.NUM_COLUMNS, markedRecords.columns().length);
				root.put(TemplateFields.COLUMNS, markedRecords.columns());
				root.put(TemplateFields.ISMATCH_COLUMN_INDEX,
						markedRecords.schema().fieldIndex(ColName.MATCH_FLAG_COL));
				root.put(TemplateFields.CLUSTER_COLUMN_INDEX,
						markedRecords.schema().fieldIndex(ColName.CLUSTER_COLUMN));
			} else {
				// fields required to generate basic document
				List<String> list = args.getFieldDefinition().stream().map(fd -> fd.getFieldName())
						.collect(Collectors.toList());
				List<String> columnList = new ArrayList<String>(getZColumnList());
				columnList.addAll(list);
				root.put(TemplateFields.NUM_COLUMNS, columnList.size());
				root.put(TemplateFields.COLUMNS, columnList.toArray());
				root.put(TemplateFields.CLUSTERS, Collections.emptyList());
				root.put(TemplateFields.ISMATCH_COLUMN_INDEX, 0);
				root.put(TemplateFields.CLUSTER_COLUMN_INDEX, 1);
			}
			checkAndCreateDir(args.getZinggDocDir());
			writeDocument(MODEL_TEMPLATE, root, args.getZinggDocFile());
			LOG.info("Model document generation finishes");
		} catch (Exception e) {
			e.printStackTrace();
			throw new ZinggClientException(e.getMessage());
		}
	}
}
