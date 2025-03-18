package zingg.spark.core.preprocess.trim;

import java.util.List;

import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataType;
import org.junit.jupiter.api.extension.ExtendWith;
import zingg.common.client.FieldDefinition;
import zingg.common.client.ZinggClientException;
import zingg.common.client.util.IWithSession;
import zingg.common.client.util.WithSession;
import zingg.common.core.context.IContext;
import zingg.common.core.preprocess.trim.TestTrimPreprocess;
import zingg.common.core.preprocess.trim.TrimPreprocessor;
import zingg.spark.client.util.SparkDFObjectUtil;
import zingg.spark.core.TestSparkBase;
import zingg.spark.core.context.ZinggSparkContext;
import zingg.spark.core.util.SparkTrimPreprocessUtility;

@ExtendWith(TestSparkBase.class)
public class TestSparkTrimPreprocess extends TestTrimPreprocess<SparkSession, Dataset<Row>, Row, Column, DataType> {

    public static IWithSession<SparkSession> iWithSession = new WithSession<SparkSession>();
    public static ZinggSparkContext zsCTX = new ZinggSparkContext();

    public TestSparkTrimPreprocess(SparkSession sparkSession) throws ZinggClientException {
        super(new SparkDFObjectUtil(iWithSession), new SparkTrimPreprocessUtility(), zsCTX);
        iWithSession.setSession(sparkSession);
        zsCTX.init(sparkSession);
    }

    @Override
    protected TrimPreprocessor<SparkSession, Dataset<Row>, Row, Column, DataType> getTrimPreprocessor(
            IContext<SparkSession, Dataset<Row>, Row, Column, DataType> context,
            List<? extends FieldDefinition> fieldDefinitions) {
        return new SparkTrimPreprocessor(context, fieldDefinitions);
    }

}
