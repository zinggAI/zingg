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

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DateSimilarityFunction extends SimFunction<Date> {
	
	public static final Log LOG = LogFactory.getLog(DateSimilarityFunction.class);


	public DateSimilarityFunction() {
		super("DateSimilarityFunction");
	}

	@Override
	public Double call(Date first, Date second) {
		if (first == null || second == null) return 1d;
		long timeDiffInMillis = first.getTime() - second.getTime();
		//added 1 to avoid 0 division
		long timeAddinMillis = 1+first.getTime() + second.getTime();
		//we want similarity, hence we subtract from 1 so that closer values have higher score
		// similar to DoubleSimilarityFunction
		double diff = 1-Math.abs(timeDiffInMillis/(1.0*timeAddinMillis));
		return diff;
	}	

}


