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

public class IntegerSimilarityFunction extends SimFunction<Integer> {
	public static final Log LOG = LogFactory
			.getLog(IntegerSimilarityFunction.class);

	public IntegerSimilarityFunction() {
		super("IntegerSimilarityFunction");
		// TODO Auto-generated constructor stub
	}

	@Override
	public Double call(Integer first, Integer second) {
		double score = 0;
		if (first != null && second != null) {
			if (first+second != 0) score = 2.0*Math.abs(first - second)/(first + second);
			LOG.debug(" IntegerSim bw " + first + " and second " + second + " is "
						+ score);
		}
		return score;		
	}
}
