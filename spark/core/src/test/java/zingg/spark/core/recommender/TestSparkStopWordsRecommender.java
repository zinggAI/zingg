package zingg.spark.core.recommender;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataType;
import org.junit.jupiter.api.extension.ExtendWith;

import zingg.common.client.IArguments;
import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;
import zingg.common.client.util.IWithSession;
import zingg.common.client.util.WithSession;
import zingg.common.core.context.IContext;
import zingg.common.core.recommender.StopWordsRecommender;
import zingg.common.core.recommender.TestStopWordsRecommenderBase;
import zingg.spark.client.SparkFrame;
import zingg.spark.client.util.SparkDFObjectUtil;
import zingg.spark.core.TestSparkBase;
import zingg.spark.core.context.ZinggSparkContext;

@ExtendWith(TestSparkBase.class)
public class TestSparkStopWordsRecommender extends TestStopWordsRecommenderBase<SparkSession, Dataset<Row>, Row, Column, DataType> {

	public static final Log LOG = LogFactory.getLog(TestSparkStopWordsRecommender.class);
	private ZinggSparkContext zinggSparkContext;
	private IWithSession<SparkSession> iWithSession;

	public TestSparkStopWordsRecommender(SparkSession sparkSession) throws ZinggClientException {
		this.zinggSparkContext = new ZinggSparkContext();
		this.iWithSession = new WithSession<SparkSession>();
		zinggSparkContext.init(sparkSession);
		iWithSession.setSession(sparkSession);
		initialize(new SparkDFObjectUtil(iWithSession), zinggSparkContext);
	}

	@Override
	public StopWordsRecommender<SparkSession, Dataset<Row>, Row, Column, DataType> getRecommender(IContext<SparkSession, Dataset<Row>, Row, Column, DataType> context, IArguments args) {
		return new SparkStopWordsRecommender(context,args);
	}

	@Override
	public ZFrame<Dataset<Row>, Row, Column> getStopWordsDataset(ZFrame<Dataset<Row>, Row, Column> dataset) {
		return new SparkFrame(dataset.df());
	}

	@Override
	public String getStopWordColName() {
		return "z_word";
	}


	
} 
