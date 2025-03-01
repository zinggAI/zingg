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
import zingg.common.client.IArguments;
import zingg.common.client.ZinggClientException;
import zingg.common.core.context.IContext;
import zingg.common.core.documenter.DocumenterBase;
import zingg.common.core.documenter.TestDocumenterBase;
import zingg.spark.core.TestSparkBase;
import zingg.spark.core.context.ZinggSparkContext;

@ExtendWith(TestSparkBase.class)
public class TestSparkDocumenter extends TestDocumenterBase<SparkSession, Dataset<Row>, Row, Column, DataType>{

	public static final Log LOG = LogFactory.getLog(TestSparkDocumenter.class);
	private ZinggSparkContext zinggSparkContext;

	public TestSparkDocumenter(SparkSession sparkSession) throws ZinggClientException {
		this.zinggSparkContext = new ZinggSparkContext();
		zinggSparkContext.init(sparkSession);
		initialize(zinggSparkContext);
	}

	@Override
	protected DocumenterBase<SparkSession, Dataset<Row>, Row, Column, DataType> getDocumenter(IContext<SparkSession, Dataset<Row>, Row, Column, DataType> context, IArguments args, ClientOptions options) {
		return new SparkModelDocumenter(context, args, options);
	}

	
}
