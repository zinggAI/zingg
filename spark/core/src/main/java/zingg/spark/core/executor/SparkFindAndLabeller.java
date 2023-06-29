package zingg.spark.core.executor;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.types.DataType;

import zingg.common.client.Arguments;
import zingg.common.client.ZinggClientException;
import zingg.common.client.ZinggOptions;
import zingg.common.client.license.IZinggLicense;
import zingg.common.core.executor.FindAndLabeller;
import zingg.spark.client.ZSparkSession;

public class SparkFindAndLabeller extends FindAndLabeller<ZSparkSession, Dataset<Row>, Row, Column,DataType> {

	private static final long serialVersionUID = 1L;
	public static String name = "zingg.spark.core.executor.SparkFindAndLabeller";
	public static final Log LOG = LogFactory.getLog(SparkFindAndLabeller.class);

	public SparkFindAndLabeller() {
		setZinggOptions(ZinggOptions.FIND_AND_LABEL);		
		finder = new SparkTrainingDataFinder();
		setContext(new ZinggSparkContext());
	}	
	
	@Override
	public void init(Arguments args, IZinggLicense license) throws ZinggClientException {
		super.init(args, license);
		getContext().init(license);
	}	
	

}
