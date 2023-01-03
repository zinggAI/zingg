package zingg.similarity.function;

import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.ml.util.Identifiable$;

public class JaroWinklerFunction extends StringSimilarityDistanceFunction {

	public static final Log LOG = LogFactory.getLog(JaroWinklerFunction.class);

		

	public JaroWinklerFunction() {
		this("JaroWinklerFunction");
	}
	
	
	public JaroWinklerFunction(String s) {
		super(s);
		gap = new SJaroWinkler();
	}
	
	
}
