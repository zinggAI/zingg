package zingg.spark.core.executor;

import java.io.File;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataType;
import org.junit.jupiter.api.AfterEach;

import zingg.common.client.ArgumentsUtil;
import zingg.common.client.IZingg;
import zingg.common.client.ZinggClientException;
import zingg.common.core.executor.TestExecutorsCompound;
import zingg.common.core.executor.TrainMatcher;
import zingg.spark.core.context.ZinggSparkContext;
import zingg.spark.core.executor.labeller.ProgrammaticSparkLabeller;
import zingg.spark.core.executor.validate.SparkTrainMatchValidator;

public class TestSparkExecutorsCompound extends TestExecutorsCompound<SparkSession,Dataset<Row>,Row,Column,DataType> {
	protected static final String CONFIG_FILE = "zingg/spark/core/executor/configSparkIntTest.json";
	protected static final String TEST_DATA_FILE = "zingg/spark/core/executor/test.csv";

    public static final Log LOG = LogFactory.getLog(TestSparkExecutorsCompound.class);
	
	protected ZinggSparkContext ctx;

	public TestSparkExecutorsCompound() throws IOException, ZinggClientException {	
		SparkSession spark = SparkSession
				.builder()
				.master("local[*]")
				.appName("Zingg" + "Junit")
				.getOrCreate();
		
		JavaSparkContext ctx1 = new JavaSparkContext(spark.sparkContext());
    	JavaSparkContext.jarOfClass(IZingg.class);    
		ctx1.setCheckpointDir("/tmp/checkpoint");

		this.ctx = new ZinggSparkContext();
		this.ctx.setSession(spark);
		this.ctx.setUtils();
		init(spark);
		//setupArgs();
	}

	@Override
	public String getConfigFile() {
		return CONFIG_FILE;
	}


	@Override
	protected SparkFindAndLabeller getFindAndLabeller() throws ZinggClientException {
		SparkFindAndLabeller sfal = new SparkFindAndLabeller(ctx);
        sfal.setLabeller(new ProgrammaticSparkLabeller(ctx));
		return sfal;
	}

	@Override
	protected SparkTrainMatcher getTrainMatcher() throws ZinggClientException {
		SparkTrainMatcher stm = new SparkTrainMatcher(ctx);
		return stm;
	}

	@Override
	protected SparkTrainMatchValidator getTrainMatchValidator(TrainMatcher<SparkSession,Dataset<Row>,Row,Column,DataType> trainMatch) {
		return new SparkTrainMatchValidator(trainMatch);
	}

	/* 
	@Override
	@AfterEach
	public void tearDown() {
		// just rename, would be removed automatically as it's in /tmp
		File dir = new File(args.getZinggDir());
	    File newDir = new File(dir.getParent() + "/zingg_junit_" + System.currentTimeMillis());
	    dir.renameTo(newDir);
	}
*/
	
}
