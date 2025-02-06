package zingg.common.core.similarity.function;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.wcohen.ss.*;

public abstract class StringSimilarityDistanceFunction extends StringSimilarityFunction{
	
	protected AbstractStringDistance gap;

	public static final Log LOG = LogFactory
			.getLog(StringSimilarityDistanceFunction.class);
	
	public StringSimilarityDistanceFunction(String name) {
		super(name);
	}

	public StringSimilarityDistanceFunction(){}
	
	public AbstractStringDistance getDistanceFunction(){
		return gap;
	}
	
	@Override
	public Double call(String first, String second) {
		if (first == null || first.trim().length() ==0) return 1d;
		if (second == null || second.trim().length() ==0) return 1d;
		if (first.equalsIgnoreCase(second)) return 1d;
		double score = getDistanceFunction().score(first, second);
		if (Double.isNaN(score)) return 0d; 
		//LOG.warn(" score  " + gap +  " " + first + " " + second + " is " + score);
		return score;		
	}

}
  