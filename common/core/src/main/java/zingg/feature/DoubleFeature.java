package zingg.feature;

import zingg.common.client.FieldDefinition;
import zingg.common.client.MatchType;
import zingg.similarity.function.DoubleSimilarityFunction;


public class DoubleFeature extends BaseFeature<Double> {

	public DoubleFeature() {

	}

	public void init(FieldDefinition newParam) {
		setFieldDefinition(newParam);
		if (newParam.getMatchType().contains(MatchType.FUZZY)) {
			addSimFunction(new DoubleSimilarityFunction());
		}
	}

}
