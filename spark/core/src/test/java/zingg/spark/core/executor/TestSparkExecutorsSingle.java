package zingg.spark.core.executor;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataType;

import zingg.common.client.IZingg;
import zingg.common.client.ZinggClientException;
import zingg.common.client.util.DFObjectUtil;
import zingg.common.client.util.IWithSession;
import zingg.common.client.util.WithSession;
import zingg.common.core.executor.Labeller;
import zingg.common.core.executor.TestExecutorsSingle;
import zingg.common.core.executor.Trainer;
import zingg.spark.client.util.SparkDFObjectUtil;
import zingg.spark.core.context.ZinggSparkContext;
import zingg.spark.core.executor.labeller.ProgrammaticSparkLabeller;
import zingg.spark.core.executor.validate.SparkTrainerValidator;

public class TestSparkExecutorsSingle extends TestExecutorsSingle<SparkSession,Dataset<Row>,Row,Column,DataType> {
	protected static final String CONFIG_FILE = "zingg/spark/core/executor/configSparkIntTest.json";
	protected static final String CONFIGLINK_FILE = "zingg/spark/core/executor/configSparkLinkTest.json";
	
	public static final Log LOG = LogFactory.getLog(TestSparkExecutorsSingle.class);
	
	protected ZinggSparkContext ctx;
	
	public TestSparkExecutorsSingle() throws IOException, ZinggClientException {	
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
