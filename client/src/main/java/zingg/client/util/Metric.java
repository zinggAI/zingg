package zingg.client.util;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

public class Metric {
    public static final String METRIC_DATA_FORMAT = "dataFormat";
    public static final String METRIC_OUTPUT_FORMAT = "outputFormat";
    public static final String METRIC_FIELDS_COUNT = "numFields";
    public static final String METRIC_EXEC_TIME = "executionTime";
    public static final String METRIC_TRAINING_MATCHES = "trainingDataMatches";
    public static final String METRIC_TRAINING_NONMATCHES = "trainingDataNonmatches";
    public static final String METRIC_DATA_COUNT = "dataCount";

    public static final long timeout = 1200L;
    public static final double confidence = 0.95; // default value

    public static double approxCount(Dataset<Row> data) {
        return data.rdd().countApprox(timeout, confidence).initialValue().mean();
    }
}