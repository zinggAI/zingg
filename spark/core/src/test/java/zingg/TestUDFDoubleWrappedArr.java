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

import scala.collection.mutable.WrappedArray;
import zingg.common.core.similarity.function.ArrayDoubleSimilarityFunction;

public class TestUDFDoubleWrappedArr implements UDF2<WrappedArray<Double>,WrappedArray<Double>, Double>{
	
	private static final long serialVersionUID = 1L;

	@Override
	public Double call(WrappedArray<Double> t1, WrappedArray<Double> t2) throws Exception {
		System.out.println("TestUDFDoubleWrappedArr class" +t1.getClass());
		
		Double[] t1Arr = new Double[t1.length()];
		if (t1!=null) {
			t1.copyToArray(t1Arr);
		}
		Double[] t2Arr = new Double[t2.length()];
		if (t2!=null) {
			t2.copyToArray(t2Arr);
		}
		return ArrayDoubleSimilarityFunction.cosineSimilarity(t1Arr, t2Arr);
	}
	
}
