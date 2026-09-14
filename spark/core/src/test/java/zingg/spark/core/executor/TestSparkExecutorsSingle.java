package zingg.spark.core.executor;

import java.io.IOException;
import java.util.Objects;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataType;

import org.junit.jupiter.api.extension.ExtendWith;
import zingg.common.client.ZinggClientException;
import zingg.common.client.arguments.model.IArguments;
import zingg.common.core.executor.testData.TestArgumentsBuilder;
import zingg.common.client.util.DFObjectUtil;
import zingg.common.client.util.IWithSession;
import zingg.common.client.util.WithSession;
import zingg.common.core.executor.Labeller;
import zingg.common.core.executor.TestExecutorsSingle;
import zingg.common.core.executor.Trainer;
import zingg.common.core.util.ICleanUpUtil;
import zingg.spark.client.util.SparkDFObjectUtil;
import zingg.spark.core.TestSparkBaseHeavy;
import zingg.spark.core.context.ZinggSparkContext;
import zingg.spark.core.executor.labeller.ProgrammaticSparkLabeller;
import zingg.spark.core.executor.validate.SparkTrainerValidator;
import zingg.spark.core.util.SparkCleanUpUtil;

@ExtendWith(TestSparkBaseHeavy.class)
public class TestSparkExecutorsSingle extends TestExecutorsSingle<SparkSession,Dataset<Row>,Row,Column,DataType> {
	protected static final String TEST_DATA_FILE = "zingg/spark/core/executor/test.csv";
	protected static final String TEST1_DATA_FILE = "zingg/spark/core/executor/test1.csv";
	protected static final String TEST2_DATA_FILE = "zingg/spark/core/executor/test2.csv";
	/** Left classpath relative on purpose: only the data pipes were ever resolved to a
	 *  real path, so the training samples are read relative to the working directory. */
	protected static final String TRAINING_DATA_FILE = "./zingg/spark/core/executor/training.csv";
	protected static final String STOP_WORDS = "./zingg/spark/core/executor/stopwords/add1.csv";
	protected static final String ZINGG_DIR = "/tmp/junit_integration_spark/single";
	protected static final String OUTPUT_DIR = "/tmp/junit_integration_spark/single/zinggOutput";
	public static final Log LOG = LogFactory.getLog(TestSparkExecutorsSingle.class);
	
	private final SparkSession sparkSession;
	protected ZinggSparkContext ctx;
	
	public TestSparkExecutorsSingle(SparkSession sparkSession) throws IOException, ZinggClientException {
		this.sparkSession = sparkSession;
		ctx = new ZinggSparkContext();
		ctx.init(sparkSession);
		init(this.sparkSession);
	}

	@Override
	public IArguments getArgs() throws ZinggClientException {
		return TestArgumentsBuilder.buildSingleArgs(getModelId(), ZINGG_DIR, resource(TEST_DATA_FILE),
				TRAINING_DATA_FILE, OUTPUT_DIR, STOP_WORDS);
	}

	@Override
	public IArguments getLinkerArgs() throws ZinggClientException {
		return TestArgumentsBuilder.buildLinkArgs(getModelId(), ZINGG_DIR, resource(TEST1_DATA_FILE),
				resource(TEST2_DATA_FILE), OUTPUT_DIR);
	}

	/** test data lives on the classpath; the executors need a real path to read it from */
	protected String resource(String classpathLocation) {
		return Objects.requireNonNull(getClass().getClassLoader().getResource(classpathLocation)).getFile();
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
	protected SparkTrainerValidator getTrainerValidator(Trainer<SparkSession,Dataset<Row>,Row,Column,DataType> trainer) {
		return new SparkTrainerValidator(trainer);
	}

	@Override
	protected DFObjectUtil<SparkSession, Dataset<Row>, Row, Column> getDFObjectUtil() {
		IWithSession<SparkSession> iWithSession = new WithSession<SparkSession>();
		iWithSession.setSession(session);
		return new SparkDFObjectUtil(iWithSession);
	}

	@Override
	public ICleanUpUtil<SparkSession> getCleanupUtil() {
		return SparkCleanUpUtil.getInstance();
	}

	@Override
	public SparkSession getSession() {
		return ctx.getSession();
	}

}
