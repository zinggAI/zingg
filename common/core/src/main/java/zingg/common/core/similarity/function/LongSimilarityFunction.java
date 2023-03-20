package zingg.common.core.similarity.function;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class LongSimilarityFunction extends SimFunction<Long> {
	private static final long serialVersionUID = 1L;
	public static final Log LOG = LogFactory
			.getLog(LongSimilarityFunction.class);

	public LongSimilarityFunction() {
		super("LongSimilarityFunction");
	}

	@Override
	public Double call(Long first, Long second) {
		double score = 0;
		if (first != null && second != null) {
			if (first+second != 0) score = 2.0*Math.abs(first - second)/(first + second);
			LOG.debug(" LongSim bw " + first + " and second " + second + " is "
						+ score);
		}
		return score;		
	}
}
