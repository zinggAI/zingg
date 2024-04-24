package zingg.common.core.similarity.function;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SimilarityFunctionExact<T> extends SimFunction<T> {
	private static final long serialVersionUID = 1L;
	public static final Log LOG = LogFactory
			.getLog(SimilarityFunctionExact.class);

	public SimilarityFunctionExact(String name) {
		super(name);
	}

	@Override
	public Double call(T first, T second) {
		if (first == null || second == null) return 1d;
		double score = first.equals(second) ? 1d : 0d;
		return score;		
	}
}
