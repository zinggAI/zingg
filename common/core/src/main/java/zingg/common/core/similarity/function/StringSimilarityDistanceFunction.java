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

	public StringSimilarityDistanceFunction(String name, AbstractStringDistance gap) {
		this(name);
		this.gap = gap;
	}

	public StringSimilarityDistanceFunction(){}
	
	public AbstractStringDistance getDistanceFunction(){
		return gap;
	}
	
	@Override
	public Double call(String first, String second) {
		if (first == null || first.length() ==0) return 1d;
		if (second == null || second.length() ==0) return 1d;
		if (first.equals(second)) return 1d;
		double score = getDistanceFunction().score(first, second);
		if (Double.isNaN(score)) return 0d; 
		//LOG.warn(" score  " + gap.getClass().getName() +  " " + first + " " + second + " is " + score);
		return score;		
	}

}
  