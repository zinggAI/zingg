package zingg.feature;

import zingg.client.FieldDefinition;
import zingg.client.MatchType;
import zingg.similarity.function.IntegerSimilarityFunction;
public class IntFeature extends BaseFeature<Integer> {

	public IntFeature() {

	}

	public void init(FieldDefinition newParam) {
		setFieldDefinition(newParam);
		if (newParam.getMatchType() == MatchType.NUMERIC) {
			addSimFunction(new IntegerSimilarityFunction());
		} /*else {
			addSimFunction(new ObjectSimilarityFunction());
		}*/
	}

}
