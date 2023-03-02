package zingg.spark.core.executor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataType;

import zingg.TrainingDataFinder;
import zingg.common.client.Arguments;
import zingg.common.client.ZinggClientException;
import zingg.common.client.ZinggOptions;

public class SparkTrainingDataFinder extends TrainingDataFinder<SparkSession, Dataset<Row>, Row, Column,DataType> {

	public static String name = "zingg.spark.core.executor.SparkTrainingDataFinder";
	public static final Log LOG = LogFactory.getLog(SparkTrainingDataFinder.class);

	public SparkTrainingDataFinder() {
		setZinggOptions(ZinggOptions.FIND_TRAINING_DATA);
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
