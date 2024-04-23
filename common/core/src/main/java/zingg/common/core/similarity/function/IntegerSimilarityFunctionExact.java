package zingg.common.core.similarity.function;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class IntegerSimilarityFunctionExact extends SimFunction<Integer> {
	private static final long serialVersionUID = 1L;
	public static final Log LOG = LogFactory
			.getLog(IntegerSimilarityFunctionExact.class);

	public IntegerSimilarityFunctionExact() {
		super("IntegerSimilarityFunctionExact");
	}

	@Override
	public Double call(Integer first, Integer second) {
		if (first == null || second == null) return 1d;
		double score = first==second ? 1d : 0d;
		return score;		
	}
}
