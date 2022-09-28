package zingg.snowpark.util;

import zingg.client.Arguments;
import zingg.client.FieldDefinition;
import zingg.client.ZFrame;
import zingg.client.ZinggClientException;
import zingg.client.util.ColName;
import zingg.client.util.ColValues;
import zingg.feature.Feature;
import zingg.model.Model;
import zingg.spark.model.SparkLabelModel;
import zingg.spark.model.SparkModel;
import zingg.util.ModelUtil;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.snowflake.snowpark_java.Column;
import com.snowflake.snowpark_java.DataFrame;
import com.snowflake.snowpark_java.Row;
import com.snowflake.snowpark_java.Session;


public class SnowModelUtil extends ModelUtil<Session,DataFrame, Row, Column> {

    public static final Log LOG = LogFactory.getLog(SnowModelUtil.class);

	public Model<Session,DataFrame, Row, Column> getModel(Map<FieldDefinition, Feature> featurers, boolean isLabel){
        Model<Session,DataFrame, Row, Column> model = null;
        if (isLabel) {
            model = new SnowLabelModel(featurers);
        }
        else {
            model = new SnowModel(featurers);            
        }
        return model;
    }

    public Model<Session,DataFrame, Row, Column> loadModel(Map<FieldDefinition, Feature> featurers, boolean isLabel,
        Arguments args)    {
        Model<Session,DataFrame, Row, Column> model = getModel(featurers, isLabel);
        model.load(args.getModel());
        return model;

     }

}
