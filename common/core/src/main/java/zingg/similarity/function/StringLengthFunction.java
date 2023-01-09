package zingg.similarity.function;

import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class StringLengthFunction {/*extends StringSimilarityFunction {

	public static final Log LOG = LogFactory
			.getLog(StringLengthFunction.class);
	
	
	public StringLengthFunction() {
		super("StringLengthFunction");
		//gap = new SAffineGap();
	}

	public void operate(SimFunctionContext<String> context) {
		String first = context.getFirstOperand();
		String second = context.getSecondOperand();
		double score1 = 0.0;
		double score2 = 0.0;
		double score = 0.0;
		try {
			if (!(first == null || first.trim().equals(""))) {
				score1 = first.length();
			}
			if (!(second == null || second.trim().equals(""))) {
				score2 = second.length();
			}
			score = score1 * score2;
			
		} catch (Exception e) {
			LOG.warn("Error processing differences for " + first + "," + second);
		} finally {
			context.addToResult(score);
			// LOG.debug("Affine gap bw " + first + " and " + second + " is " +
			// score1 + "," + score2 + ", " + score);
			// LOG.debug(gap.explainScore(first, second));
		}
	}

	public void prepare() {

	}

	public void cleanUp() {
		// TODO Auto-generated method stub

	}

	@Override
	public int getNumFeatures() {
		return 1;
	}

	@Override
	public void setNorm() {
		this.norm.add(true);
	}
	*/

}
