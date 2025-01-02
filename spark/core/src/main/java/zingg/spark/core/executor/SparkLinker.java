package zingg.spark.core.executor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataType;

import zingg.common.client.ClientOptions;
import zingg.common.client.IZArgs;
import zingg.common.client.ZinggClientException;
import zingg.common.client.options.ZinggOptions;
import zingg.common.core.executor.Linker;
import zingg.common.core.model.Model;
import zingg.common.core.preprocess.stopwords.StopWordsRemover;
import zingg.spark.core.context.ZinggSparkContext;
import zingg.spark.core.preprocess.stopwords.SparkStopWordsRemover;


public class SparkLinker extends Linker<SparkSession, Dataset<Row>, Row, Column,DataType> {

	private static final long serialVersionUID = 1L;
	public static String name = "zingg.spark.core.executor.SparkLinker";
	public static final Log LOG = LogFactory.getLog(SparkLinker.class);

	public SparkLinker() {
		this(new ZinggSparkContext());
	}

	public SparkLinker(ZinggSparkContext sparkContext) {
		setZinggOption(ZinggOptions.LINK);
		setContext(sparkContext);
	}
	
    @Override
    public void init(IZArgs args, SparkSession s, ClientOptions options)  throws ZinggClientException {
        super.init(args,s,options);
        getContext().init(s);
    }
	
	@Override
	public Model getModel() throws ZinggClientException {
		Model model = getModelUtil().loadModel(false, args, getModelHelper());
		model.register();
		return model;
	}

	@Override
	public StopWordsRemover<SparkSession, Dataset<Row>, Row, Column, DataType> getStopWords() {
		return new SparkStopWordsRemover(getContext(),getArgs());
	}
	
}
