package zingg.spark.client;

import org.apache.spark.SparkContext;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.*;
import zingg.common.client.BannerPrinter;
import zingg.common.client.Client;
import zingg.common.client.IZingg;
import zingg.common.client.SessionManager;
import zingg.common.client.util.PipeUtilBase;
import zingg.spark.client.util.SparkPipeUtil;

public class SparkClient extends Client<SparkSession, Dataset<Row>, Row, Column> {

    private JavaSparkContext javaSparkContext;

    public SparkClient(BannerPrinter bannerPrinter) {
        super(new SessionManager<>(() -> {
            SparkSession sparkSession = SparkSession
                    .builder()
                    .appName("Zingg")
                    .getOrCreate();
            SparkContext sparkContext = sparkSession.sparkContext();
            if (sparkContext.getCheckpointDir().isEmpty()) {
                sparkContext.setCheckpointDir("/tmp/checkpoint");
            }
            JavaSparkContext ctx = JavaSparkContext.fromSparkContext(sparkContext);
            JavaSparkContext.jarOfClass(IZingg.class);
            if (!ctx.getCheckpointDir().isPresent()) {
                ctx.setCheckpointDir(String.valueOf(sparkContext.getCheckpointDir()));
            }
            return sparkSession;
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
