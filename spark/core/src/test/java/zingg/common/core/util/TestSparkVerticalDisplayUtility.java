package zingg.common.core.util;

import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.junit.jupiter.api.extension.ExtendWith;
import zingg.TestSparkBase;
import zingg.common.client.util.IWithSession;
import zingg.common.client.util.VerticalDisplayUtility;
import zingg.common.client.util.WithSession;
import zingg.spark.client.util.SparkDFObjectUtil;
import zingg.spark.client.util.SparkVerticalDisplayUtility;


@ExtendWith(TestSparkBase.class)
public class TestSparkVerticalDisplayUtility extends TestVerticalDisplayUtility<SparkSession, Dataset<Row>, Row, Column> {

    private static final IWithSession<SparkSession> iWithSession = new WithSession<SparkSession>();

    public TestSparkVerticalDisplayUtility(SparkSession sparkSession) {
        super(new SparkDFObjectUtil(iWithSession));
        iWithSession.setSession(sparkSession);
    }

    @Override
    protected VerticalDisplayUtility<Dataset<Row>, Row, Column> getVerticalDisplayUtility() {
        return new SparkVerticalDisplayUtility();
    }
}