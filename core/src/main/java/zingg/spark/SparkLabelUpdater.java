package zingg.spark;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataType;

import zingg.LabelUpdater;
import zingg.client.ZinggClientException;
import zingg.client.ZinggOptions;



public class SparkLabelUpdater extends LabelUpdater<SparkSession, Dataset<Row>, Row, Column,DataType> {

	public static String name = "zingg.SparkLabelUpdater";
	public static final Log LOG = LogFactory.getLog(SparkLabelUpdater.class);

	public SparkLabelUpdater() {
		setZinggOptions(ZinggOptions.UPDATE_LABEL);
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
