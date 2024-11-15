package zingg.spark.core.executor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.types.DataType;
import org.apache.spark.sql.SparkSession;

import zingg.common.client.ClientOptions;
import zingg.common.client.IArguments;
import zingg.common.client.ZinggClientException;
import zingg.common.client.options.ZinggOptions;

import zingg.common.client.util.DFObjectUtil;
import zingg.common.client.util.IWithSession;
import zingg.common.client.util.WithSession;
import zingg.spark.client.util.SparkDFObjectUtil;
import zingg.spark.core.context.ZinggSparkContext;
import zingg.common.core.executor.Labeller;


/**
 * Spark specific implementation of Labeller
 * 
 *
 */
public class SparkLabeller extends Labeller<SparkSession, Dataset<Row>, Row, Column,DataType> {

	private static final long serialVersionUID = 1L;
	public static String name = "zingg.spark.core.executor.SparkLabeller";
	public static final Log LOG = LogFactory.getLog(SparkLabeller.class);

	public SparkLabeller() {
		this(new ZinggSparkContext());
	}

	@Override
	protected DFObjectUtil<SparkSession, Dataset<Row>, Row, Column> getDfObjectUtil() {
		IWithSession<SparkSession> iWithSession = new WithSession<SparkSession>();
		iWithSession.setSession(getContext().getSession());
		return new SparkDFObjectUtil(iWithSession);
	}

	public SparkLabeller(ZinggSparkContext sparkContext) {
		setZinggOption(ZinggOptions.LABEL);
		setContext(sparkContext);
	}

  @Override
  public void init(IArguments args, SparkSession s, ClientOptions options)  throws ZinggClientException {
    super.init(args,s,options);
    getContext().init(s);
  }
	
	
}
