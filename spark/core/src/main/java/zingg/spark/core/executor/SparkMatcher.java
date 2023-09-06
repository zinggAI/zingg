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

import zingg.common.core.executor.Matcher;
import zingg.common.core.model.Model;
import zingg.common.core.preprocess.StopWordsRemover;
import org.apache.spark.sql.SparkSession;
import zingg.spark.core.preprocess.SparkStopWordsRemover;

/**
 * Spark specific implementation of Matcher
 * 
 *
 */
public class SparkMatcher extends Matcher<SparkSession,Dataset<Row>,Row,Column,DataType>{


	private static final long serialVersionUID = 1L;
	public static String name = "zingg.spark.core.executor.SparkMatcher";
	public static final Log LOG = LogFactory.getLog(SparkMatcher.class);    

    public SparkMatcher() {
        this(new ZinggSparkContext());
    }

    public SparkMatcher(ZinggSparkContext sparkContext) {
        setZinggOptions(ZinggOptions.MATCH);
		setContext(sparkContext);
    }

    @Override
    public void init(Arguments args)  throws ZinggClientException {
        super.init(args);
       // getContext().init(license);
    }
	

	@Override
	public Model getModel() throws ZinggClientException {
		Model model = getModelUtil().loadModel(false, args);
		model.register();
		return model;
	}

	@Override
	public StopWordsRemover<SparkSession, Dataset<Row>, Row, Column, DataType> getStopWords() {
		return new SparkStopWordsRemover(getContext(),getArgs());
	}
	
}
