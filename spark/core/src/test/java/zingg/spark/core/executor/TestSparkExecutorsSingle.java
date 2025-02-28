package zingg.spark.core.executor;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataType;

import org.junit.jupiter.api.extension.ExtendWith;
import zingg.common.client.ZinggClientException;
import zingg.common.client.util.DFObjectUtil;
import zingg.common.client.util.IWithSession;
import zingg.common.client.util.WithSession;
import zingg.common.core.executor.Labeller;
import zingg.common.core.executor.TestExecutorsSingle;
import zingg.common.core.executor.Trainer;
import zingg.spark.client.util.SparkDFObjectUtil;
import zingg.spark.core.TestSparkBase;
import zingg.spark.core.context.ZinggSparkContext;
import zingg.spark.core.executor.labeller.ProgrammaticSparkLabeller;
import zingg.spark.core.executor.validate.SparkTrainerValidator;

@ExtendWith(TestSparkBase.class)
public class TestSparkExecutorsSingle extends TestExecutorsSingle<SparkSession,Dataset<Row>,Row,Column,DataType> {
	protected static final String CONFIG_FILE = "zingg/spark/core/executor/single/configSparkIntTest.json";
	protected static final String CONFIGLINK_FILE = "zingg/spark/core/executor/single/configSparkLinkTest.json";
	protected static final String TEST1_DATA_FILE = "zingg/spark/core/executor/test1.csv";
	protected static final String TEST2_DATA_FILE = "zingg/spark/core/executor/test2.csv";
	private final SparkSession sparkSession;
	public static final Log LOG = LogFactory.getLog(TestSparkExecutorsSingle.class);
	
	protected ZinggSparkContext ctx;
	
	public TestSparkExecutorsSingle(SparkSession sparkSession) throws IOException, ZinggClientException {
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

}
