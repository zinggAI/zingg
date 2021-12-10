package zingg.client.util;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

public class Metric {
    public static final String METRIC_SOURCE_TYPE = "source_type";
    public static final String METRIC_SINK_TYPE = "sink_type";
    public static final String METRIC_FEATURE_COUNT = "no_features";
    public static final String METRIC_EXEC_TIME = "exec_time";
    public static final String METRIC_POSITIVE_COUNT = "positive_count";
    public static final String METRIC_NEGATIVE_COUNT = "negative_count";
    public static final String METRIC_TOTAL_COUNT = "total_count";

    public static final long timeout = 1200L;
    public static final double confidence = 0.95; // default value

    public static double approxCount(Dataset<Row> data) {
        return data.rdd().countApprox(timeout, confidence).initialValue().mean();
    }
}