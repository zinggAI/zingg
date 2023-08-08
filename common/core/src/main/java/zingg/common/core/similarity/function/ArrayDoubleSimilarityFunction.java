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

import scala.collection.mutable.WrappedArray;

public class ArrayDoubleSimilarityFunction extends SimFunction<WrappedArray<Double>> {
	private static final long serialVersionUID = 1L;
	public static final Log LOG = LogFactory
			.getLog(ArrayDoubleSimilarityFunction.class);

	
	public static Double cosineSimilarity(Double[] vectorA, Double[] vectorB) {
    	if (vectorA==null || vectorB==null || vectorA.length==0 || vectorB.length==0 || vectorA.length != vectorB.length) {
    		return 0.0;
    	}
	    double dotProduct = 0.0;
	    double normA = 0.0;
	    double normB = 0.0;
	    for (int i = 0; i < vectorA.length; i++) {
	    	
	    	if (vectorA[i]==null || vectorB[i]==null) {
	    		return 0.0;
	    	}
	    	
	        dotProduct += vectorA[i] * vectorB[i];
	        normA += Math.pow(vectorA[i], 2);
	        normB += Math.pow(vectorB[i], 2);
	    }   
	   
	    // If any of them is 0 then doesn't match
	    if (normA > 0 && normB > 0) {
	   	    double cosineVal = Math.abs(dotProduct / (Math.sqrt(normA) * Math.sqrt(normB)));
	   	    
	   	    // to avoid floating point errors
	   	    if(cosineVal > 1) {
	   	    	return 1.0;
	   	    } else {
	   	    	return cosineVal;
	   	    }
			
	    } else {
	    	return 0.0;
	    }
	}	
	
	public ArrayDoubleSimilarityFunction() {
		super("ArrayDoubleSimilarityFunction");
	}

	
	public Double call(Double[] first, Double[] second) {
		return cosineSimilarity(first,second);	
	}
	
	@Override
	public Double call(WrappedArray<Double> t1, WrappedArray<Double> t2) {
		Double[] t1Arr = new Double[] {};
		if (t1!=null) {
			t1Arr = new Double[t1.length()];
			t1.copyToArray(t1Arr);
		}
		Double[] t2Arr = new Double[] {};
		if (t2!=null) {
			t2Arr = new Double[t2.length()];
			t2.copyToArray(t2Arr);
		}
		return call(t1Arr, t2Arr);
	}
	
}
