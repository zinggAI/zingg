package zingg.common.core.feature;

import zingg.common.client.FieldDefinition;
import zingg.common.client.MatchTypes;
import zingg.common.core.similarity.function.CheckNullFunction;
import zingg.common.core.similarity.function.SimilarityFunctionExact;

public class BooleanFeature extends BaseFeature<Boolean> {

    private static final long serialVersionUID = 1L;

	public BooleanFeature() {
	}

    public void init(FieldDefinition f){
        setFieldDefinition(f);
        if (f.getMatchType().contains(MatchTypes.EXACT)) {
			addSimFunction(new SimilarityFunctionExact<Boolean>("BooleanSimilarityFunctionExact"));
		} 
        if (f.getMatchType().contains(MatchTypes.NULL_OR_BLANK)) {
			addSimFunction(new CheckNullFunction<Boolean>("CheckNullFunctionBoolean"));
		}
    }
    
}
