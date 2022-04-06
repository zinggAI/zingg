package zingg.client;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.SparkSession;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

public class BaseSparkTest {

    public static Arguments args;
    public static JavaSparkContext ctx;
    public static SparkSession spark;

    public static final Log LOG = LogFactory.getLog(BaseSparkTest.class);

    @BeforeAll
    public static void setup() {
    	try {
    		spark = SparkSession
    				.builder()
    				.master("local[*]")
    				.appName("Zingg" + "Junit")
    				.getOrCreate();
    		ctx = new JavaSparkContext(spark.sparkContext());
    		JavaSparkContext.jarOfClass(BaseSparkTest.class);
    		args = new Arguments();
    	} catch (Throwable e) {
    		if (LOG.isDebugEnabled())
    			e.printStackTrace();
    		LOG.info("Problem in spark env setup");
    	}
    }

    @AfterAll
    public static void teardown() {
    	if (ctx != null)
    		ctx.stop();
    	if (spark != null)
    		spark.stop();
    }
}
