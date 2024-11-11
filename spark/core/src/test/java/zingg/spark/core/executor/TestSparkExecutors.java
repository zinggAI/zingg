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

import org.junit.jupiter.api.extension.ExtendWith;
import zingg.common.client.ZinggClientException;
import zingg.common.core.executor.Labeller;
import zingg.common.core.executor.TestExecutorsGeneric;
import zingg.common.core.executor.Trainer;
import zingg.spark.core.TestSparkBase;
import zingg.spark.core.context.ZinggSparkContext;

@ExtendWith(TestSparkBase.class)
public class TestSparkExecutors extends TestExecutorsGeneric<SparkSession,Dataset<Row>,Row,Column,DataType> {
	protected static final String CONFIG_FILE = "zingg/spark/core/executor/configSparkIntTest.json";
	protected static final String TEST_DATA_FILE = "zingg/spark/core/executor/test.csv";

	protected static final String CONFIGLINK_FILE = "zingg/spark/core/executor/configSparkLinkTest.json";
	protected static final String TEST1_DATA_FILE = "zingg/spark/core/executor/test1.csv";
	protected static final String TEST2_DATA_FILE = "zingg/spark/core/executor/test2.csv";
	private final SparkSession sparkSession;
	public static final Log LOG = LogFactory.getLog(TestSparkExecutors.class);
	
	protected ZinggSparkContext ctx;
	
	public TestSparkExecutors(SparkSession sparkSession) throws IOException, ZinggClientException {
		this.sparkSession = sparkSession;
		ctx = new ZinggSparkContext();
		ctx.init(sparkSession);
		init(this.sparkSession);
	}

	@Override
	public String getConfigFile() {
		return CONFIG_FILE;
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
	/* 

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
	protected SparkTrainMatchTester getTrainMatchValidator(TrainMatcher<SparkSession,Dataset<Row>,Row,Column,DataType> trainMatch) {
		return new SparkTrainMatchTester(trainMatch,args);
	}
		*/

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
