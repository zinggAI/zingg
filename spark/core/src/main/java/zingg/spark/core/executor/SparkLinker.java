package zingg.spark.core.executor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataType;

import zingg.common.client.IArguments;
import zingg.common.client.ZinggClientException;
import zingg.common.client.options.ZinggOptions;
import zingg.common.core.executor.Linker;
import zingg.common.core.model.Model;
import zingg.common.core.preprocess.StopWordsRemover;
import zingg.spark.core.context.ZinggSparkContext;
import zingg.spark.core.preprocess.SparkStopWordsRemover;


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
    public void init(IArguments args, SparkSession s)  throws ZinggClientException {
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
