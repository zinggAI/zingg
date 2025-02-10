package zingg.spark.core.executor;

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
import zingg.common.core.executor.TestTrainer;
import zingg.common.core.executor.Trainer;
import zingg.spark.core.TestSparkBase;
import zingg.spark.core.context.ZinggSparkContext;
import zingg.spark.core.util.SparkDataFrameUtility;

@ExtendWith(TestSparkBase.class)
public class TestSparkTrainer extends TestTrainer<SparkSession, Dataset<Row>, Row, Column, DataType> {

    private static ZinggSparkContext zinggSparkContext = new ZinggSparkContext();
    public static IWithSession<SparkSession> iWithSession = new WithSession<SparkSession>();
    protected ZFrame<Dataset<Row>, Row, Column> tenRowsDF;
    protected ZFrame<Dataset<Row>, Row, Column> oneRowsDF;

    public TestSparkTrainer(SparkSession sparkSession) throws ZinggClientException {
        super(zinggSparkContext); 
        iWithSession.setSession(sparkSession);
		zinggSparkContext.init(sparkSession); 
        setupDF();
    }

    public void setupDF() {
        tenRowsDF = new SparkDataFrameUtility().createDFWithDoubles(10,1, context.getSession());
        oneRowsDF = new SparkDataFrameUtility().createDFWithDoubles(1,1, context.getSession());
    }

    @Override
    public Trainer<SparkSession, Dataset<Row>, Row, Column, DataType> getTestTrainer() {
        return new SparkTrainer();
    }

    @Override
    public ZFrame<Dataset<Row>, Row, Column> getTenRowsDF() {
       return tenRowsDF;
    }

    @Override
    public ZFrame<Dataset<Row>, Row, Column> getOneRowsDF() {
        return oneRowsDF;
    }

}