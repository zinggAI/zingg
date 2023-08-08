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

package zingg;

import org.apache.spark.sql.api.java.UDF2;

import zingg.common.core.similarity.function.ArrayDoubleSimilarityFunction;

public class TestUDFDoubleArr implements UDF2<Double[],Double[], Double>{
	
	private static final long serialVersionUID = 1L;

	@Override
	public Double call(Double[] t1, Double[] t2) throws Exception {
		return ArrayDoubleSimilarityFunction.cosineSimilarity(t1, t2);
	}
	
}
