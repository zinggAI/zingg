package zingg.common.core.similarity.function;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ArrayDoubleSimilarityFunction extends SimFunction<Double[]> {
	private static final long serialVersionUID = 1L;
	public static final Log LOG = LogFactory
			.getLog(ArrayDoubleSimilarityFunction.class);

	
	public static Double cosineSimilarity(Double[] vectorA, Double[] vectorB) {
	    double dotProduct = 0.0;
	    double normA = 0.0;
	    double normB = 0.0;
	    for (int i = 0; i < vectorA.length; i++) {
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

	@Override
	public Double call(Double[] first, Double[] second) {
		return cosineSimilarity(first,second);	
	}
}
