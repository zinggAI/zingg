package zingg.spark.core.executor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataType;

import zingg.common.client.Arguments;
import zingg.common.client.ZinggClientException;
import zingg.common.client.ZinggOptions;
import zingg.common.core.executor.TrainMatcher;
import zingg.common.core.model.Model;
import zingg.spark.core.model.SparkModel;

/**
 * Spark specific implementation of TrainMatcher
 * 
 * @author vikasgupta
 *
 */
public class SparkTrainMatcher extends TrainMatcher<SparkSession, Dataset<Row>, Row, Column,DataType> {

	public static String name = "zingg.spark.core.executor.SparkTrainMatcher";
	public static final Log LOG = LogFactory.getLog(SparkTrainMatcher.class);

	public SparkTrainMatcher() {
		setZinggOptions(ZinggOptions.TRAIN_MATCH);
		setContext(new ZinggSparkContext());
		trainer = new SparkTrainer();
	}

    @Override
    public void init(Arguments args, String license)  throws ZinggClientException {
        super.init(args, license);
        getContext().init(license);
    }
        
	
	@Override
	protected Model getModel() throws ZinggClientException {
		Model model = getModelUtil().loadModel(false, args);
		model.register(getContext().getSession());
		return model;
	}


	@Override
	public void cleanup() throws ZinggClientException {
		// TODO Auto-generated method stub
		
	}


	
	

		
	
}
