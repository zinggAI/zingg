package zingg.common.core.feature;

import zingg.common.client.FieldDefinition;
import zingg.common.client.MatchTypes;
import zingg.common.core.similarity.function.AffineGapSimilarityFunction;
import zingg.common.core.similarity.function.CheckBlankOrNullFunction;
import zingg.common.core.similarity.function.EmailMatchTypeFunction;
import zingg.common.core.similarity.function.JaccSimFunction;
import zingg.common.core.similarity.function.JaroWinklerFunction;
import zingg.common.core.similarity.function.NumbersJaccardFunction;
import zingg.common.core.similarity.function.OnlyAlphabetsAffineGapSimilarity;
import zingg.common.core.similarity.function.OnlyAlphabetsExactSimilarity;
import zingg.common.core.similarity.function.PinCodeMatchTypeFunction;
import zingg.common.core.similarity.function.ProductCodeFunction;
import zingg.common.core.similarity.function.StringSimilarityFunction;


public class StringFeature extends BaseFeature<String> {

	public StringFeature() {

	}

	public void init(FieldDefinition f) {
		setFieldDefinition(f);
		// based on stat, evaluate which function(s) to use
		// if long string, cosine
		// if short string, affine gap
		// if short string but inverted, like fname lname where ordering is not
		// important
		// then do cosine or something
		if (f.getMatchType().contains(MatchTypes.FUZZY)) {
			addSimFunction(new AffineGapSimilarityFunction());
			addSimFunction(new JaroWinklerFunction());
		} 		
		if (f.getMatchType().contains(MatchTypes.TEXT)) {
			addSimFunction(new JaccSimFunction());			
		} 
		if (f.getMatchType().contains(MatchTypes.NUMERIC)) {
			addSimFunction(new NumbersJaccardFunction());
		}
		if (f.getMatchType().contains(MatchTypes.EXACT)) {
			addSimFunction(new StringSimilarityFunction());
		} 
		if(f.getMatchType().contains(MatchTypes.PINCODE)){
			addSimFunction(new PinCodeMatchTypeFunction());
		}
		if(f.getMatchType().contains(MatchTypes.EMAIL)){
			addSimFunction(new EmailMatchTypeFunction());
		}
		if (f.getMatchType().contains(MatchTypes.NUMERIC_WITH_UNITS)) {
			addSimFunction(new ProductCodeFunction());
		}
		if (f.getMatchType().contains(MatchTypes.NULL_OR_BLANK)) {
			addSimFunction(new CheckBlankOrNullFunction());
		}
		if (f.getMatchType().contains(MatchTypes.ONLY_ALPHABETS_FUZZY)) {
			addSimFunction(new OnlyAlphabetsAffineGapSimilarity());
		}
		if (f.getMatchType().contains(MatchTypes.ONLY_ALPHABETS_EXACT)) {
			addSimFunction(new OnlyAlphabetsExactSimilarity());
		}
	}

}
