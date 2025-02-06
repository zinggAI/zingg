package zingg.spark.core.session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.SparkContext;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.SparkSession;
import zingg.common.client.Arguments;
import zingg.common.client.IArguments;
import zingg.common.client.IZingg;
import zingg.spark.core.context.ZinggSparkContext;

public class SparkSessionProvider {

    private static SparkSessionProvider sparkSessionProvider;

    private SparkSession sparkSession;
    private JavaSparkContext javaSparkContext;
    private ZinggSparkContext zinggSparkContext;
    private IArguments args;
    public static final Log LOG = LogFactory.getLog(SparkSessionProvider.class);

    private void initializeSession() {
        if (sparkSession == null) {
            try {
                String sparkDriverMemory = System.getenv("SPARK_DRIVER_MEMORY");
                if (sparkDriverMemory == null) {
                    sparkDriverMemory = "1g";
                }
                sparkSession = SparkSession
                        .builder()
                        .master("local[*]")
                        .appName("ZinggJunit")
                        .config("spark.debug.maxToStringFields", 100)
                        .config("spark.driver.memory", sparkDriverMemory)
                        .getOrCreate();
                SparkContext sparkContext = sparkSession.sparkContext();
                long driverMemory = sparkContext.getConf().getSizeAsGb("spark.driver.memory", "0");
                System.out.println("Spark driver memory: " + driverMemory + " GB");
                if (sparkContext.getCheckpointDir().isEmpty()) {
                    sparkContext.setCheckpointDir("/tmp/checkpoint");
                }
                javaSparkContext = new JavaSparkContext(sparkContext);
                JavaSparkContext.jarOfClass(IZingg.class);
                if (!javaSparkContext.getCheckpointDir().isPresent()) {
                    javaSparkContext.setCheckpointDir(String.valueOf(sparkContext.getCheckpointDir()));
                }
                args = new Arguments();
                zinggSparkContext = new ZinggSparkContext();
                zinggSparkContext.init(sparkSession);
            } catch (Throwable e) {
                if (LOG.isDebugEnabled())
                    e.printStackTrace();
                LOG.info("Problem in spark env setup");
            }
        } else {
            LOG.info("Spark session already active, ignoring create spark session!");
        }
    }

    public static SparkSessionProvider getInstance() {
        if (sparkSessionProvider == null) {
            sparkSessionProvider = new SparkSessionProvider();
            sparkSessionProvider.initializeSession();
        }
        return sparkSessionProvider;
    }



    //set getters
    public SparkSession getSparkSession() {
        return this.sparkSession;
    }

    public JavaSparkContext getJavaSparkContext() {
        return this.javaSparkContext;
    }

    public ZinggSparkContext getZinggSparkContext() {
        return this.zinggSparkContext;
    }

    public IArguments getArgs() {
        return this.args;
    }
}
