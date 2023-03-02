package zingg.common.core.similarity.function;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SameFirstWordFunction extends AffineGapSimilarityFunction {

	public static final Log LOG = LogFactory
			.getLog(SameFirstWordFunction.class);
	
	
	public SameFirstWordFunction() {
		super("SameFirstWordFunction");
		//gap = new SAffineGap();
	}

	

/*	public void operate(SimFunctionContext<String> context) {
		String first = context.getFirstOperand();
		String second = context.getSecondOperand();
		double score1 = 0.0;
		double score2 = 0.0;
		double score = 0.0;
		try {
			if (!(first == null || first.trim().equals(""))) {
				score1 = 1.0d;
			}
			if (!(second == null || second.trim().equals(""))) {
				score2 = 1.0d;
			}
			if (score1 == 1.0d && score2 == 1.0d) {
				SAffineGap gap = new SAffineGap();
				SJaroWinkler gap1 = new SJaroWinkler();
				String f = first.split("\\s+")[0];
				String s = second.split("\\s+")[0];
				if (!(f == null || f.trim().equals("")) && !(s == null || s.trim().equals(""))) {
					score = gap.score(f.trim(), s.trim());
					score1 = gap1.score(f.trim(), s.trim());
					
					//LOG.debug(gap.explainScore(first, second));
					gap = null;
				}
				LOG.debug("gap bw " + f + " and " + s + " is " +
						 score1 + "," + score2 + ", " + score);
			}			
		} catch (Exception e) {
			LOG.warn("Error processing differences for " + first + "," + second);
		} finally {
			context.addToResult(score1);
			//context.addToResult(score2);
			context.addToResult(score);
			LOG.debug("Same first word gap bw " + first + " and " + second + " is " +
			 score1 + "," + score2 + ", " + score + ", " + score1);
		}
	}
*/

	@Override
	public Double call(String first, String second) {
		if (first == null || first.trim().length() ==0) return 1d;
		if (second == null || second.trim().length() ==0) return 1d;
		String f = first.split("-")[0];
		String s = second.split("-")[0];
		double score = super.call(f, s);
		//LOG.info(" score " + f + " " + s + " " + score);
		return score;		
	}

}
