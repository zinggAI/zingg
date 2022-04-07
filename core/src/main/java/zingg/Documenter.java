package zingg;

import static org.apache.spark.sql.functions.desc;
import static org.apache.spark.sql.functions.explode;
import static org.apache.spark.sql.functions.split;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

import zingg.client.ZFrame;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
import zingg.client.FieldDefinition;
import zingg.client.MatchType;
import zingg.client.ZinggClientException;
import zingg.client.ZinggOptions;
import zingg.client.util.ColName;
import zingg.util.DSUtil;
import zingg.util.PipeUtilBase;
import zingg.util.RowAdapter;
import zingg.util.RowWrapper;

public abstract class Documenter<S,D,R,C,T1,T2> extends ZinggBase<S,D,R,C,T1,T2> {

	protected static String name = "zingg.Documenter";
	public static final Log LOG = LogFactory.getLog(Documenter.class);
	public static Configuration config;

	private final String CSV_TEMPLATE = "stopWordsCSV.ftlh";
	private final String HTML_TEMPLATE = "stopWordsHTML.ftlh";

	public Documenter() {
		setZinggOptions(ZinggOptions.GENERATE_DOCS);
		config = createConfigurationObject();
	}

	public void execute() throws ZinggClientException {
		try {
			LOG.info("Document generation in progress");
			ZFrame<D,R,C> markedRecords = getPipeUtil().read(false, false, getPipeUtil().getTrainingDataMarkedPipe(args));
			markedRecords = markedRecords.cache();
			//List<Column> displayCols = DSUtil.getFieldDefColumns(markedRecords, args, false);
			List<R> clusterIDs = markedRecords.select(ColName.CLUSTER_COLUMN).distinct().collectAsList();
			int totalPairs = clusterIDs.size();
			/* Create a data-model */
			Map<String, Object> root = new HashMap<String, Object>();
			root.put("modelId", args.getModelId());
			root.put("clusters", markedRecords.collectAsList());
			root.put("numColumns", markedRecords.columns().length);
			root.put("columns", markedRecords.columns());
			root.put("fieldDefinitionCount", args.getFieldDefinition().size());
			buildAndWriteHTML(root);
			extractStopWords();
		} catch (Exception e) {
			e.printStackTrace();
			throw new ZinggClientException(e.getMessage());
		}
	}

	public void buildAndWriteHTML(Map<String, Object> root) throws Exception {

		Configuration cfg = getTemplateConfig();

		/* Get the template (uses cache internally) */
		Template temp = cfg.getTemplate("model.ftlh");

		/* Merge data-model with template */
		// Writer out = new OutputStreamWriter(System.out);
		Writer file = new FileWriter(new File(args.getZinggDocFile()));
		// StringWriter writer = new StringWriter();
		temp.process(root, file);
		// Note: Depending on what `out` is, you may need to call `out.close()`.
		// This is usually the case for file output, but not for servlet output.
		// file.flush();

		// List<String> textList = Collections.singletonList(writer.toString());

		// ZFrame<D,R,C>> data = spark.createDataset(textList, Encoders.STRING()).toDF();

		// PipeUtil.write(data, args, ctx, PipeUtil.getModelDocumentationPipe(args));
		file.close();
		// LOG.warn("written documentation at " + args.getZinggDocFile());
	}

	public Configuration getTemplateConfig() {
		if (config == null) {
			config = createConfigurationObject();
		}
		return config;
	}

	private Configuration createConfigurationObject() {
		/* ------------------------------------------------------------------------ */
		/* You should do this ONLY ONCE in the whole application life-cycle: */

		/* Create and adjust the configuration singleton */
		Configuration cfg = new Configuration(Configuration.VERSION_2_3_29);
		cfg.setClassForTemplateLoading(this.getClass(), "/");
		// cfg.setDirectoryForTemplateLoading(new File("/where/you/store/templates"));
		// Recommended settings for new projects:
		cfg.setDefaultEncoding("UTF-8");
		cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
		cfg.setLogTemplateExceptions(false);
		cfg.setWrapUncheckedExceptions(true);
		cfg.setFallbackOnNullLoopVariable(false);
		cfg.setObjectWrapper(new RowWrapper(cfg.getIncompatibleImprovements()));

		return cfg;
	}

	private void extractStopWords() throws ZinggClientException {
		LOG.info("Stop words generation starts");
		ZFrame<D,R,C> data = getPipeUtil().read(false, false, args.getData());
		LOG.warn("Read input data : " + data.count());

		List<FieldDefinition> fields = getDSUtil().getFieldDefinitionFiltered(args, MatchType.DONT_USE);
		for (FieldDefinition field : fields) {
			findAndWriteStopWords(data, field);
		}
		LOG.info("Stop words generation finishes");
	}

	private void findAndWriteStopWords(ZFrame<D,R,C> data, FieldDefinition field) throws ZinggClientException {
		String stopWordsDir = args.getZinggDocDir() + "/stopWords/";
		String columnsDir = args.getZinggDocDir() + "/columns/";

		checkAndCreateDir(stopWordsDir);
		checkAndCreateDir(columnsDir);

		LOG.debug("Field: " + field.fieldName);
		/*
		data = data.select(split(data.col(field.fieldName), "\\s+").as("split"));
		data = data.select(explode(data.col("split")).as("word"));
		data = data.filter(data.col("word").notEqual(""));
		data = data.groupBy("word").count().orderBy(desc("count"));
		*/
		data = data.limit(Math.round(data.count()*args.getStopWordsCutoff()));
		String filenameCSV = stopWordsDir + field.fieldName + ".csv";
		String filenameHTML = columnsDir + field.fieldName + ".html";
		Map<String, Object> root = new HashMap<String, Object>();
		root.put("modelId", args.getModelId());
		root.put("stopWords", data.collectAsList());
		
		writeStopWords(CSV_TEMPLATE, root, filenameCSV);
		writeStopWords(HTML_TEMPLATE, root, filenameHTML);
	}

	private void checkAndCreateDir(String dirName) {
		File directory = new File(dirName);
		if (!directory.exists()) {
			directory.mkdirs();
		}
	}

	public void writeStopWords(String template, Map<String, Object> root, String fileName)
			throws ZinggClientException {
		try {
			Configuration cfg = getTemplateConfig();
			Template temp = cfg.getTemplate(template);
			Writer file = new FileWriter(new File(fileName));
			temp.process(root, file);
			file.close();
		} catch (Exception e) {
			e.printStackTrace();
			throw new ZinggClientException(e.getMessage());
		}
	}
}
