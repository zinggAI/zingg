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
import zingg.common.core.executor.Labeller;
import zingg.common.core.executor.TestSingleExecutors;
import zingg.common.core.executor.Trainer;
import zingg.spark.core.context.ZinggSparkContext;

public class TestSparkExecutors extends TestSingleExecutors<SparkSession,Dataset<Row>,Row,Column,DataType> {
	protected static final String CONFIG_FILE = "zingg/spark/core/executor/configSparkIntTest.json";
	protected static final String TEST_DATA_FILE = "zingg/spark/core/executor/test.csv";

	protected static final String CONFIGLINK_FILE = "zingg/spark/core/executor/configSparkLinkTest.json";
	protected static final String TEST1_DATA_FILE = "zingg/spark/core/executor/test1.csv";
	protected static final String TEST2_DATA_FILE = "zingg/spark/core/executor/test2.csv";

	public static final Log LOG = LogFactory.getLog(TestSparkExecutors.class);
	
	protected ZinggSparkContext ctx;
	
	public TestSparkExecutors() throws IOException, ZinggClientException {	
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
		setupArgs();
		setupLinkerArgs();
	}

	@Override
	public String getConfigFile() {
		return CONFIG_FILE;
	}

	@Override
	public String getLinkerConfigFile(){
		return CONFIGLINK_FILE;
	}
	
	@Override
	protected SparkTrainingDataFinder getTrainingDataFinder() throws ZinggClientException {
		SparkTrainingDataFinder stdf = new SparkTrainingDataFinder(ctx);
		return stdf;
	}

	@Override
	protected Labeller<SparkSession,Dataset<Row>,Row,Column,DataType> getLabeller() throws ZinggClientException {
		ProgrammaticSparkLabeller jlbl = new ProgrammaticSparkLabeller(ctx);
		return jlbl;
	}

	@Override
	protected SparkTrainer getTrainer() throws ZinggClientException {
		SparkTrainer st = new SparkTrainer(ctx);
		return st;
	}

	@Override
	protected SparkVerifyBlocker getVerifyBlocker() throws ZinggClientException {
		SparkVerifyBlocker svb = new SparkVerifyBlocker(ctx);
		return svb;
	}

	@Override
	protected SparkMatcher getMatcher() throws ZinggClientException {
		SparkMatcher sm = new SparkMatcher(ctx);
		return sm;
	}

	
	@Override
	protected SparkLinker getLinker() throws ZinggClientException {
		SparkLinker sl = new SparkLinker(ctx);
		return sl;
	} 

	@Override
	protected SparkTrainerTester getTrainerValidator(Trainer<SparkSession,Dataset<Row>,Row,Column,DataType> trainer) {
		return new SparkTrainerTester(trainer,args);
	}

	public String setupArgs() throws ZinggClientException, IOException {
		String configFile = getClass().getClassLoader().getResource(getConfigFile()).getFile();
		args = new ArgumentsUtil().createArgumentsFromJSON(configFile, "findTrainingData");
		String testFile = getClass().getClassLoader().getResource(TEST_DATA_FILE).getFile();
		// correct the location of test data
		args.getData()[0].setProp("location", testFile);
		return configFile;
	}

	public String setupLinkerArgs() throws ZinggClientException, IOException {
		String configFile = getClass().getClassLoader().getResource(getLinkerConfigFile()).getFile();
		linkerArgs = new ArgumentsUtil().createArgumentsFromJSON(configFile, "link");
		String testOneFile = getClass().getClassLoader().getResource(TEST1_DATA_FILE).getFile();
		// correct the location of test data
		args.getData()[0].setProp("location", testOneFile);
		String testTwoFile = getClass().getClassLoader().getResource(TEST2_DATA_FILE).getFile();
		// correct the location of test data
		args.getData()[0].setProp("location", testTwoFile);
		return configFile;
	} 
	
	@Override
	@AfterEach
	public void tearDown() {
		// just rename, would be removed automatically as it's in /tmp
		File dir = new File(args.getZinggDir());
	    File newDir = new File(dir.getParent() + "/zingg_junit_" + System.currentTimeMillis());
	    dir.renameTo(newDir);
	}

}
