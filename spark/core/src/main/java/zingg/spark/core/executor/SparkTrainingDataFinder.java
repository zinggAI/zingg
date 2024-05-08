package zingg.spark.core.executor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.types.DataType;

import zingg.common.client.IArguments;
import zingg.common.client.IZArgs;
import zingg.common.client.ZinggClientException;
import zingg.common.client.options.ZinggOptions;
import zingg.spark.core.context.ZinggSparkContext;
import zingg.common.core.executor.TrainingDataFinder;
import zingg.common.core.preprocess.StopWordsRemover;
import org.apache.spark.sql.SparkSession;
import zingg.spark.core.preprocess.SparkStopWordsRemover;

public class SparkTrainingDataFinder extends TrainingDataFinder<SparkSession, Dataset<Row>, Row, Column,DataType> {

	private static final long serialVersionUID = 1L;
	public static String name = "zingg.spark.core.executor.SparkTrainingDataFinder";
	public static final Log LOG = LogFactory.getLog(SparkTrainingDataFinder.class);

	public SparkTrainingDataFinder() {
		this(new ZinggSparkContext());
	}

	public SparkTrainingDataFinder(ZinggSparkContext sparkContext) {
		super();
		setContext(sparkContext);
	}
	
	@Override
	public void init(IZArgs args, SparkSession s)  throws ZinggClientException {
		super.init(args,s);
		getContext().init(s);
	}
	
	@Override
	public StopWordsRemover<SparkSession, Dataset<Row>, Row, Column, DataType> getStopWords() {
		return new SparkStopWordsRemover(getContext(),getArgs());
	}
	
}
