package zingg.spark;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataType;

import zingg.TrainingDataFinder;
import zingg.client.ZinggClientException;
import zingg.client.ZinggOptions;

public class SparkTrainingDataFinder extends TrainingDataFinder<SparkSession, Dataset<Row>, Row, Column,DataType> {

	public static String name = "zingg.SparkTrainingDataFinder";
	public static final Log LOG = LogFactory.getLog(SparkTrainingDataFinder.class);

	public SparkTrainingDataFinder() {
		setZinggOptions(ZinggOptions.FIND_TRAINING_DATA);
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
