package zingg.spark.core.executor;

import zingg.common.client.ZinggClientException;
import zingg.common.core.executor.TestExecutorsCompound;
import zingg.common.core.executor.TrainMatcher;
import zingg.spark.core.context.ZinggSparkContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataType;

public class TestSparkExecutorsCompound extends TestExecutorsCompound<SparkSession,Dataset<Row>,Row,Column,DataType> {

    public static final Log LOG = LogFactory.getLog(TestSparkExecutorsCompound.class);
	
	protected ZinggSparkContext ctx;

    @Override
	protected SparkFindAndLabeller getFindAndLabeller() throws ZinggClientException {
		SparkFindAndLabeller sfal = new SparkFindAndLabeller(ctx);
        sfal.setLabeller(new ProgrammaticSparkLabeller(ctx));
		return sfal;
	}

	@Override
	protected SparkTrainMatcher getTrainMatcher() throws ZinggClientException {
		SparkTrainMatcher stm = new SparkTrainMatcher(ctx);
		return stm;
	}

	@Override
	protected SparkTrainMatchTester getTrainMatchValidator(TrainMatcher<SparkSession,Dataset<Row>,Row,Column,DataType> trainMatch) {
		return new SparkTrainMatchTester(trainMatch,args);
	}
    
}
