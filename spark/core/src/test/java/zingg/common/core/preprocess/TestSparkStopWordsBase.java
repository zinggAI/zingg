package zingg.common.core.preprocess;

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
import zingg.common.client.Arguments;
import zingg.common.client.ZinggClientException;
import zingg.common.client.util.WithSession;
import zingg.common.core.util.SampleStopWordRemover;
import zingg.spark.client.util.SparkDFObjectUtil;
import zingg.spark.client.util.WithSparkSession;
import zingg.spark.core.context.ZinggSparkContext;

public class TestSparkStopWordsBase extends TestStopWordsBase<SparkSession, Dataset<Row>, Row, Column, DataType> {

    public static final Log LOG = LogFactory.getLog(TestSparkStopWordsBase.class);
    public static JavaSparkContext ctx;
    public static SparkSession spark;
    public static ZinggSparkContext zsCTX;
    public static WithSession<SparkSession> withSession;

    @BeforeAll
    public static void setup() {
        setUpSpark();
    }

    public TestSparkStopWordsBase() throws ZinggClientException {
        super(new SparkDFObjectUtil(withSession), SampleStopWordRemover.getStopWordRemovers(zsCTX, new Arguments()), zsCTX);
    }

    protected static void setUpSpark() {
        try {
            spark = SparkSession
                    .builder()
                    .master("local[*]")
                    .appName("Zingg" + "Junit")
                    .getOrCreate();
            ctx = new JavaSparkContext(spark.sparkContext());
            withSession = new WithSparkSession();
            withSession.setSession(spark);
            zsCTX = new ZinggSparkContext();
            zsCTX.init(spark);
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
