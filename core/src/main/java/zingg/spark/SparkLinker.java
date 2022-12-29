package zingg.spark;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataType;

import zingg.Linker;
import zingg.client.Arguments;
import zingg.client.ZinggClientException;
import zingg.client.ZinggOptions;
import zingg.model.Model;
import zingg.spark.model.SparkModel;


public class SparkLinker extends Linker<SparkSession, Dataset<Row>, Row, Column,DataType> {

	public static String name = "zingg.spark.SparkLinker";
	public static final Log LOG = LogFactory.getLog(SparkLinker.class);

	public SparkLinker() {
		setZinggOptions(ZinggOptions.LINK);
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
	protected Model getModel() {
		Model model = new SparkModel(getModelUtil().getFeaturers());
		model.register(getContext());
		model.load(args.getModel());
		return model;
	}
	
}
