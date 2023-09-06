package zingg.spark.core.executor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.types.DataType;

import zingg.common.client.Arguments;
import zingg.common.client.ZinggClientException;
import zingg.common.client.ZinggOptions;

import zingg.common.core.executor.TrainMatcher;
import zingg.spark.core.context.ZinggSparkContext;
import org.apache.spark.sql.SparkSession;
 
public class SparkTrainMatcher extends TrainMatcher<SparkSession, Dataset<Row>, Row, Column,DataType> {

	private static final long serialVersionUID = 1L;
	public static String name = "zingg.spark.core.executor.SparkTrainMatcher";
	public static final Log LOG = LogFactory.getLog(SparkTrainMatcher.class);

	public SparkTrainMatcher() {
		setZinggOptions(ZinggOptions.TRAIN_MATCH);
		ZinggSparkContext sparkContext = new ZinggSparkContext();
		setContext(sparkContext);
		trainer = new SparkTrainer(sparkContext);
		matcher = new SparkMatcher(sparkContext);
	}

    @Override
    public void init(Arguments args)  throws ZinggClientException {
        super.init(args);
        //getContext().init(license);
    }
        	
}
