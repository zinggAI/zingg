package zingg.spark;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataType;

import zingg.Trainer;
import zingg.client.Arguments;
import zingg.client.ZinggClientException;
import zingg.client.ZinggOptions;
import zingg.preprocess.StopWords;
import zingg.spark.preprocess.SparkStopWords;


/**
 * Spark specific implementation of Trainer
 * 
 * @author vikasgupta
 *
 */
public class SparkTrainer extends Trainer<SparkSession, Dataset<Row>, Row, Column,DataType> {

	private static final long serialVersionUID = 1L;
	public static String name = "zingg.spark.SparkTrainer";
	public static final Log LOG = LogFactory.getLog(SparkTrainer.class);

	public SparkTrainer() {
		setZinggOptions(ZinggOptions.TRAIN);
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

	@Override
	protected StopWords<SparkSession, Dataset<Row>, Row, Column, DataType> getStopWords() {
		return new SparkStopWords(getContext(),getArgs());
	}
	
}
