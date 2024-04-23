package zingg.common.core.similarity.function;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class LongSimilarityFunctionExact extends SimFunction<Long> {
	private static final long serialVersionUID = 1L;
	public static final Log LOG = LogFactory
			.getLog(LongSimilarityFunctionExact.class);

	public LongSimilarityFunctionExact() {
		super("LongSimilarityFunctionExact");
	}

	@Override
	public Double call(Long first, Long second) {
		if (first == null || second == null) return 1d;
		double score = first==second ? 1d : 0d;
		return score;		
	}
}
