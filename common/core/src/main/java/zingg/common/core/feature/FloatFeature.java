package zingg.common.core.feature;

import zingg.common.client.FieldDefinition;
import zingg.common.client.MatchType;
import zingg.common.core.similarity.function.FloatSimilarityFunction;


public class FloatFeature extends BaseFeature<Float> {

	private static final long serialVersionUID = 1L;

	public FloatFeature() {

	}

	public void init(FieldDefinition newParam) {
		setFieldDefinition(newParam);
		if (newParam.getMatchType().contains(MatchType.FUZZY)) {
			addSimFunction(new FloatSimilarityFunction());
		}
	}

}
