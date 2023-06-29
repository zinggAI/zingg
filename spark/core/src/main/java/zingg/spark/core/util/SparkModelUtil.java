package zingg.spark.core.util;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.types.DataType;

import zingg.common.client.Arguments;
import zingg.common.client.ZinggClientException;
import zingg.common.core.feature.FeatureFactory;
import zingg.common.core.model.Model;
import zingg.common.core.util.ModelUtil;
import zingg.spark.client.ZSparkSession;
import zingg.spark.core.feature.SparkFeatureFactory;
import zingg.spark.core.model.SparkLabelModel;
import zingg.spark.core.model.SparkModel;


public class SparkModelUtil extends ModelUtil<ZSparkSession,DataType,Dataset<Row>, Row, Column> {

    public static final Log LOG = LogFactory.getLog(SparkModelUtil.class);
    

    public SparkModelUtil(ZSparkSession s) {
        this.session = s;
    }

	public Model<ZSparkSession,DataType,Dataset<Row>, Row, Column> getModel(boolean isLabel, Arguments args) throws ZinggClientException{
        Model<ZSparkSession,DataType,Dataset<Row>, Row, Column> model = null;
        if (isLabel) {
            model = new SparkLabelModel(getFeaturers(args));
        }
        else {
            model = new SparkModel(getFeaturers(args));            
        }
        return model;
    }

    @Override
    public Model<ZSparkSession,DataType,Dataset<Row>, Row, Column> loadModel(boolean isLabel,
        Arguments args) throws ZinggClientException    {
        Model<ZSparkSession,DataType,Dataset<Row>, Row, Column> model = getModel(isLabel, args);
        model.load(args.getModel());
        return model;

     }

    @Override
    public FeatureFactory<DataType> getFeatureFactory() {
        return new SparkFeatureFactory();
    }

}
