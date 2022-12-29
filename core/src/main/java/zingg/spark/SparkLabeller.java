package zingg.spark;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataType;

import zingg.Labeller;
import zingg.client.Arguments;
import zingg.client.ZinggClientException;
import zingg.client.ZinggOptions;

/**
 * Spark specific implementation of Labeller
 * 
 * @author vikasgupta
 *
 */
public class SparkLabeller extends Labeller<SparkSession, Dataset<Row>, Row, Column,DataType> {

	public static String name = "zingg.spark.SparkLabeller";
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
        
	@Override
	public void cleanup() throws ZinggClientException {
		// TODO Auto-generated method stub
		
	}


	

	
}
