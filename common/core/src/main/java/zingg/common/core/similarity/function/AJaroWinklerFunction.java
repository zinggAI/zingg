package zingg.common.core.similarity.function;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class AJaroWinklerFunction extends StringSimilarityDistanceFunction {

	public static final Log LOG = LogFactory.getLog(JaroWinklerFunction.class);

	public AJaroWinklerFunction() {
		this("AJaroWinklerFunction");
		
	}

	public AJaroWinklerFunction(String s) {
		super(s);
		gap = new SJaroWinkler();
		
	}
	
	
}
