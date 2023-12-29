package zingg.spark.client;

import java.io.Serializable;

import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.types.DataType;
import org.apache.spark.sql.SparkSession;

import zingg.common.client.Client;
import zingg.common.client.ClientOptions;
import zingg.common.client.IArguments;
import zingg.common.client.IZinggFactory;
import zingg.common.client.ZinggClientException;
/**
 * This is the main point of interface with the Zingg matching product.
 * 
 * @author sgoyal
 *
 */
public class SparkClient extends Client<SparkSession, Dataset<Row>, Row, Column, DataType> {
	
	private static final long serialVersionUID = 1L;
	protected static final String zFactoryClassName = "zingg.spark.core.executor.SparkZFactory";

	public SparkClient(IArguments args, ClientOptions options) throws ZinggClientException {
		super(args, options, zFactoryClassName);
		
	}
		
	

	public SparkClient(IArguments args, ClientOptions options, SparkSession s) throws ZinggClientException {
		super(args, options, s, zFactoryClassName);
	}

	
	public SparkClient() {
		/*SparkSession session = SparkSession
                .builder()
                .appName("Zingg")
                .getOrCreate();
		JavaSparkContext ctx = JavaSparkContext.fromSparkContext(session.sparkContext());
        JavaSparkContext.jarOfClass(IZingg.class);
		
		*/
		super(zFactoryClassName);

	}


	@Override
	public Client<SparkSession, Dataset<Row>, Row, Column, DataType> getClient(IArguments args, 
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