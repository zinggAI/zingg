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
import zingg.common.core.executor.TestExecutorsCompound;
import zingg.common.core.executor.TrainMatcher;
import zingg.common.core.util.ICleanUpUtil;
import zingg.spark.client.util.SparkDFObjectUtil;
import zingg.spark.core.TestSparkBase;
import zingg.spark.core.context.ZinggSparkContext;
import zingg.spark.core.executor.labeller.ProgrammaticSparkLabeller;
import zingg.spark.core.executor.validate.SparkTrainMatchValidator;
import zingg.spark.core.util.SparkCleanUpUtil;

@ExtendWith(TestSparkBase.class)
public class TestSparkExecutorsCompound extends TestExecutorsCompound<SparkSession,Dataset<Row>,Row,Column,DataType> {
	protected static final String CONFIG_FILE = "zingg/spark/core/executor/compound/configSparkIntTest.json";
	protected static final String TEST_DATA_FILE = "zingg/spark/core/executor/test.csv";

    public static final Log LOG = LogFactory.getLog(TestSparkExecutorsCompound.class);
	
	protected ZinggSparkContext ctx;

	public TestSparkExecutorsCompound(SparkSession sparkSession) throws IOException, ZinggClientException {
		this.ctx = new ZinggSparkContext();
		this.ctx.setSession(sparkSession);
		this.ctx.setUtils();
		init(sparkSession);
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
	
}
