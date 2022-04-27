package zingg.documenter;

import org.apache.spark.sql.SparkSession;

import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;
import zingg.client.Arguments;
import zingg.util.RowWrapper;

class DocumenterBase {
	protected static Configuration config;
	protected SparkSession spark;
	protected Arguments args;

	public DocumenterBase(SparkSession spark, Arguments args) {
		this.spark = spark;
		this.args = args;
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
		cfg.setObjectWrapper(new RowWrapper(cfg.getIncompatibleImprovements()));

		/* ------------------------------------------------------------------------ */
		/* You usually do these for MULTIPLE TIMES in the application life-cycle: */
		return cfg;
	}
}
