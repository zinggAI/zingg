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
import zingg.common.client.ZinggClientException;
import zingg.common.client.util.IWithSession;
import zingg.common.client.util.WithSession;
import zingg.common.core.util.SparkStopWordRemoverUtility;
import zingg.spark.client.util.SparkDFObjectUtil;
import zingg.spark.core.context.ZinggSparkContext;

public class TestSparkStopWords extends TestStopWordsBase<SparkSession, Dataset<Row>, Row, Column, DataType> {

    public static final Log LOG = LogFactory.getLog(TestSparkStopWords.class);
    public static JavaSparkContext ctx;
    public static SparkSession spark;
    public static ZinggSparkContext zsCTX;
    public static IWithSession<SparkSession> iWithSession;

    @BeforeAll
    public static void setup() {
        setUpSpark();
    }

    public TestSparkStopWords() throws ZinggClientException {
        super(new SparkDFObjectUtil(iWithSession), new SparkStopWordRemoverUtility(), zsCTX);
    }

    protected static void setUpSpark() {
        try {
            spark = SparkSession
                    .builder()
                    .master("local[*]")
                    .appName("Zingg" + "Junit")
                    .getOrCreate();
            ctx = new JavaSparkContext(spark.sparkContext());
            iWithSession = new WithSession<>();
            iWithSession.setSession(spark);
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
