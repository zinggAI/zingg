package zingg.similarity.function;

import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.ml.util.Identifiable$;

public class AJaroWinklerFunction extends StringSimilarityDistanceFunction {

	public static final Log LOG = LogFactory.getLog(JaroWinklerFunction.class);

	public AJaroWinklerFunction() {
		this("AJaroWinklerFunction");
		
	}

	public AJaroWinklerFunction(String s) {
		super(s);
		gap = new SJaroWinkler();
		
	}
	
	@Override
	public String getUid() {
   	if (uid == null) {
   		uid = Identifiable$.MODULE$.randomUID("AJaroWinklerFunction");
   	}
   	return uid;
   }
}
