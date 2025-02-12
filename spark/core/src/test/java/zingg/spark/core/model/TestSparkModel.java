package zingg.spark.core.model;

import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataType;

import zingg.common.client.ZinggClientException;
import zingg.common.client.util.IWithSession;
import zingg.common.client.util.WithSession;
import zingg.common.core.model.TestModelBase;
import zingg.common.core.util.ModelUtil;
import zingg.spark.client.util.SparkDFObjectUtil;
import zingg.spark.core.TestSparkBase;
import zingg.spark.core.context.ZinggSparkContext;
import zingg.spark.core.util.SparkModelUtil;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(TestSparkBase.class)
public class TestSparkModel extends TestModelBase<SparkSession, Dataset<Row>, Row, Column, DataType> {

    private SparkSession sparkSession;
    private static ZinggSparkContext zinggSparkContext = new ZinggSparkContext();
    public static IWithSession<SparkSession> iWithSession = new WithSession<SparkSession>();

    public TestSparkModel(SparkSession sparkSession) throws ZinggClientException {
        super(new SparkDFObjectUtil(iWithSession), zinggSparkContext);
        this.sparkSession = sparkSession;
        zinggSparkContext.init(sparkSession);
        iWithSession.setSession(sparkSession);
    }

    @Override
    public ModelUtil<SparkSession, DataType, Dataset<Row>, Row, Column> getModelUtil() {
        return new SparkModelUtil(sparkSession);
    }
    
}

