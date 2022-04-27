package zingg.documenter;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

import freemarker.template.Configuration;
import freemarker.template.Template;
import zingg.client.Arguments;
import zingg.client.ZinggClientException;
import zingg.client.util.ColName;
import zingg.util.DSUtil;
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

			File directory = new File(args.getZinggTrainingDataMarkedDir());
			if (!directory.exists()) {
				LOG.warn("Marked data folder(models/<model_id>/trainingData/marked) does not exist. Please run findTrainingData and/or label phases to mark records");
				return;
			}
			Dataset<Row> markedRecords = null;
			try {
				markedRecords = PipeUtil.read(spark, false, false, PipeUtil.getTrainingDataMarkedPipe(args));
			} catch (Exception e) {
				if (LOG.isDebugEnabled()) e.printStackTrace();
				LOG.warn("No marked record found or there is an issue reading marked records");
				return;
			}

			markedRecords = markedRecords.cache();
			List<Column> displayCols = DSUtil.getFieldDefColumns(markedRecords, args, false, args.getShowConcise());
			displayCols.add(0, markedRecords.col(ColName.MATCH_FLAG_COL));
			displayCols.add(1, markedRecords.col(ColName.CLUSTER_COLUMN));
			/* Create a data-model */
 			Map<String, Object> root = new HashMap<String, Object>();
			root.put("modelId", args.getModelId());
			root.put("clusters", markedRecords.collectAsList());
			root.put("numColumns", markedRecords.columns().length);
			root.put("columns", markedRecords.columns());
			root.put("fieldDefinitionCount", args.getFieldDefinition().size());
			root.put("isMatchColumnIndex", markedRecords.schema().fieldIndex(ColName.MATCH_FLAG_COL));
			root.put("clusterColumnIndex", markedRecords.schema().fieldIndex(ColName.CLUSTER_COLUMN));
			buildAndWriteHTML(root);
			LOG.info("Model document generation finishes");
		} catch (Exception e) {
			e.printStackTrace();
			throw new ZinggClientException(e.getMessage());
		}
	}

	public void buildAndWriteHTML(Map<String, Object> root) throws Exception {

		Configuration cfg = getTemplateConfig();

		/* Get the template (uses cache internally) */
		Template temp = cfg.getTemplate(MODEL_TEMPLATE);

		/* Merge data-model with template */
		// Writer out = new OutputStreamWriter(System.out);
		Writer file = new FileWriter(new File(args.getZinggDocFile()));
		// StringWriter writer = new StringWriter();
		temp.process(root, file);
		// Note: Depending on what `out` is, you may need to call `out.close()`.
		// This is usually the case for file output, but not for servlet output.
		// file.flush();

		// List<String> textList = Collections.singletonList(writer.toString());

		// Dataset<Row> data = spark.createDataset(textList, Encoders.STRING()).toDF();

		// PipeUtil.write(data, args, ctx, PipeUtil.getModelDocumentationPipe(args));
		file.close();
		// LOG.warn("written documentation at " + args.getZinggDocFile());
	}
}
