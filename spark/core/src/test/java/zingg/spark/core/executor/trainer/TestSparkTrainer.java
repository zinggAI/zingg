package zingg.spark.core.executor.trainer;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.types.DataType;
import org.junit.jupiter.api.extension.ExtendWith;
import zingg.common.client.ZinggClientException;
import zingg.common.client.util.IWithSession;
import zingg.common.client.util.WithSession;
import zingg.common.core.executor.Trainer;
import zingg.common.core.executor.trainer.TestTrainer;
import zingg.common.core.executor.trainer.util.IDataFrameUtility;
import zingg.spark.core.TestSparkBase;
import zingg.spark.core.context.ZinggSparkContext;
import zingg.spark.core.executor.SparkTrainer;
import zingg.spark.core.executor.trainer.util.SparkDataFrameUtility;

@ExtendWith(TestSparkBase.class)
public class TestSparkTrainer extends TestTrainer<SparkSession, Dataset<Row>, Row, Column, DataType> {

    private ZinggSparkContext zinggSparkContext;
    private IWithSession<SparkSession> iWithSession;
    
    public TestSparkTrainer(SparkSession sparkSession) throws ZinggClientException {
        this.zinggSparkContext = new ZinggSparkContext();
        this.iWithSession = new WithSession<SparkSession>();
		zinggSparkContext.init(sparkSession); 
        iWithSession.setSession(sparkSession);
        initialize(zinggSparkContext);
        setUpDF(sparkSession);
    }

    @Override
    public Trainer<SparkSession, Dataset<Row>, Row, Column, DataType> getTestTrainer() {
        return new SparkTrainer();
    }

    @Override
    public IDataFrameUtility<SparkSession, Dataset<Row>, Row, Column, DataType> getDataFrameUtility() {
        return new SparkDataFrameUtility();
    }

}