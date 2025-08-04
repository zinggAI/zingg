package zingg.spark.core.recommender;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.types.DataType;

import zingg.common.client.arguments.model.IArguments;
import zingg.common.core.context.IContext;
import zingg.common.core.recommender.StopWordsRecommender;
import org.apache.spark.sql.SparkSession;


/**
 * Spark specific implementation of StopWordsRecommender
 * 
 *
 */
public class SparkStopWordsRecommender extends StopWordsRecommender<SparkSession, Dataset<Row>, Row, Column,DataType> {

	private static final long serialVersionUID = 1L;
	public static String name = "zingg.spark.SparkStopWordsRecommender";
	public static final Log LOG = LogFactory.getLog(SparkStopWordsRecommender.class);

	public SparkStopWordsRecommender(IContext<SparkSession, Dataset<Row>, Row, Column,DataType> context, IArguments args) {
		super(context,args);
	}
	
}
