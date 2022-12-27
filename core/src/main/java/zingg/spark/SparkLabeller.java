package zingg.spark;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

import org.apache.spark.sql.types.DataType;

import zingg.Labeller;

import zingg.client.ZinggClientException;
import zingg.client.ZinggOptions;


public class SparkLabeller extends Labeller<SparkSession, Dataset<Row>, Row, Column,DataType> {

	public static String name = "zingg.spark.SparkLabeller";
	public static final Log LOG = LogFactory.getLog(SparkLabeller.class);

	SparkSession sparkSession;
	

	public SparkLabeller() {
		setZinggOptions(ZinggOptions.LABEL);
	}

	

	@Override
	public void cleanup() throws ZinggClientException {
		// TODO Auto-generated method stub
		
	}


	

	
}
