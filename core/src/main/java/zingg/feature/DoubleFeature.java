package zingg.feature;

import zingg.client.FieldDefinition;
import zingg.client.MatchType;
import zingg.similarity.function.DoubleSimilarityFunction;


public class DoubleFeature extends BaseFeature<Double> {

	public DoubleFeature() {

	}

	public void init(FieldDefinition newParam) {
		setFieldDefinition(newParam);
		if (newParam.getMatchType().contains(MatchType.NUMERIC)) {
			addSimFunction(new DoubleSimilarityFunction());
		}
	}

}
