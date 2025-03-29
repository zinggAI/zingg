package zingg.spark.client;

import org.apache.spark.SparkContext;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataType;

import zingg.common.client.Client;
import zingg.common.client.ClientOptions;
import zingg.common.client.IZingg;
import zingg.common.client.IZArgs;
import zingg.common.client.ZinggClientException;
import zingg.common.client.util.PipeUtilBase;
import zingg.common.core.util.Analytics;
import zingg.common.core.util.Metric;
import zingg.spark.client.util.SparkPipeUtil;
/**
 * This is the main point of interface with the Zingg matching product.
 * 
 * @author sgoyal
 *
 */
public class SparkClient extends Client<SparkSession, Dataset<Row>, Row, Column, DataType> {
	
	private static final long serialVersionUID = 1L;
	protected static final String zFactoryClassName = "zingg.spark.core.executor.SparkZFactory";
	private JavaSparkContext javaSparkContext;

	public SparkClient(IZArgs args, ClientOptions options) throws ZinggClientException {
		super(args, options, zFactoryClassName);
		
	}
	

	public SparkClient(IZArgs args, ClientOptions options, SparkSession s) throws ZinggClientException {
		super(args, options, s, zFactoryClassName);
		Analytics.track(Metric.IS_PYTHON, "true", args.getCollectMetrics());
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
	public Client<SparkSession, Dataset<Row>, Row, Column, DataType> getClient(IZArgs args, 
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

	@Override
	public SparkSession getSession() {
		if (session!=null) {
			return session;
		} else {
			SparkSession s = SparkSession
                    .builder()
                    .appName("Zingg")
                    .getOrCreate();
			SparkContext sparkContext = s.sparkContext();
			if (sparkContext.getCheckpointDir().isEmpty()) {
				sparkContext.setCheckpointDir("/tmp/checkpoint");
			}
			JavaSparkContext ctx = JavaSparkContext.fromSparkContext(sparkContext);
					JavaSparkContext.jarOfClass(IZingg.class);
					LOG.debug("Context " + ctx.toString());
					//initHashFns();
			if (!ctx.getCheckpointDir().isPresent()) {
				ctx.setCheckpointDir(String.valueOf(sparkContext.getCheckpointDir()));
			}
			javaSparkContext = ctx;
			setSession(s);
			return s;
		}
		
	}
	
	@Override
	public PipeUtilBase<SparkSession, Dataset<Row>, Row, Column> getPipeUtil() {
		if (pipeUtil!=null) {
			return pipeUtil;
		} else {
			PipeUtilBase<SparkSession, Dataset<Row>, Row, Column> p = new SparkPipeUtil(session);
			setPipeUtil(p);
			return p;
		}
	}


	public JavaSparkContext getJavaSparkContext() {
		return javaSparkContext;
	}
}