package zingg.common.core.feature;

import zingg.common.client.FieldDefinition;
import zingg.common.client.MatchType;
import zingg.common.core.similarity.function.CheckNullFunction;
import zingg.common.core.similarity.function.LongSimilarityFunction;
import zingg.common.core.similarity.function.SimilarityFunctionExact;
public class LongFeature extends BaseFeature<Long> {

	private static final long serialVersionUID = 1L;

	public LongFeature() {

	}

	public void init(FieldDefinition newParam) {
		setFieldDefinition(newParam);
		if (newParam.getMatchType().contains(MatchType.FUZZY)) {
			addSimFunction(new LongSimilarityFunction());
		} 
		if (newParam.getMatchType().contains(MatchType.EXACT)) {
			addSimFunction(new SimilarityFunctionExact<Long>("LongSimilarityFunctionExact"));
		} 				
		if (newParam.getMatchType().contains(MatchType.NULL_OR_BLANK)) {
			addSimFunction(new CheckNullFunction<Long>("CheckNullFunctionLong"));	
		}		
	}

}
