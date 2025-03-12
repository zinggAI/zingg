package zingg.common.core.feature;

import java.util.Date;

import zingg.common.client.FieldDefinition;
import zingg.common.client.MatchTypes;
import zingg.common.core.similarity.function.CheckNullFunction;
import zingg.common.core.similarity.function.DateSimilarityFunction;
import zingg.common.core.similarity.function.SimilarityFunctionExact;

public class DateFeature extends BaseFeature<Date> {

	private static final long serialVersionUID = 1L;

	public DateFeature() {

	}

	public void init(FieldDefinition f) {
		setFieldDefinition(f);
		// based on stat, evaluate which function(s) to use
		// if long string, cosine
		// if short string, affine gap
		// if short string but inverted, like fname lname where ordering is not
		// important
		// then do cosine or something
		/*if (f == FieldType.WORD) {
			addSimFunction(new AffineGapSimilarityFunction());
			addSimFunction(new JaroWinklerFunction());			
		}
		else*/ 
		if (f.getMatchType().contains(MatchTypes.FUZZY)) {
			addSimFunction(new DateSimilarityFunction());
		} 
		if (f.getMatchType().contains(MatchTypes.EXACT)) {
			addSimFunction(new SimilarityFunctionExact<Date>("DateSimilarityFunctionExact"));
		} 				
		if (f.getMatchType().contains(MatchTypes.NULL_OR_BLANK)) {
			addSimFunction(new CheckNullFunction<Date>("CheckNullFunctionDate"));	
		}				
	}

}
