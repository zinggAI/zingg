package zingg.feature;

import zingg.client.FieldDefinition;
import zingg.client.MatchType;
import zingg.similarity.function.AJaroWinklerFunction;
import zingg.similarity.function.AffineGapSimilarityFunction;
import zingg.similarity.function.CheckBlankOrNullFunction;
import zingg.similarity.function.JaccSimFunction;
import zingg.similarity.function.JaroWinklerFunction;
import zingg.similarity.function.NumbersJaccardFunction;
import zingg.similarity.function.OnlyAlphabetsAffineGapSimilarity;
import zingg.similarity.function.OnlyAlphabetsExactSimilarity;
import zingg.similarity.function.ProductCodeFunction;
import zingg.similarity.function.SameFirstWordFunction;
import zingg.similarity.function.StringSimilarityFunction;

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
		if (f.getMatchType().contains(MatchType.FUZZY)) {
			addSimFunction(new AffineGapSimilarityFunction());
			addSimFunction(new JaroWinklerFunction());
		} 		
		if (f.getMatchType().contains(MatchType.TEXT)) {
			addSimFunction(new JaccSimFunction());			
		} 
		if (f.getMatchType().contains(MatchType.NUMERIC)) {
			addSimFunction(new NumbersJaccardFunction());
		}
		if (f.getMatchType().contains(MatchType.EXACT)) {
			addSimFunction(new StringSimilarityFunction());
		} 
		if(f.getMatchType().contains(MatchType.PINCODE)){
			addSimFunction(new PinCodeMatchTypeFunction());
		}
		if (f.getMatchType().contains(MatchType.NUMERIC_WITH_UNITS)) {
			addSimFunction(new ProductCodeFunction());
		}
		if (f.getMatchType().contains(MatchType.NULL_OR_BLANK)) {
			addSimFunction(new CheckBlankOrNullFunction());
		}
		if (f.getMatchType().contains(MatchType.ONLY_ALPHABETS_FUZZY)) {
			addSimFunction(new OnlyAlphabetsAffineGapSimilarity());
		}
		if (f.getMatchType().contains(MatchType.ONLY_ALPHABETS_EXACT)) {
			addSimFunction(new OnlyAlphabetsExactSimilarity());
		}
	}

}
