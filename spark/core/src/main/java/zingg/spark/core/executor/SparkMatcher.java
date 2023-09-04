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
import zingg.common.core.executor.Matcher;
import zingg.common.core.model.Model;
import zingg.common.core.preprocess.StopWordsRemover;
import zingg.spark.client.ZSparkSession;
import zingg.spark.core.preprocess.SparkStopWordsRemover;

/**
 * Spark specific implementation of Matcher
 * 
 *
 */
public class SparkMatcher extends Matcher<ZSparkSession,Dataset<Row>,Row,Column,DataType>{


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
    public void init(Arguments args, IZinggLicense license)  throws ZinggClientException {
        super.init(args, license);
        getContext().init(license);
    }
	

	@Override
	protected Model getModel() throws ZinggClientException {
		Model model = getModelUtil().loadModel(false, args);
		model.register(getContext().getSession());
		return model;
	}

}
