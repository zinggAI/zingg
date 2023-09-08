package zingg.spark.core.executor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.types.DataType;
import org.apache.spark.sql.SparkSession;

import zingg.common.client.Arguments;
import zingg.common.client.ZinggClientException;
import zingg.common.client.options.ZinggOptions;

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

	public SparkLabeller(ZinggSparkContext sparkContext) {
		setZinggOption(ZinggOptions.LABEL);
		setContext(sparkContext);
	}

  @Override
  public void init(Arguments args)  throws ZinggClientException {
    super.init(args);
    getContext().init();
  }
	
	
}
