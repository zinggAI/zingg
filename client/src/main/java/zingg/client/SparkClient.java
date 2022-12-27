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
public class SparkClient extends Client<SparkSession, Dataset<Row>, Row, Column, DataType>  implements Serializable {
	
	public SparkClient(Arguments args, ClientOptions options) throws ZinggClientException {
		super(args, options);
		JavaSparkContext ctx = new JavaSparkContext(session.sparkContext());
        JavaSparkContext.jarOfClass(IZingg.class);
	}

	public SparkClient() {

	}

	@Override
	public Client<SparkSession, Dataset<Row>, Row, Column, DataType> getClient(Arguments args, ClientOptions options) throws ZinggClientException {
		// TODO Auto-generated method stub
		return new SparkClient(args, options);
	}

	public static void main(String... args) {
		SparkClient client = new SparkClient();
		client.mainMethod(args);
	}

	
}