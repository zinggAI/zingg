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
import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;
import zingg.common.client.util.IWithSession;
import zingg.common.client.util.WithSession;
import zingg.common.core.context.IContext;
import zingg.common.core.documenter.ModelDocumenter;
import zingg.common.core.documenter.TestModelDocumenterBase;
import zingg.spark.client.SparkFrame;
import zingg.spark.core.TestSparkBase;
import zingg.spark.core.context.ZinggSparkContext;


@ExtendWith(TestSparkBase.class)
public class TestSparkModelDocumenter extends TestModelDocumenterBase<SparkSession, Dataset<Row>, Row, Column, DataType> {

	public static final Log LOG = LogFactory.getLog(TestSparkModelDocumenter.class);
	private SparkSession sparkSession;
	private static ZinggSparkContext zinggSparkContext = new ZinggSparkContext();
	public static IWithSession<SparkSession> iWithSession = new WithSession<SparkSession>();

	public TestSparkModelDocumenter(SparkSession sparkSession) throws ZinggClientException {
		super(zinggSparkContext);
		this.sparkSession = sparkSession;
		iWithSession.setSession(sparkSession);
		zinggSparkContext.init(sparkSession);
	}

	@Override
	protected ZFrame<Dataset<Row>, Row, Column> getMarkedRecordsZFrame() {
		return new SparkFrame(sparkSession.emptyDataFrame());
	}

	@Override
	protected ModelDocumenter<SparkSession, Dataset<Row>, Row, Column, DataType> getModelDocumenter(IContext<SparkSession, Dataset<Row>, Row, Column, DataType> context, IArguments args, ClientOptions options) {
			return new SparkModelDocumenter(context, args, options);
	}

}
