package zingg.spark.core.executor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataType;

import zingg.common.client.Arguments;
import zingg.common.client.ZinggClientException;
import zingg.common.client.ZinggOptions;
import zingg.common.core.executor.Labeller;

/**
 * Spark specific implementation of Labeller
 * 
 *
 */
public class SparkLabeller extends Labeller<SparkSession, Dataset<Row>, Row, Column,DataType> {

	public static String name = "zingg.spark.core.executor.SparkLabeller";
	public static final Log LOG = LogFactory.getLog(SparkLabeller.class);

	SparkSession sparkSession;
	

	public SparkLabeller() {
		setZinggOptions(ZinggOptions.LABEL);
		setContext(new ZinggSparkContext());
	}


  @Override
  public void init(Arguments args, String license)  throws ZinggClientException {
    super.init(args, license);
    getContext().init(license);
  }
	
	
}
