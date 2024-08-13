package zingg.common.core.block;

import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataType;
import org.junit.jupiter.api.extension.ExtendWith;
import zingg.TestSparkBase;
import zingg.common.client.ZinggClientException;
import zingg.common.client.util.IWithSession;
import zingg.common.client.util.WithSession;
import zingg.spark.client.util.SparkDFObjectUtil;
import zingg.spark.core.context.ZinggSparkContext;
import zingg.spark.core.util.SparkBlockingTreeUtil;
import zingg.spark.core.util.SparkHashUtil;

@ExtendWith(TestSparkBase.class)
public class TestSparkBlock extends TestBlockBase<SparkSession, Dataset<Row>, Row, Column, DataType> {

    public static ZinggSparkContext zsCTX = new ZinggSparkContext();
    public static IWithSession<SparkSession> iWithSession = new WithSession<SparkSession>();

    public TestSparkBlock(SparkSession sparkSession) throws ZinggClientException {
        super(new SparkDFObjectUtil(iWithSession), new SparkHashUtil(sparkSession), new SparkBlockingTreeUtil(sparkSession, zsCTX.getPipeUtil()));
        iWithSession.setSession(sparkSession);
        zsCTX.init(sparkSession);
    }
}
