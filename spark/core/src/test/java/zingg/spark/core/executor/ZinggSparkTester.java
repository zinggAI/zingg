package zingg.spark.core.executor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructType;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import zingg.common.client.Arguments;
import zingg.common.client.IZingg;
import zingg.spark.client.ZSparkSession;
import zingg.spark.core.util.SparkBlockingTreeUtil;
import zingg.spark.core.util.SparkDSUtil;
import zingg.spark.core.util.SparkGraphUtil;
import zingg.spark.core.util.SparkHashUtil;
import zingg.spark.core.util.SparkModelUtil;
import zingg.spark.core.util.SparkPipeUtil;

public class ZinggSparkTester {

    public static Arguments args;
    public static JavaSparkContext ctx;
    public static SparkSession spark;
    public static ZinggSparkContext zsCTX;

    public static final Log LOG = LogFactory.getLog(ZinggSparkTester.class);

	protected static final String FIELD_INTEGER = "fieldInteger";
	protected static final String FIELD_DOUBLE = "fieldDouble";

    @BeforeAll
    public static void setup() {
    	try {
    		spark = SparkSession
    				.builder()
    				.master("local[*]")
    				.appName("Zingg" + "Junit")
    				.getOrCreate();
    		ctx = new JavaSparkContext(spark.sparkContext());
    		JavaSparkContext.jarOfClass(IZingg.class);    
			args = new Arguments();
			zsCTX = new ZinggSparkContext();
			zsCTX.ctx = ctx;
			ZSparkSession zSession = new ZSparkSession(spark, null);
			zsCTX.zSession = zSession;
			
            ctx.setCheckpointDir("/tmp/checkpoint");	
            zsCTX.setPipeUtil(new SparkPipeUtil(zSession));
            zsCTX.setDSUtil(new SparkDSUtil(zSession));
            zsCTX.setHashUtil(new SparkHashUtil(zSession));
            zsCTX.setGraphUtil(new SparkGraphUtil());
            zsCTX.setModelUtil(new SparkModelUtil(zSession));
            zsCTX.setBlockingTreeUtil(new SparkBlockingTreeUtil(zSession, zsCTX.getPipeUtil()));
			
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

	public Dataset<Row> createDFWithDoubles(int numRows, int numCols) {
	
		StructType structType = new StructType();

		List<Double> rowValues = new ArrayList<Double>();

		for (int n = 0; n < numCols; ++n) {
			structType = structType.add("col" + n, DataTypes.DoubleType, false);
			rowValues.add(0d);
		};
		
		List<Row> nums = new ArrayList<Row>();

		IntStream.range(0, numRows).forEachOrdered(n -> {
			nums.add(RowFactory.create(rowValues));
		});


		return spark.createDataFrame(nums, structType);





	}
}
