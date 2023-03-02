package zingg.common.core.similarity.function;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.wcohen.ss.api.*;

public class AffineGapSimilarityFunction extends StringSimilarityDistanceFunction {

	public static final Log LOG = LogFactory
			.getLog(AffineGapSimilarityFunction.class);
	
	
	public AffineGapSimilarityFunction() {
		this("AffineGapSimilarityFunction");		
	}
	
	public AffineGapSimilarityFunction(String s) {
		this.name = s;
		gap = new SAffineGap();
	}

	@Override
	public Double call(String first, String second) {
		return super.call(first,second);
	}
	
	
	
	
}
