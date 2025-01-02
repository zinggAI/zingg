package zingg.spark.core.util;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.types.DataType;

import zingg.common.client.IArguments;
import zingg.common.client.ZinggClientException;
import zingg.common.client.util.IModelHelper;
import zingg.common.core.feature.FeatureFactory;
import zingg.common.core.model.Model;
import zingg.common.core.util.ModelUtil;
import org.apache.spark.sql.SparkSession;
import zingg.spark.core.feature.SparkFeatureFactory;
import zingg.spark.core.model.SparkLabelModel;
import zingg.spark.core.model.SparkModel;


public class SparkModelUtil extends ModelUtil<SparkSession,DataType,Dataset<Row>, Row, Column> {

    public static final Log LOG = LogFactory.getLog(SparkModelUtil.class);
    

    public SparkModelUtil(SparkSession s) {
        super(s);
    }

	public Model<SparkSession,DataType,Dataset<Row>, Row, Column> getModel(boolean isLabel, IArguments args) throws ZinggClientException{
        Model<SparkSession,DataType,Dataset<Row>, Row, Column> model = null;
        if (isLabel) {
            model = new SparkLabelModel(session, getFeaturers(args));
        }
        else {
            model = new SparkModel(session, getFeaturers(args));            
        }
        return model;
    }

    @Override
    public Model<SparkSession,DataType,Dataset<Row>, Row, Column> loadModel(boolean isLabel,
        IArguments args, IModelHelper mh) throws ZinggClientException    {
        Model<SparkSession,DataType,Dataset<Row>, Row, Column> model = getModel(isLabel, args);
        model.load(mh.getModel(args));
        return model;
     }

    @Override
    public FeatureFactory<DataType> getFeatureFactory() {
        return new SparkFeatureFactory();
    }

}
