package zingg.util;
import zingg.client.Arguments;
import zingg.client.FieldDefinition;
import zingg.client.ZFrame;
import zingg.client.ZinggClientException;
import zingg.client.util.ColName;
import zingg.client.util.ColValues;
import zingg.feature.Feature;
import zingg.model.Model;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public abstract class ModelUtil<S,T, D,R,C> {

    public static final Log LOG = LogFactory.getLog(ModelUtil.class);

	public Model<S,T,D,R,C> createModel(ZFrame<D,R,C> positives,
        ZFrame<D,R,C> negatives, Map<FieldDefinition, Feature<T>> featurers, S spark, boolean isLabel) throws Exception, ZinggClientException {
        LOG.info("Learning similarity rules");
        ZFrame<D,R,C> posLabeledPointsWithLabel = positives.withColumn(ColName.MATCH_FLAG_COL, ColValues.MATCH_TYPE_MATCH);
        posLabeledPointsWithLabel = posLabeledPointsWithLabel.cache();
        posLabeledPointsWithLabel = posLabeledPointsWithLabel.drop(ColName.PREDICTION_COL);
        ZFrame<D,R,C> negLabeledPointsWithLabel = negatives.withColumn(ColName.MATCH_FLAG_COL, ColValues.MATCH_TYPE_NOT_A_MATCH);
        negLabeledPointsWithLabel = negLabeledPointsWithLabel.cache();
        negLabeledPointsWithLabel = negLabeledPointsWithLabel.drop(ColName.PREDICTION_COL);
        if (LOG.isDebugEnabled()) {
            LOG.debug(" +,-,Total labeled data "
                    + posLabeledPointsWithLabel.count() + ", "
                    + negLabeledPointsWithLabel.count());
        }
        Model<S,T, D,R,C> model = getModel(featurers, isLabel);
        model.register(spark);
        model.fit(posLabeledPointsWithLabel, negLabeledPointsWithLabel);
        return model;
    }

    public abstract Model<S,T,D,R,C> getModel(Map<FieldDefinition, Feature<T>> featurers, boolean isLabel);

    public abstract Model<S,T,D,R,C> loadModel(Map<FieldDefinition, Feature<T>> featurers, boolean isLabel, Arguments args);




    
}
