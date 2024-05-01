package zingg.spark.core.executor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.types.DataType;

import zingg.common.client.IArguments;
import zingg.common.client.ZinggClientException;
import zingg.common.client.options.ZinggOptions;

import zingg.common.core.executor.Recommender;
import zingg.common.core.recommender.StopWordsRecommender;
import org.apache.spark.sql.SparkSession;
import zingg.spark.core.context.ZinggSparkContext;
import zingg.spark.core.recommender.SparkStopWordsRecommender;


/**
 * Spark specific implementation of Recommender
 *
 */
public class SparkRecommender extends Recommender<SparkSession, Dataset<Row>, Row, Column,DataType> {

	private static final long serialVersionUID = 1L;
	public static String name = "zingg.spark.core.executor.SparkRecommender";
	public static final Log LOG = LogFactory.getLog(SparkRecommender.class);

	public SparkRecommender() {
		this(new ZinggSparkContext());
	}

	public SparkRecommender(ZinggSparkContext sparkContext) {
		setZinggOption(ZinggOptions.RECOMMEND);
		setContext(sparkContext);
	}	
	
    @Override
    public void init(IArguments args, SparkSession s)  throws ZinggClientException {
        super.init(args,s);
        getContext().init(s);
    }	

    @Override
    public StopWordsRecommender<SparkSession, Dataset<Row>, Row, Column, DataType> getStopWordsRecommender() {
    	StopWordsRecommender<SparkSession, Dataset<Row>, Row, Column, DataType> stopWordsRecommender = new SparkStopWordsRecommender(getContext(),args);    	
    	return stopWordsRecommender;
    }
	
}
