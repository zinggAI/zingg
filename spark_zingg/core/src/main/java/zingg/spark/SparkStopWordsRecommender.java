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
import zingg.client.util.ColName;
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
	public ZFrame<Dataset<Row>, Row, Column> findStopWords(ZFrame<Dataset<Row>, Row, Column> zDF, String fieldName) {
		LOG.debug("Field: " + fieldName);
		
		Dataset<Row> data = zDF.df();
		
		if(!data.isEmpty()) {
			data = data.select(split(data.col(fieldName), "\\s+").as(ColName.COL_SPLIT));
			data = data.select(explode(data.col(ColName.COL_SPLIT)).as(ColName.COL_WORD));
			data = data.filter(data.col(ColName.COL_WORD).notEqual(""));
			data = data.groupBy(ColName.COL_WORD).count().withColumnRenamed("count", ColName.COL_COUNT);
			long count = data.agg(sum(ColName.COL_COUNT)).collectAsList().get(0).getLong(0);
			double threshold = count * args.getStopWordsCutoff();
			data = data.filter(data.col(ColName.COL_COUNT).gt(threshold));
			data = data.coalesce(1);
		}
		
		return new SparkFrame(data);		

	}	
	
}
