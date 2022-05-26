package zingg.similarity.function;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.ml.util.Identifiable$;

import com.wcohen.ss.api.*;

public class AffineGapSimilarityFunction extends StringSimilarityDistanceFunction {

	public static final Log LOG = LogFactory
			.getLog(AffineGapSimilarityFunction.class);
	
	
	public AffineGapSimilarityFunction() {
		this("AffineGapSimilarityFunction");		
	}
	
	public AffineGapSimilarityFunction(String s) {
		super(s);
		gap = new SAffineGap();
	}

	@Override
	public Double call(String first, String second) {
		return super.call(first,second);
	}
	
	@Override
	 public String getUid() {
    	if (uid == null) {
    		uid = Identifiable$.MODULE$.randomUID("AffineGapSimilarityFunction");
    	}
    	return uid;
    }
	
	
}
