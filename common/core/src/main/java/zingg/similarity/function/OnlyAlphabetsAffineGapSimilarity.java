package zingg.similarity.function;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.ml.util.Identifiable$;

import com.wcohen.ss.api.*;

public class OnlyAlphabetsAffineGapSimilarity extends StringSimilarityDistanceFunction {

	public static final Log LOG = LogFactory
			.getLog(OnlyAlphabetsAffineGapSimilarity.class);
	
	
	public OnlyAlphabetsAffineGapSimilarity() {
		this("OnlyAlphabetsAffineGapSimilarity");		
	}
	
	public OnlyAlphabetsAffineGapSimilarity(String s) {
		super(s);
		gap = new SAffineGap();
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
				score = super.call(first, second);
			}
			else {
				score = 1.0d;
			}
			
			
		}  catch (Exception e) {
			e.printStackTrace();
			LOG.warn("Error processing differences for " + first + "," + second);
		} finally {
			if (Double.isNaN(score)) {
				score = 0.0;
			}
			return score;
		}
	}
	
}
