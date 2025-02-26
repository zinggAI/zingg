package zingg.spark.core.executor.blockingverifier;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataType;
import org.junit.jupiter.api.extension.ExtendWith;

import zingg.common.client.ZinggClientException;
import zingg.common.client.util.IWithSession;
import zingg.common.client.util.WithSession;
import zingg.common.core.executor.blockingverifier.IVerifyBlockingPipes;
import zingg.common.core.executor.blockingverifier.TestVerifyBlocking;
import zingg.common.core.executor.blockingverifier.VerifyBlocking;
import zingg.spark.client.util.SparkDFObjectUtil;
import zingg.spark.client.util.SparkModelHelper;
import zingg.spark.client.util.SparkPipeUtil;
import zingg.spark.core.TestSparkBase;
import zingg.spark.core.context.ZinggSparkContext;
import zingg.spark.core.executor.SparkVerifyBlocker;
import zingg.spark.core.executor.SparkVerifyBlockingPipes;

@ExtendWith(TestSparkBase.class)
public class TestSparkVerifyBlocking extends TestVerifyBlocking<SparkSession,Dataset<Row>,Row,Column,DataType> {

    public static final Log LOG = LogFactory.getLog(TestSparkVerifyBlocking.class);
	private ZinggSparkContext zinggSparkContext;
	private IWithSession<SparkSession> iWithSession;

    public TestSparkVerifyBlocking(SparkSession sparkSession) throws ZinggClientException {
		this.zinggSparkContext = new ZinggSparkContext();
		this.iWithSession = new WithSession<SparkSession>();
		zinggSparkContext.init(sparkSession);
		iWithSession.setSession(sparkSession);
		initialize(new SparkDFObjectUtil(iWithSession), zinggSparkContext);
	}

    @Override
    public VerifyBlocking<SparkSession, Dataset<Row>, Row, Column, DataType> getVerifyBlocker() {
        return new SparkVerifyBlocker();
    }

    @Override
    public IVerifyBlockingPipes<SparkSession, Dataset<Row>, Row, Column> getVerifyBlockingPipes() {
        return new SparkVerifyBlockingPipes(new SparkPipeUtil(zinggSparkContext.getSession()), getVerifyBlocker().getTimestamp(), new SparkModelHelper());
    } 

    
}
