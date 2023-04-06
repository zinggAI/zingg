package zingg.common.core.feature;

import zingg.common.client.FieldDefinition;
import zingg.common.client.MatchType;
import zingg.common.core.similarity.function.ArrayDoubleSimilarityFunction;
public class ArrayDoubleFeature extends BaseFeature<Double[]> {

	private static final long serialVersionUID = 1L;

	public ArrayDoubleFeature() {

	}

	public void init(FieldDefinition newParam) {
		setFieldDefinition(newParam);
		if (newParam.getMatchType().contains(MatchType.FUZZY)) {
			addSimFunction(new ArrayDoubleSimilarityFunction());
		} 
	}

}
