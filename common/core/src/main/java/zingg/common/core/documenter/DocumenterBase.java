package zingg.common.core.documenter;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.util.Map;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
import freemarker.template.Version;
import zingg.common.client.IArguments;
import zingg.common.client.ZinggClientException;
import zingg.common.client.util.ColName;
import zingg.common.core.context.Context;
import zingg.common.core.executor.ZinggBase;

public abstract class DocumenterBase<S,D,R,C,T> extends ZinggBase<S,D,R,C,T>{
	private static final long serialVersionUID = 1L;
	protected static Configuration config;

	public DocumenterBase(Context<S,D,R,C,T> context, IArguments args) {
		super.context = context;
		super.args = args;
		config = createConfigurationObject();
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
		cfg.setObjectWrapper(getRowWrapper(cfg.getIncompatibleImprovements()));
		cfg.setBooleanFormat("c");
		/* ------------------------------------------------------------------------ */
		/* You usually do these for MULTIPLE TIMES in the application life-cycle: */
		return cfg;
	}

	protected void writeDocument(String template, Map<String, Object> root, String fileName)
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

	protected void checkAndCreateDir(String dirName) {
		File directory = new File(dirName);
		if (!directory.exists()) {
			directory.mkdirs();
		}
	}

	public boolean isZColumn(String colName) {
		return colName.startsWith(ColName.COL_PREFIX);
	}

	public abstract RowWrapper<R> getRowWrapper(Version v) ;
}
