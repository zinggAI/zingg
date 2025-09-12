package zingg.spark.core.model;

import org.apache.spark.internal.config.R;
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
    private ZinggSparkContext zinggSparkContext;
    private IWithSession<SparkSession> iWithSession;

    public TestSparkModel(SparkSession sparkSession) throws ZinggClientException {        
        this.sparkSession = sparkSession;
        zinggSparkContext = new ZinggSparkContext();
        iWithSession = new WithSession<>();
        zinggSparkContext.init(sparkSession);
        iWithSession.setSession(sparkSession);
        initialize(new SparkDFObjectUtil(iWithSession), zinggSparkContext);
    }

    @Override
    public ModelUtil<SparkSession, Dataset<Row>, Row, Column, DataType> getModelUtil() {
        return new SparkModelUtil(sparkSession);
    }
    
}

