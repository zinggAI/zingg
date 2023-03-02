package zingg.feature;

import zingg.common.client.FieldDefinition;
import zingg.common.client.MatchType;
import zingg.similarity.function.IntegerSimilarityFunction;
public class IntFeature extends BaseFeature<Integer> {

	public IntFeature() {

	}

	public void init(FieldDefinition newParam) {
		setFieldDefinition(newParam);
		if (newParam.getMatchType().contains(MatchType.FUZZY)) {
			addSimFunction(new IntegerSimilarityFunction());
		} 
	}

}
