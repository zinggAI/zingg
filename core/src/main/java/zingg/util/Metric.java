package zingg.util;

import com.snowflake.snowpark_java.DataFrame;
import com.snowflake.snowpark_java.Row;

public class Metric {
    public static final String DATA_FORMAT = "dataFormat";
    public static final String OUTPUT_FORMAT = "outputFormat";
    public static final String TOTAL_FIELDS_COUNT = "numTotalFields";
    public static final String MATCH_FIELDS_COUNT = "numMatchFields";
    public static final String EXEC_TIME = "executionTime";
    public static final String TRAINING_MATCHES = "trainingDataMatches";
    public static final String TRAINING_NONMATCHES = "trainingDataNonmatches";
    public static final String DATA_COUNT = "dataCount";

    public static final long timeout = 1200L;
    public static final double confidence = 0.95; // default value

    public static double approxCount(DataFrame data) {
        return data.async().count().getResult((int)timeout/1000);
    }
}
