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
import zingg.common.core.util.IDataFrameUtility;
import zingg.spark.core.TestSparkBase;
import zingg.spark.core.context.ZinggSparkContext;
import zingg.spark.core.util.SparkDataFrameUtility;

@ExtendWith(TestSparkBase.class)
public class TestSparkTrainer extends TestTrainer<SparkSession, Dataset<Row>, Row, Column, DataType> {

    private SparkSession sparkSession;
    private static ZinggSparkContext zinggSparkContext = new ZinggSparkContext();
    public static IWithSession<SparkSession> iWithSession = new WithSession<SparkSession>();
    protected ZFrame<Dataset<Row>, Row, Column> tenRowsDF;
    protected ZFrame<Dataset<Row>, Row, Column> oneRowsDF;

    public TestSparkTrainer(SparkSession sparkSession) throws ZinggClientException {
        super(zinggSparkContext); 
        this.sparkSession = sparkSession;
        iWithSession.setSession(sparkSession);
		zinggSparkContext.init(sparkSession); 
        setUpDF();
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

    @Override
    public void setUpDF() {
        tenRowsDF = getDataFrameUtility().createDFWithDoubles(10,1, sparkSession);
        oneRowsDF = getDataFrameUtility().createDFWithDoubles(1,1, sparkSession);
    }

    @Override
    public IDataFrameUtility<SparkSession, Dataset<Row>, Row, Column, DataType> getDataFrameUtility() {
        return new SparkDataFrameUtility();
    }

}