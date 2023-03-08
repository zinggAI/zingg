package zingg.spark.core.recommender;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataType;

import zingg.common.client.Arguments;
import zingg.common.core.Context;
import zingg.common.core.recommender.StopWordsRecommender;


/**
 * Spark specific implementation of StopWordsRecommender
 * 
 *
 */
public class SparkStopWordsRecommender extends StopWordsRecommender<SparkSession, Dataset<Row>, Row, Column,DataType> {

	private static final long serialVersionUID = 1L;
	public static String name = "zingg.spark.SparkStopWordsRecommender";
	public static final Log LOG = LogFactory.getLog(SparkStopWordsRecommender.class);

	public SparkStopWordsRecommender(Context<SparkSession, Dataset<Row>, Row, Column,DataType> context,Arguments args) {
		super(context,args);
	}
	
}
