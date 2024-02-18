package zingg.spark.core.executor;

import java.io.File;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataType;
import org.junit.jupiter.api.AfterEach;

import zingg.common.client.ZinggClientException;
import zingg.common.core.executor.Labeller;
import zingg.common.core.executor.TestExecutorsGeneric;
import zingg.spark.core.context.ZinggSparkContext;

public class TestSparkExecutors extends TestExecutorsGeneric<SparkSession,Dataset<Row>,Row,Column,DataType> {
	protected static final String CONFIG_FILE = "zingg/spark/core/executor/configSparkIntTest.json";
	
	protected static final String TEST_DATA_FILE = "zingg/spark/core/executor/test.csv";

	public static final Log LOG = LogFactory.getLog(TestSparkExecutors.class);
	
	protected ZinggSparkContext ctx;
	

	public TestSparkExecutors() throws IOException, ZinggClientException {	
		SparkSession spark = SparkSession
				.builder()
				.master("local[*]")
				.appName("Zingg" + "Junit")
				.getOrCreate();
		this.ctx = new ZinggSparkContext();
		this.ctx.setSession(spark);
		this.ctx.setUtils();
		init(spark);
	}

	@Override
	public String getConfigFile() {
		return CONFIG_FILE;
	}
	
	@Override
	protected SparkTrainingDataFinder getTrainingDataFinder() throws ZinggClientException {
		SparkTrainingDataFinder stdf = new SparkTrainingDataFinder(ctx);
		stdf.init(args);
		return stdf;
	}
	@Override
	protected Labeller<SparkSession,Dataset<Row>,Row,Column,DataType> getLabeller() throws ZinggClientException {
		JunitSparkLabeller jlbl = new JunitSparkLabeller(ctx);
		jlbl.init(args);
		return jlbl;
	}
	@Override
	protected SparkTrainer getTrainer() throws ZinggClientException {
		SparkTrainer st = new SparkTrainer(ctx);
		st.init(args);
		return st;
	}
	@Override
	protected SparkMatcher getMatcher() throws ZinggClientException {
		SparkMatcher sm = new SparkMatcher(ctx);
		sm.init(args);
		return sm;
	}
	
	
	@Override
	public String setupArgs() throws ZinggClientException, IOException {
		String configFile = super.setupArgs();
		String testFile = getClass().getClassLoader().getResource(TEST_DATA_FILE).getFile();
		// correct the location of test data
		args.getData()[0].setProp("location", testFile);
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
