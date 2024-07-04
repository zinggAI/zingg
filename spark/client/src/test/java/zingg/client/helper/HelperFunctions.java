package zingg.client.helper;

import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import zingg.common.client.ZFrame;
import zingg.spark.client.SparkFrame;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class HelperFunctions {
    public static void assertTrueCheckingExceptOutput(ZFrame<Dataset<Row>, Row, Column> sf1, ZFrame<Dataset<Row>, Row, Column> sf2, String message) {
        assertTrue(sf1.except(sf2).isEmpty(), message);
    }


    public static void assertTrueCheckingExceptOutput(ZFrame<Dataset<Row>, Row, Column> sf1, Dataset<Row> df2, String message) {
        SparkFrame sf2 = new SparkFrame(df2);
        assertTrue(sf1.except(sf2).isEmpty(), message);
    }
}
