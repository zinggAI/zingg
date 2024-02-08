package zingg.spark.core.executor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import zingg.common.client.ZinggClientException;
import zingg.common.core.executor.ExecutorTester;
import zingg.common.core.executor.Labeller;
import zingg.common.core.executor.LabellerTester;
import zingg.common.core.executor.MatcherTester;
import zingg.common.core.executor.TestExecutorsGeneric;
import zingg.common.core.executor.TrainerTester;
import zingg.common.core.executor.TrainingDataFinderTester;
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
	
	@Test
	public void testExecutors() throws ZinggClientException {	
		List<ExecutorTester<SparkSession,Dataset<Row>,Row,Column,DataType>> executorTesterList = new ArrayList<ExecutorTester<SparkSession,Dataset<Row>,Row,Column,DataType>>();

		TrainingDataFinderTester<SparkSession,Dataset<Row>,Row,Column,DataType> tdft = new TrainingDataFinderTester<SparkSession,Dataset<Row>,Row,Column,DataType>(getTrainingDataFinder());
		executorTesterList.add(tdft);
		
		LabellerTester<SparkSession,Dataset<Row>,Row,Column,DataType> lt = new LabellerTester<SparkSession,Dataset<Row>,Row,Column,DataType>(getLabeller());
		executorTesterList.add(lt);

		// training and labelling needed twice to get sufficient data
		TrainingDataFinderTester<SparkSession,Dataset<Row>,Row,Column,DataType> tdft2 = new TrainingDataFinderTester<SparkSession,Dataset<Row>,Row,Column,DataType>(getTrainingDataFinder());
		executorTesterList.add(tdft2);
		
		LabellerTester<SparkSession,Dataset<Row>,Row,Column,DataType> lt2 = new LabellerTester<SparkSession,Dataset<Row>,Row,Column,DataType>(getLabeller());
		executorTesterList.add(lt2);
	
		TrainerTester<SparkSession,Dataset<Row>,Row,Column,DataType> tt = new TrainerTester<SparkSession,Dataset<Row>,Row,Column,DataType>(getTrainer());
		executorTesterList.add(tt);

		MatcherTester<SparkSession,Dataset<Row>,Row,Column,DataType> mt = new SparkMatcherTester(getMatcher());
		executorTesterList.add(mt);
		
		super.testExecutors(executorTesterList);
	}
	
	protected SparkTrainingDataFinder getTrainingDataFinder() throws ZinggClientException {
		SparkTrainingDataFinder stdf = new SparkTrainingDataFinder(ctx);
		stdf.init(args);
		return stdf;
	}
	
	protected Labeller<SparkSession,Dataset<Row>,Row,Column,DataType> getLabeller() throws ZinggClientException {
		JunitSparkLabeller jlbl = new JunitSparkLabeller(ctx);
		jlbl.init(args);
		return jlbl;
	}
	
	protected SparkTrainer getTrainer() throws ZinggClientException {
		SparkTrainer st = new SparkTrainer(ctx);
		st.init(args);
		return st;
	}

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
