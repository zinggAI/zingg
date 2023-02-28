package zingg.spark;

import static org.apache.spark.sql.functions.explode;
import static org.apache.spark.sql.functions.split;
import static org.apache.spark.sql.functions.sum;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataType;

import zingg.client.Arguments;
import zingg.client.SparkFrame;
import zingg.client.ZFrame;
import zingg.common.Context;
import zingg.recommender.StopWordsRecommender;


/**
 * Spark specific implementation of StopWordsRecommender
 * 
 * @author vikasgupta
 *
 */
public class SparkStopWordsRecommender extends StopWordsRecommender<SparkSession, Dataset<Row>, Row, Column,DataType> {

	private static final long serialVersionUID = 1L;
	public static String name = "zingg.spark.SparkStopWordsRecommender";
	public static final Log LOG = LogFactory.getLog(SparkStopWordsRecommender.class);

	public SparkStopWordsRecommender(Context<SparkSession, Dataset<Row>, Row, Column,DataType> context,Arguments args) {
		super(context,args);
	}
	
	@Override
	protected ZFrame<Dataset<Row>, Row, Column> filterOnThreshold(ZFrame<Dataset<Row>, Row, Column> zDF, double threshold, String countColName) {
		Dataset<Row> sparkDF = zDF.df();
		sparkDF = sparkDF.filter(sparkDF.col(countColName).gt(threshold));
		sparkDF = sparkDF.coalesce(1);
		return new SparkFrame(sparkDF);	
	}

	@Override
	protected double getThreshold(ZFrame<Dataset<Row>, Row, Column> zDF, String countColName) {
		Dataset<Row> sparkDF = zDF.df();
		long count = sparkDF.agg(sum(countColName)).collectAsList().get(0).getLong(0);
		double threshold = count * args.getStopWordsCutoff();
		return threshold;
	}
	
	@Override
	protected ZFrame<Dataset<Row>, Row, Column> getCount(ZFrame<Dataset<Row>, Row, Column> zDF, String wordColName, String countColName) {
		Dataset<Row> sparkDF = zDF.df();
		return new SparkFrame(sparkDF.groupBy(wordColName).count().withColumnRenamed("count",countColName));
	}
	
	@Override
	protected ZFrame<Dataset<Row>, Row, Column> convertToRows(ZFrame<Dataset<Row>, Row, Column> zDF, String splitColName, String wordColName) {
		Dataset<Row> sparkDF = zDF.df();
		return new SparkFrame(sparkDF.select(explode(sparkDF.col(splitColName)).as(wordColName)));
	}
	
	@Override
	protected ZFrame<Dataset<Row>, Row, Column> splitFieldOnWhiteSpace(ZFrame<Dataset<Row>, Row, Column> zDF, String fieldName, String splitColName) {
		Dataset<Row> sparkDF = zDF.df();
		return new SparkFrame(sparkDF.select(split(sparkDF.col(fieldName), "\\s+").as(splitColName)));
	}	
	
}
