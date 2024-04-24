package zingg.common.core.feature;

import zingg.common.client.FieldDefinition;
import zingg.common.client.MatchType;
import zingg.common.core.similarity.function.CheckNullFunction;
import zingg.common.core.similarity.function.IntegerSimilarityFunction;
import zingg.common.core.similarity.function.SimilarityFunctionExact;
public class IntFeature extends BaseFeature<Integer> {

	private static final long serialVersionUID = 1L;

	public IntFeature() {

	}

	public void init(FieldDefinition newParam) {
		setFieldDefinition(newParam);
		if (newParam.getMatchType().contains(MatchType.FUZZY)) {
			addSimFunction(new IntegerSimilarityFunction());
		} 
		if (newParam.getMatchType().contains(MatchType.EXACT)) {
			addSimFunction(new SimilarityFunctionExact<Integer>("IntegerSimilarityFunctionExact"));
		} 		
		if (newParam.getMatchType().contains(MatchType.NULL_OR_BLANK)) {
			addSimFunction(new CheckNullFunction<Integer>("CheckNullFunctionInt"));	
		}
	}

}
