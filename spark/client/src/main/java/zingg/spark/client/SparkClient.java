package zingg.spark.client;

import java.io.Serializable;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataType;

import zingg.common.client.*;

/**
 * This is the main point of interface with the Zingg matching product.
 * 
 * @author sgoyal
 *
 */
public class SparkClient extends Client<SparkSession, Dataset<Row>, Row, Column, DataType>  implements Serializable {
	
	public SparkClient(Arguments args, ClientOptions options) throws ZinggClientException {
		super(args, options);
		
	}

	public SparkClient() {
		SparkSession session = SparkSession
                .builder()
                .appName("Zingg")
                .getOrCreate();
		JavaSparkContext ctx = new JavaSparkContext(session.sparkContext());
        JavaSparkContext.jarOfClass(IZingg.class);

	}

	@Override
	public IZinggFactory getZinggFactory() throws InstantiationException, IllegalAccessException, ClassNotFoundException{
		return (IZinggFactory) Class.forName("zingg.spark.core.executor.SparkZFactory").newInstance();
	}
	

	@Override
	public Client<SparkSession, Dataset<Row>, Row, Column, DataType> getClient(Arguments args, 
		ClientOptions options) throws ZinggClientException {
		// TODO Auto-generated method stub
		return new SparkClient(args, options);
	}

	public static void main(String... args) {
		SparkClient client = new SparkClient();
		client.mainMethod(args);
	}

	
}