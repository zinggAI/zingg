package zingg.common.core.similarity.function;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class StringSimilarityFunction extends SimFunction<String> {

	public static final Log LOG = LogFactory
			.getLog(StringSimilarityFunction.class);

	public StringSimilarityFunction() {
		this("StringSimilarityFunction");
		// TODO Auto-generated constructor stub
	}

	public StringSimilarityFunction(String name) {
		super(name);
	}


	@Override
	public Double call(String first, String second) {
		if (first == null || first.trim().length() ==0) return 1d;
		if (second == null || second.trim().length() ==0) return 1d;
		double score = first.trim().equals(second.trim()) ? 1d : 0d;
		return score;		
	}

	

}
