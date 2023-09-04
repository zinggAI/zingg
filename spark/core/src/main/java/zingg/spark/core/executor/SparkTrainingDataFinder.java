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
import zingg.common.core.executor.TrainingDataFinder;
import zingg.common.core.preprocess.StopWordsRemover;
import zingg.spark.client.ZSparkSession;
import zingg.spark.core.preprocess.SparkStopWordsRemover;

public class SparkTrainingDataFinder extends TrainingDataFinder<ZSparkSession, Dataset<Row>, Row, Column,DataType> {

	private static final long serialVersionUID = 1L;
	public static String name = "zingg.spark.core.executor.SparkTrainingDataFinder";
	public static final Log LOG = LogFactory.getLog(SparkTrainingDataFinder.class);

	public SparkTrainingDataFinder() {
		this(new ZinggSparkContext());
	}

	public SparkTrainingDataFinder(ZinggSparkContext sparkContext) {
		setZinggOptions(ZinggOptions.FIND_TRAINING_DATA);
		setContext(sparkContext);
	}
	
	@Override
	public void init(Arguments args, IZinggLicense license)  throws ZinggClientException {
		super.init(args, license);
		getContext().init(license);
	}
	
}
