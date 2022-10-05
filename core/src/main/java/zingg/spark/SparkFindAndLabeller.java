package zingg.spark;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

import org.apache.spark.sql.types.DataType;

import zingg.FindAndLabeller;
import zingg.client.ZinggClientException;
import zingg.client.ZinggOptions;



public class SparkFindAndLabeller extends FindAndLabeller<SparkSession, Dataset<Row>, Row, Column,DataType> {

	public static String name = "zingg.SparkFindAndLabeller";
	public static final Log LOG = LogFactory.getLog(SparkFindAndLabeller.class);
	SparkSession sparkSession;

	@Override
	public void setSession(SparkSession session) {
		this.sparkSession = session;
		
	}

	public SparkFindAndLabeller() {
		setZinggOptions(ZinggOptions.FIND_AND_LABEL);
	}

	
	@Override
	public void cleanup() throws ZinggClientException {
		// TODO Auto-generated method stub
		
	}
	
}
