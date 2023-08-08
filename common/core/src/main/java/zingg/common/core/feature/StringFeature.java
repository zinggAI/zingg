/*
 * Zingg
 * Copyright (C) 2021-Present  Zingg Labs,inc
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package zingg.common.core.feature;

import zingg.common.client.FieldDefinition;
import zingg.common.client.MatchType;
import zingg.common.core.similarity.function.AJaroWinklerFunction;
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
import zingg.common.core.similarity.function.SameFirstWordFunction;
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
		if(f.getMatchType().contains(MatchType.EMAIL)){
			addSimFunction(new EmailMatchTypeFunction());
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
