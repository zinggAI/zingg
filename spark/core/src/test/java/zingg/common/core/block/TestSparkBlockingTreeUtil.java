package zingg.common.core.block;

import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataType;
import org.junit.jupiter.api.extension.ExtendWith;
import zingg.common.client.ZFrame;
import zingg.common.client.util.DFObjectUtil;
import zingg.common.client.util.IWithSession;
import zingg.common.client.util.ListMap;
import zingg.common.client.util.WithSession;
import zingg.common.core.hash.HashFunction;
import zingg.common.core.util.BlockingTreeUtil;
import zingg.common.core.util.HashUtil;
import zingg.spark.client.util.SparkDFObjectUtil;
import zingg.spark.client.util.SparkPipeUtil;
import zingg.spark.core.TestSparkBase;
import zingg.spark.core.block.SparkBlock;
import zingg.spark.core.util.SparkBlockingTreeUtil;
import zingg.spark.core.util.SparkHashUtil;

@ExtendWith(TestSparkBase.class)
public class TestSparkBlockingTreeUtil extends TestBlockingTreeUtil<SparkSession, Dataset<Row>, Row, Column, DataType>{

    private final IWithSession<SparkSession> withSession;

    public TestSparkBlockingTreeUtil(SparkSession sparkSession) {
        withSession = new WithSession<>();
        withSession.setSession(sparkSession);
    }

    @Override
    protected DFObjectUtil<SparkSession, Dataset<Row>, Row, Column> getDFObjectUtil() {
        return new SparkDFObjectUtil(withSession);
    }

    @Override
    protected BlockingTreeUtil<SparkSession, Dataset<Row>, Row, Column, DataType> getBlockingTreeUtil() {
        return new SparkBlockingTreeUtil(withSession.getSession(), new SparkPipeUtil(withSession.getSession()));
    }

    @Override
    protected HashUtil<SparkSession, Dataset<Row>, Row, Column, DataType> getHashUtil() {
        return new SparkHashUtil(withSession.getSession());
    }

    @Override
    protected void setTestDataBaseLocation() {
        TEST_DATA_BASE_LOCATION = "testFebrl";
    }

    @Override
    protected Block<Dataset<Row>, Row, Column, DataType> getBlock(ZFrame<Dataset<Row>, Row, Column> sample, ZFrame<Dataset<Row>, Row, Column> positives, ListMap<DataType, HashFunction<Dataset<Row>, Row, Column, DataType>> hashFunctions, long blockSize) {
        return new SparkBlock(sample, positives, hashFunctions, blockSize);
    }
}
