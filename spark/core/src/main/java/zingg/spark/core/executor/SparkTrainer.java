package zingg.spark.core.executor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.types.DataType;
import org.apache.spark.sql.SparkSession;

import zingg.common.client.Arguments;
import zingg.common.client.ZinggClientException;
import zingg.common.client.ZinggOptions;
import zingg.spark.core.context.ZinggSparkContext;
import zingg.common.core.executor.Trainer;
import zingg.common.core.preprocess.StopWordsRemover;

import zingg.spark.core.preprocess.SparkStopWordsRemover;


public class SparkTrainer extends Trainer<SparkSession, Dataset<Row>, Row, Column,DataType> {

	public static String name = "zingg.spark.core.executor.SparkTrainer";
	private static final long serialVersionUID = 1L;
	public static final Log LOG = LogFactory.getLog(SparkTrainer.class);

	public SparkTrainer() {
		this(new ZinggSparkContext());
	}

	public SparkTrainer(ZinggSparkContext sparkContext) {
		setZinggOptions(ZinggOptions.TRAIN);
		setContext(sparkContext);
	}
	
    @Override
    public void init(Arguments args)  throws ZinggClientException {
        super.init(args);
        //getContext().init(license);
    }	
	
	@Override
	protected StopWordsRemover<SparkSession, Dataset<Row>, Row, Column, DataType> getStopWords() {
		return new SparkStopWordsRemover(getContext(),getArgs());
	}
	
}
