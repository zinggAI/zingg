package zingg.common.core.feature;

import zingg.common.client.FieldDefinition;
import zingg.common.client.MatchType;
import zingg.common.core.similarity.function.BooleanSimilarityFunction;
import zingg.common.core.similarity.function.CheckBlankOrNullFunction;
import zingg.common.core.similarity.function.CheckNullFunction;
import zingg.common.core.similarity.function.IntegerSimilarityFunction;
import zingg.common.core.similarity.function.SimilarityFunctionExact;
import zingg.common.core.similarity.function.StringSimilarityFunction;

public class BooleanFeature extends BaseFeature<Boolean> {

    private static final long serialVersionUID = 1L;

	public BooleanFeature() {
	}

    public void init(FieldDefinition f){
        setFieldDefinition(f);
        if (f.getMatchType().contains(MatchType.FUZZY)) {
			addSimFunction(new BooleanSimilarityFunction());
		} 
        if (f.getMatchType().contains(MatchType.EXACT)) {
			addSimFunction(new SimilarityFunctionExact<Boolean>("BooleanSimilarityFunctionExact"));
		} 
        if (f.getMatchType().contains(MatchType.NULL_OR_BLANK)) {
			addSimFunction(new CheckNullFunction<Boolean>("CheckNullFunctionBoolean"));
		}
    }
    
}
