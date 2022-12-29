package zingg.util;
import zingg.client.Arguments;
import zingg.client.FieldDefinition;
import zingg.client.MatchType;
import zingg.client.ZFrame;
import zingg.client.ZinggClientException;
import zingg.client.util.ColName;
import zingg.client.util.ColValues;
import zingg.feature.Feature;
import zingg.feature.FeatureFactory;
import zingg.model.Model;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public abstract class ModelUtil<S,T, D,R,C> {

    public static final Log LOG = LogFactory.getLog(ModelUtil.class);
    protected Map<FieldDefinition, Feature<T>> featurers;
    
    public abstract FeatureFactory<T> getFeatureFactory();

    public void loadFeatures(Arguments args) throws ZinggClientException {
		try{
		LOG.info("Start reading internal configurations and functions");
        if (args.getFieldDefinition() != null) {
			featurers = new HashMap<FieldDefinition, Feature<T>>();
			for (FieldDefinition def : args.getFieldDefinition()) {
				if (! (def.getMatchType() == null || def.getMatchType().contains(MatchType.DONT_USE))) {
					Feature fea =  (Feature) getFeatureFactory().get(def.getDataType());
					fea.init(def);
					featurers.put(def, fea);			
				}
			}
			LOG.info("Finished reading internal configurations and functions");
			}
		}
		catch(Throwable t) {
			LOG.warn("Unable to initialize internal configurations and functions");
            t.printStackTrace();
			if (LOG.isDebugEnabled()) t.printStackTrace();
			throw new ZinggClientException("Unable to initialize internal configurations and functions");
		}
	}

    public Map<FieldDefinition,Feature<T>> getFeaturers() {
        return this.featurers;
    }

    public void setFeaturers(Map<FieldDefinition,Feature<T>> featurers) {
        this.featurers = featurers;
    }

	public Model<S,T,D,R,C> createModel(ZFrame<D,R,C> positives,
        ZFrame<D,R,C> negatives, boolean isLabel, Arguments args) throws Exception, ZinggClientException {
        if (this.featurers == null) loadFeatures(args);
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
        //TODOmodel.register(spark);
        model.fit(posLabeledPointsWithLabel, negLabeledPointsWithLabel);
        return model;
    }

    public abstract Model<S,T,D,R,C> getModel(Map<FieldDefinition, Feature<T>> featurers, boolean isLabel);

    public abstract Model<S,T,D,R,C> loadModel(Map<FieldDefinition, Feature<T>> featurers, boolean isLabel, Arguments args);




    
}
