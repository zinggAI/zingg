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
import zingg.common.core.executor.TestExecutorsCompound;
import zingg.common.core.executor.TrainMatcher;
import zingg.common.core.util.ICleanUpUtil;
import zingg.spark.client.util.SparkDFObjectUtil;
import zingg.spark.core.TestSparkBaseHeavy;
import zingg.spark.core.context.ZinggSparkContext;
import zingg.spark.core.executor.labeller.ProgrammaticSparkLabeller;
import zingg.spark.core.executor.validate.SparkTrainMatchValidator;
import zingg.spark.core.util.SparkCleanUpUtil;

@ExtendWith(TestSparkBaseHeavy.class)
public class TestSparkExecutorsCompound extends TestExecutorsCompound<SparkSession,Dataset<Row>,Row,Column,DataType> {
	protected static final String TEST_DATA_FILE = "zingg/spark/core/executor/test.csv";
	/** Left classpath relative on purpose: only the data pipes were ever resolved to a
	 *  real path, so the training samples are read relative to the working directory. */
	protected static final String TRAINING_DATA_FILE = "./zingg/spark/core/executor/training.csv";
	protected static final String STOP_WORDS = "./zingg/spark/core/executor/stopwords/add1.csv";
	protected static final String ZINGG_DIR = "/tmp/junit_integration_spark/compound";
	protected static final String OUTPUT_DIR = "/tmp/junit_integration_spark/compound/zinggOutput";

    public static final Log LOG = LogFactory.getLog(TestSparkExecutorsCompound.class);
	
	protected ZinggSparkContext ctx;

	public TestSparkExecutorsCompound(SparkSession sparkSession) throws IOException, ZinggClientException {
		this.ctx = new ZinggSparkContext();
		this.ctx.setSession(sparkSession);
		this.ctx.setUtils();
		init(sparkSession);
	}

	@Override
	public IArguments getArgs() throws ZinggClientException {
		return TestArgumentsBuilder.buildSingleArgs(getModelId(), ZINGG_DIR, resource(TEST_DATA_FILE),
				TRAINING_DATA_FILE, OUTPUT_DIR, STOP_WORDS);
	}

	/** test data lives on the classpath; the executors need a real path to read it from */
	protected String resource(String classpathLocation) {
		return Objects.requireNonNull(getClass().getClassLoader().getResource(classpathLocation)).getFile();
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
