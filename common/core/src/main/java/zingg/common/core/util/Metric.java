package zingg.common.core.util;


public class Metric {
    public static final String DATA_FORMAT = "dataFormat";
    public static final String OUTPUT_FORMAT = "outputFormat";
    public static final String TOTAL_FIELDS_COUNT = "numTotalFields";
    public static final String MATCH_FIELDS_COUNT = "numMatchFields";
    public static final String EXEC_TIME = "executionTime";
    public static final String TRAINING_MATCHES = "trainingDataMatches";
    public static final String TRAINING_NONMATCHES = "trainingDataNonmatches";
    public static final String DATA_COUNT = "dataCount";
    public static final String STOPWORDS = "stopWords";

    public static final long timeout = 1200L;
    public static final double confidence = 0.95; // default value
    public static final String MODEL_ID = "modelId";
    public static final String IS_PYTHON = "is_python";
     
    public static final String IS_CONFIG_JSON = "is_config";
    public static final String ZINGG_VERSION = "zingg_version";
    public static final String DATABRICKS_RUNTIME_VERSION = "DATABRICKS_RUNTIME_VERSION";
    public static final String DB_INSTANCE_TYPE = "DB_INSTANCE_TYPE";
    public static final String JAVA_HOME = "java_home";
    public static final String JAVA_VERSION = "java_version";
    public static final String OS_ARCH = "os_arch";
    public static final String OS_NAME = "os_name";
    public static final String DOMAIN = "domain";
    //public static final String USER_NAME = "user.name";
    //public static final String USER_HOME = "user.home";
    //public static final String ZINGG_HOME = "ZINGG_HOME";




    /* 
    public static double approxCount(Dataset<Row> data) {
        return data.rdd().countApprox(timeout, confidence).initialValue().mean();
    }
    */
}
