package zingg.spark;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataType;

import zingg.Trainer;
import zingg.client.ZinggClientException;
import zingg.client.ZinggOptions;



public class SparkTrainer extends Trainer<SparkSession, Dataset<Row>, Row, Column,DataType> {

	public static String name = "zingg.SparkTrainer";
	public static final Log LOG = LogFactory.getLog(SparkTrainer.class);

	public SparkTrainer() {
		setZinggOptions(ZinggOptions.TRAIN);
	}

	
	@Override
	public void cleanup() throws ZinggClientException {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void setSession(SparkSession session) {
		// TODO Auto-generated method stub
		
	}

		
	
}
