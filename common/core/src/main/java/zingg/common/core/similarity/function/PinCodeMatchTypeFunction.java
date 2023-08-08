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



public class PinCodeMatchTypeFunction extends StringSimilarityFunction {

	public static final Log LOG = LogFactory
			.getLog(PinCodeMatchTypeFunction.class);
	
	
	public PinCodeMatchTypeFunction() {
		this("PinCodeMatchTypeFunction");		
	}
	
	public PinCodeMatchTypeFunction(String s) {
		super(s);
	}
	
	@Override
	public Double call(String first, String second) {
		if (first == null || first.trim().length() ==0) return 1d;
		if (second == null || second.trim().length() ==0) return 1d;
		first = first.split("-")[0];
		second = second.split("-")[0];
		double score = first.trim().equalsIgnoreCase(second.trim()) ? 1d : 0d;
		return score;		
	}	
}