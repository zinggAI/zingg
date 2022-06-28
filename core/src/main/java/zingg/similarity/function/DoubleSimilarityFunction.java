package zingg.similarity.function;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DoubleSimilarityFunction extends BaseSimilarityFunction<Double> {
	public static final Log LOG = LogFactory
			.getLog(DoubleSimilarityFunction.class);

	public DoubleSimilarityFunction() {
		super("DoubleSimilarityFunction");
		// TODO Auto-generated constructor stub
	}

	@Override
	public Double call(Double first, Double second) {
		if (first == null || first.isNaN()) return 1d;
		if (second == null || second.isNaN()) return 1d;
		double score = (Math.abs(first-second))/(1.0+first + second);
		LOG.debug(" DoubleSim bw " + first + " and second " + second + " is "
		 + score);
		return score;
	}

}
