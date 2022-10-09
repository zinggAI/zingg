package zingg.client;

import java.io.Serializable;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataType;

/**
 * This is the main point of interface with the Zingg matching product.
 * 
 * @author sgoyal
 *
 */
public class SparkClient extends Client<SparkSession, Dataset, Row, Column, DataType>  implements Serializable {
	
	public SparkClient(Arguments args, ClientOptions options, SparkSession session) throws ZinggClientException {
		super(args, options, session);
		JavaSparkContext ctx = new JavaSparkContext(session.sparkContext());
        JavaSparkContext.jarOfClass(IZingg.class);
	}

	
}