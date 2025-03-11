package zingg.spark.core.preprocess.casenormalize;

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
import zingg.common.core.preprocess.casenormalize.CaseNormalizer;
import zingg.common.core.preprocess.casenormalize.TestCaseNormalizer;
import zingg.spark.client.util.SparkDFObjectUtil;
import zingg.spark.core.TestSparkBase;
import zingg.spark.core.context.ZinggSparkContext;
import zingg.spark.core.util.SparkCaseNormalizerUtility;

import java.util.List;

@ExtendWith(TestSparkBase.class)
public class TestSparkCaseNormalizer extends TestCaseNormalizer<SparkSession, Dataset<Row>, Row, Column, DataType> {

    public static IWithSession<SparkSession> iWithSession = new WithSession<SparkSession>();
    public static ZinggSparkContext zsCTX = new ZinggSparkContext();

    public TestSparkCaseNormalizer(SparkSession sparkSession) throws ZinggClientException {
        super(new SparkDFObjectUtil(iWithSession), new SparkCaseNormalizerUtility(zsCTX), zsCTX);
        iWithSession.setSession(sparkSession);
        zsCTX.init(sparkSession);
    }

    @Override
    protected CaseNormalizer<SparkSession, Dataset<Row>, Row, Column, DataType> getCaseNormalizer(IContext<SparkSession, Dataset<Row>, Row, Column, DataType> context,
                                                                                                  List<? extends FieldDefinition> fieldDefinitions) {
        return new SparkCaseNormalizer(context, fieldDefinitions);
    }
}
