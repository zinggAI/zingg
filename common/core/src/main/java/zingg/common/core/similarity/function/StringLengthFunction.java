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

import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class StringLengthFunction {/*extends StringSimilarityFunction {

	public static final Log LOG = LogFactory
			.getLog(StringLengthFunction.class);
	
	
	public StringLengthFunction() {
		super("StringLengthFunction");
		//gap = new SAffineGap();
	}

	public void operate(SimFunctionContext<String> context) {
		String first = context.getFirstOperand();
		String second = context.getSecondOperand();
		double score1 = 0.0;
		double score2 = 0.0;
		double score = 0.0;
		try {
			if (!(first == null || first.trim().equals(""))) {
				score1 = first.length();
			}
			if (!(second == null || second.trim().equals(""))) {
				score2 = second.length();
			}
			score = score1 * score2;
			
		} catch (Exception e) {
			LOG.warn("Error processing differences for " + first + "," + second);
		} finally {
			context.addToResult(score);
			// LOG.debug("Affine gap bw " + first + " and " + second + " is " +
			// score1 + "," + score2 + ", " + score);
			// LOG.debug(gap.explainScore(first, second));
		}
	}

	public void prepare() {

	}

	public void cleanUp() {
		// TODO Auto-generated method stub

	}

	@Override
	public int getNumFeatures() {
		return 1;
	}

	@Override
	public void setNorm() {
		this.norm.add(true);
	}
	*/

}
