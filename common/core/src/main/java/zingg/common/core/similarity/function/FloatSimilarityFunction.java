package zingg.common.core.similarity.function;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class FloatSimilarityFunction extends SimFunction<Float> {
	private static final long serialVersionUID = 1L;
	public static final Log LOG = LogFactory
			.getLog(FloatSimilarityFunction.class);

	public FloatSimilarityFunction() {
		super("FloatSimilarityFunction");
	}

	@Override
	public Double call(Float first, Float second) {
		if (first == null || first.isNaN()) return 1d;
		if (second == null || second.isNaN()) return 1d;
		//we want similarity, hence we subtract from 1 so that closer values have higher score
		double score = 1 - (Math.abs(first-second))/(1.0+first + second);
		LOG.debug(" DoubleSim bw " + first + " and second " + second + " is "
		 + score);
		return score;
	}

}
