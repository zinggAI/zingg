package zingg.spark.core.documenter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataType;

import org.junit.jupiter.api.extension.ExtendWith;

import zingg.common.client.ClientOptions;
import zingg.common.client.ZinggClientException;
import zingg.common.core.documenter.TestDocumenterExecutorBase;
import zingg.common.core.executor.Documenter;
import zingg.spark.core.TestSparkBaseLite;
import zingg.spark.core.context.ZinggSparkContext;
import zingg.spark.core.executor.SparkDocumenter;

@ExtendWith(TestSparkBaseLite.class)
public class TestSparkDocumenterExecutor
        extends TestDocumenterExecutorBase<SparkSession, Dataset<Row>, Row, Column, DataType> {

    public static final Log LOG = LogFactory.getLog(TestSparkDocumenterExecutor.class);

    private final SparkSession sparkSession;
    private final ZinggSparkContext zinggSparkContext;

    public TestSparkDocumenterExecutor(SparkSession sparkSession) throws ZinggClientException {
        this.sparkSession = sparkSession;
        this.zinggSparkContext = new ZinggSparkContext();
        zinggSparkContext.init(sparkSession);
        initialize(zinggSparkContext);
    }

    @Override
    protected Documenter<SparkSession, Dataset<Row>, Row, Column, DataType> getDocumenter()
            throws ZinggClientException {
        SparkDocumenter documenter = new SparkDocumenter(zinggSparkContext);
        documenter.init(docArguments, sparkSession, new ClientOptions());
        return documenter;
    }
}
