package zingg.spark.core.documenter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataType;

import org.junit.jupiter.api.extension.ExtendWith;

import zingg.common.core.context.IContext;
import zingg.common.core.documenter.DataDocumenter;
import zingg.common.core.documenter.TestDataDocumenterBase;
import zingg.spark.core.TestSparkBase;
import zingg.common.client.ClientOptions;
import zingg.common.client.arguments.model.IArguments;
import zingg.common.client.ZinggClientException;
import zingg.spark.core.context.ZinggSparkContext;

@ExtendWith(TestSparkBase.class)
public class TestSparkDataDocumenter extends TestDataDocumenterBase<SparkSession, Dataset<Row>, Row, Column, DataType> {

	public static final Log LOG = LogFactory.getLog(TestSparkDataDocumenter.class);
	private ZinggSparkContext zinggSparkContext;
	
	public TestSparkDataDocumenter(SparkSession sparkSession) throws ZinggClientException {
		this.zinggSparkContext = new ZinggSparkContext();
		zinggSparkContext.init(sparkSession);
		initialize(zinggSparkContext);
	}

	@Override
	protected DataDocumenter<SparkSession, Dataset<Row>, Row, Column, DataType> getDataDocumenter(IContext<SparkSession, Dataset<Row>, Row, Column, DataType> context, IArguments args, ClientOptions options) {
		return new SparkDataDocumenter(context, args, options);
	}

	
}
