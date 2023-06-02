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
	
	private static final long serialVersionUID = 1L;

	public SparkClient(Arguments args, ClientOptions options) throws ZinggClientException {
		super(args, options);
		
	}

	public SparkClient(Arguments args, ClientOptions options, SparkSession s) throws ZinggClientException {
		super(args, options, s);
	}

	public SparkClient() {
		/*SparkSession session = SparkSession
                .builder()
                .appName("Zingg")
                .getOrCreate();
		JavaSparkContext ctx = JavaSparkContext.fromSparkContext(session.sparkContext());
        JavaSparkContext.jarOfClass(IZingg.class);
		*/

	}

	@Override
	public IZinggFactory getZinggFactory() throws InstantiationException, IllegalAccessException, ClassNotFoundException{
		return (IZinggFactory) Class.forName("zingg.spark.core.executor.SparkZFactory").newInstance();
	}
	

	@Override
	public Client<SparkSession, Dataset<Row>, Row, Column, DataType> getClient(Arguments args, 
		ClientOptions options) throws ZinggClientException {
		// TODO Auto-generated method stub
		SparkClient client = null;
		if ((session != null)) {
			LOG.debug("Creating client with existing session");
			client = new SparkClient(args, options, session);
		}
		else {
			client = new SparkClient(args, options);
		}
		
		return client;
	}

	public static void main(String... args) {
		SparkClient client = new SparkClient();
		client.mainMethod(args);
	}

	
}