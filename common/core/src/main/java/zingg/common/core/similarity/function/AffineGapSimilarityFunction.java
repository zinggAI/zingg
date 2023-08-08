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

public class AffineGapSimilarityFunction extends StringSimilarityDistanceFunction {

	public static final Log LOG = LogFactory
			.getLog(AffineGapSimilarityFunction.class);
	
	
	public AffineGapSimilarityFunction() {
		this("AffineGapSimilarityFunction");		
	}
	
	public AffineGapSimilarityFunction(String s) {
		this.name = s;
		gap = new SAffineGap();
	}

	@Override
	public Double call(String first, String second) {
		return super.call(first,second);
	}
	
	
	
	
}
