package zingg.common.core.similarity.function;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class IntegerSimilarityFunction extends SimFunction<Integer> {
	public static final Log LOG = LogFactory
			.getLog(IntegerSimilarityFunction.class);

	public IntegerSimilarityFunction() {
		super("IntegerSimilarityFunction");
	}

	@Override
	public Double call(Integer first, Integer second) {
		double score = 0;
		if (first != null && second != null) {
			if (first+second != 0) score = 2.0*Math.abs(first - second)/(first + second);
			LOG.debug(" IntegerSim bw " + first + " and second " + second + " is "
						+ score);
		}
		return score;		
	}
}
