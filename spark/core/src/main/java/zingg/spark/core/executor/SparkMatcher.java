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
        setZinggOption(ZinggOptions.MATCH);
		setContext(sparkContext);
    }

    @Override
    public void init(IZArgs args, SparkSession s)  throws ZinggClientException {
        super.init(args,s);
        getContext().init(s);
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
