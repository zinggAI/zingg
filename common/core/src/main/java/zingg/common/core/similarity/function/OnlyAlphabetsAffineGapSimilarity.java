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

package zingg.common.core.similarity.function;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.wcohen.ss.api.*;

public class OnlyAlphabetsAffineGapSimilarity extends StringSimilarityDistanceFunction {

	public static final Log LOG = LogFactory
			.getLog(OnlyAlphabetsAffineGapSimilarity.class);
	
	
	public OnlyAlphabetsAffineGapSimilarity() {
		this("OnlyAlphabetsAffineGapSimilarity");		
	}
	
	public OnlyAlphabetsAffineGapSimilarity(String s) {
		super(s);
		gap = new SAffineGap();
	}
	
	

	@Override
	public Double call(String first, String second) {
		double score1 = 0.0;
		double score2 = 0.0;
		double score = 0.0;

		try {
			if (first == null || first.equals("")) {
				score1 = 1.0d;
			}
			if (second == null || second.equals("")) {
				score2 = 1.0d;
			}
			if (score1 != 1.0d && score2 != 1.0d) {
				first = first.replaceAll("[0-9.]", "");
				second = second.replaceAll("[0-9.]", "");
				score = super.call(first, second);
			}
			else {
				score = 1.0d;
			}
			
			
		}  catch (Exception e) {
			e.printStackTrace();
			LOG.warn("Error processing differences for " + first + "," + second);
		} finally {
			if (Double.isNaN(score)) {
				score = 0.0;
			}
			return score;
		}
	}
	
}
