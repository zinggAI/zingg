package zingg.common.core.similarity.function;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class StringSimilarityFunction extends SimFunction<String> {

	public static final Log LOG = LogFactory.getLog(StringSimilarityFunction.class);

	public StringSimilarityFunction() {
		this("StringSimilarityFunction");
	}

	public StringSimilarityFunction(String name) {
		super(name);
	}

	@Override
	public Double call(String first, String second) {
		if (first == null || first.length() ==0) return 1d;
		if (second == null || second.length() ==0) return 1d;
		double score = first.equals(second) ? 1d : 0d;
		return score;		
	}

	

}
