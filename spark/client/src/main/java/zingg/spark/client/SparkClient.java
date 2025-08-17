package zingg.spark.client;

import org.apache.spark.SparkContext;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.*;
import zingg.common.client.BannerPrinter;
import zingg.common.client.Client;
import zingg.common.client.SessionManager;
import zingg.common.client.util.PipeUtilBase;
import zingg.spark.client.util.SparkPipeUtil;

public class SparkClient extends Client<SparkSession, Dataset<Row>, Row, Column> {

    private JavaSparkContext javaSparkContext;

    public SparkClient(BannerPrinter bannerPrinter) {
        super(new SessionManager<>(() -> {
            SparkSession spark = SparkSession.builder()
                    .appName("Zingg")
                    .master("local[*]")
                    .config("spark.sql.shuffle.partitions", "8")
                    .getOrCreate();

            spark.sparkContext().setCheckpointDir("/tmp/checkpoint");
            return spark;
        }), bannerPrinter);
    }


    public SparkSession getSession() {
        return sessionManager.get();
    }

    @Override
    public PipeUtilBase<SparkSession, Dataset<Row>, Row, Column> getPipeUtil() {
        return new SparkPipeUtil(getSession());
    }

    public JavaSparkContext getJavaSparkContext() {
        if (javaSparkContext == null) {
            SparkContext sc = getSession().sparkContext();
            javaSparkContext = JavaSparkContext.fromSparkContext(sc);
        }
        return javaSparkContext;
    }

}
