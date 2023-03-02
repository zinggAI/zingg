package zingg.common.core.similarity.function;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;



public class PinCodeMatchTypeFunction extends StringSimilarityFunction {

	public static final Log LOG = LogFactory
			.getLog(PinCodeMatchTypeFunction.class);
	
	
	public PinCodeMatchTypeFunction() {
		this("PinCodeMatchTypeFunction");		
	}
	
	public PinCodeMatchTypeFunction(String s) {
		super(s);
	}
	
	@Override
	public Double call(String first, String second) {
		if (first == null || first.trim().length() ==0) return 1d;
		if (second == null || second.trim().length() ==0) return 1d;
		first = first.split("-")[0];
		second = second.split("-")[0];
		double score = first.trim().equalsIgnoreCase(second.trim()) ? 1d : 0d;
		return score;		
	}	
}