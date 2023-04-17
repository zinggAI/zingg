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
	   	    return Math.abs(dotProduct / (Math.sqrt(normA) * Math.sqrt(normB)));
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
