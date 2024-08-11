package zingg.common.core.block;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataType;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import zingg.common.client.IArguments;
import zingg.common.client.util.IWithSession;
import zingg.common.client.util.WithSession;
import zingg.spark.client.util.SparkDFObjectUtil;
import zingg.spark.core.context.ZinggSparkContext;
import zingg.spark.core.executor.ZinggSparkTester;
import zingg.spark.core.util.SparkBlockingTreeUtil;
import zingg.spark.core.util.SparkHashUtil;

public class TestSparkBlock extends TestBlockBase<SparkSession, Dataset<Row>, Row, Column, DataType> {

    public static final Log LOG = LogFactory.getLog(TestSparkBlock.class);
    public static IArguments args;
    public static JavaSparkContext ctx;
    public static ZinggSparkContext zsCTX;
    public static SparkSession spark;
    public static IWithSession<SparkSession> iWithSession;

    public TestSparkBlock() {
        super(new SparkDFObjectUtil(iWithSession), new SparkHashUtil(spark), new SparkBlockingTreeUtil(spark, zsCTX.getPipeUtil()));
    }

    @BeforeAll
    public static void setup() {
        setUpSpark();
    }

    protected static void setUpSpark() {
        try {

            if(spark == null && ZinggSparkTester.spark == null) {
                ZinggSparkTester.setup();
            }
            spark = ZinggSparkTester.spark;
            ctx = ZinggSparkTester.ctx;
            zsCTX = ZinggSparkTester.zsCTX;
            iWithSession = new WithSession<>();
            iWithSession.setSession(spark);
        } catch (Throwable e) {
            if (LOG.isDebugEnabled())
                e.printStackTrace();
            LOG.info("Problem in spark env setup");
        }
    }

    @AfterAll
    public static void teardown() {
        if (ctx != null) {
            ctx.stop();
            ctx = null;
        }
        if (spark != null) {
            spark.stop();
            spark = null;
        }
    }
}
