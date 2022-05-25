package zingg.similarity.function;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.ml.util.Identifiable$;


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
				first = first.split("-",0)[0];
				second = second.split("-",0)[0];
				score = first.equalsIgnoreCase(second)? 1.0d : 0.0d;
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

//String first_part=string.split("-",0)[0];