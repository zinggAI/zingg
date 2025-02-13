package zingg.spark.core.executor.trainer;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.types.DataType;
import org.junit.jupiter.api.extension.ExtendWith;
import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;
import zingg.common.client.util.IWithSession;
import zingg.common.client.util.WithSession;
import zingg.common.core.executor.Trainer;
import zingg.common.core.executor.trainer.TestTrainer;
import zingg.common.core.executor.trainer.util.IDataFrameUtility;
import zingg.spark.client.SparkFrame;
import zingg.spark.core.TestSparkBase;
import zingg.spark.core.context.ZinggSparkContext;
import zingg.spark.core.executor.SparkTrainer;
import zingg.spark.core.executor.trainer.util.SparkDataFrameUtility;

@ExtendWith(TestSparkBase.class)
public class TestSparkTrainer extends TestTrainer<SparkSession, Dataset<Row>, Row, Column, DataType> {

    private static ZinggSparkContext zinggSparkContext = new ZinggSparkContext();
    public static IWithSession<SparkSession> iWithSession = new WithSession<SparkSession>();
    
    public TestSparkTrainer(SparkSession sparkSession) throws ZinggClientException {
        super(zinggSparkContext); 
        iWithSession.setSession(sparkSession);
		zinggSparkContext.init(sparkSession); 
        setUpDF(sparkSession);
    }

    @Override
    public Trainer<SparkSession, Dataset<Row>, Row, Column, DataType> getTestTrainer() {
        return new SparkTrainer();
    }

    @Override
    public ZFrame<Dataset<Row>, Row, Column> getTenRowsDF(ZFrame<Dataset<Row>, Row, Column> tenRowsDF) {
       return new SparkFrame(tenRowsDF.df());
    }

    @Override
    public ZFrame<Dataset<Row>, Row, Column> getOneRowsDF(ZFrame<Dataset<Row>, Row, Column> oneRowsDF) {
        return new SparkFrame(oneRowsDF.df());
    }

    @Override
    public IDataFrameUtility<SparkSession, Dataset<Row>, Row, Column, DataType> getDataFrameUtility() {
        return new SparkDataFrameUtility();
    }

}