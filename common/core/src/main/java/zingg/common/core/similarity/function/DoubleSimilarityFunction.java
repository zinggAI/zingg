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

public class DoubleSimilarityFunction extends SimFunction<Double> {
	public static final Log LOG = LogFactory
			.getLog(DoubleSimilarityFunction.class);

	public DoubleSimilarityFunction() {
		super("DoubleSimilarityFunction");
		// TODO Auto-generated constructor stub
	}

	@Override
	public Double call(Double first, Double second) {
		if (first == null || first.isNaN()) return 1d;
		if (second == null || second.isNaN()) return 1d;
		//we want similarity, hence we subtract from 1 so that closer values have higher score
		double score = 1 - (Math.abs(first-second))/(1.0+first + second);
		LOG.debug(" DoubleSim bw " + first + " and second " + second + " is "
		 + score);
		return score;
	}

}
