package zingg.util;

import com.snowflake.snowpark_java.Functions;
import com.snowflake.snowpark_java.DataFrame;
import com.snowflake.snowpark_java.Row;
import org.apache.spark.storage.StorageLevel;

import zingg.client.ZinggClientException;
import zingg.client.util.ColName;
import zingg.client.util.ColValues;
import zingg.client.util.Util;
import zingg.model.Model;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.snowflake.snowpark_java.Session;

public class ModelUtil {

    public static final Log LOG = LogFactory.getLog(ModelUtil.class);

	public static Model createModel(DataFrame positives,
        DataFrame negatives, Model model, Session snow) throws Exception, ZinggClientException {
        LOG.info("Learning similarity rules");
        DataFrame posLabeledPointsWithLabel = positives.withColumn(ColName.MATCH_FLAG_COL, Functions.lit(ColValues.MATCH_TYPE_MATCH));
        posLabeledPointsWithLabel = posLabeledPointsWithLabel.persist(StorageLevel.MEMORY_ONLY());
        posLabeledPointsWithLabel = posLabeledPointsWithLabel.drop(ColName.PREDICTION_COL);
        DataFrame negLabeledPointsWithLabel = negatives.withColumn(ColName.MATCH_FLAG_COL, Functions.lit(ColValues.MATCH_TYPE_NOT_A_MATCH));
        negLabeledPointsWithLabel = negLabeledPointsWithLabel.persist(StorageLevel.MEMORY_ONLY());
        negLabeledPointsWithLabel = negLabeledPointsWithLabel.drop(ColName.PREDICTION_COL);
        if (LOG.isDebugEnabled()) {
            LOG.debug(" +,-,Total labeled data "
                    + posLabeledPointsWithLabel.count() + ", "
                    + negLabeledPointsWithLabel.count());
        }
        model.register(snow);
        model.fit(posLabeledPointsWithLabel, negLabeledPointsWithLabel);
        return model;
    }



    
}
