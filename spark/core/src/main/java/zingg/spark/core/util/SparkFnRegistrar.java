package zingg.spark.core.util;

import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.api.java.UDF1;
import org.apache.spark.sql.api.java.UDF2;
import org.apache.spark.sql.types.DataType;

public class SparkFnRegistrar {

    public static void registerSparkFunctionUDF1(SparkSession sparkSession, String functionName, UDF1 udf1, DataType dataType) {

        //only register udf1 if it is not registered already
        if (!sparkSession.catalog().functionExists(functionName)) {
            sparkSession.udf().register(functionName, udf1, dataType);
        }
    }

    public static void registerSparkFunctionUDF2(SparkSession sparkSession, String functionName, UDF2 udf2, DataType dataType) {

        //only register udf2 if it is not registered already
        if (!sparkSession.catalog().functionExists(functionName)) {
            sparkSession.udf().register(functionName, udf2, dataType);
        }
    }
}
