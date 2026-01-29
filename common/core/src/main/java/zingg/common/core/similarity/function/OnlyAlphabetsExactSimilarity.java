package zingg.common.core.similarity.function;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class OnlyAlphabetsExactSimilarity extends StringSimilarityDistanceFunction {

	public static final Log LOG = LogFactory
			.getLog(OnlyAlphabetsExactSimilarity.class);
	
	
	public OnlyAlphabetsExactSimilarity() {
		this("OnlyAlphabetsExactSimilarity");		
	}
	
	public OnlyAlphabetsExactSimilarity(String s) {
		super(s);
	}
	
	

	@Override
	public Double call(String first, String second) {
		double score1 = 0.0;
		double score2 = 0.0;
		double score = 0.0;

		try {
			if (first == null || first.equals("")) {
				score1 = 1.0d;
			}
			if (second == null || second.equals("")) {
				score2 = 1.0d;
			}
			if (score1 != 1.0d && score2 != 1.0d) {
				first = first.replaceAll("[0-9.]", "");
				second = second.replaceAll("[0-9.]", "");
				score = first.equals(second)? 1.0d : 0.0d;
			}
			else {
				score = 1.0d;
			}
			
			
		}  catch (Exception e) {
			if(LOG.isDebugEnabled()) {
				e.printStackTrace();
			}
			LOG.warn("Error processing differences for " + first + "," + second);
		} finally {
			if (Double.isNaN(score)) {
				score = 0.0;
			}
			return score;
		}
	}
	
}
