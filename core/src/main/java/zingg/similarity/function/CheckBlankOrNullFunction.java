package zingg.similarity.function;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.ml.util.Identifiable$;

public class CheckBlankOrNullFunction extends BaseSimilarityFunction<String> {

	public static final Log LOG = LogFactory
			.getLog(CheckBlankOrNullFunction.class);

	public CheckBlankOrNullFunction() {
		super("CheckBlankOrNullFunction");
		// TODO Auto-generated constructor stub
	}

	public CheckBlankOrNullFunction(String name) {
		super(name);
	}

	@Override
	 public String getUid() {
    	if (uid == null) {
    		uid = Identifiable$.MODULE$.randomUID("CheckBlankOrNullFunction");
    	}
    	return uid;
    }
	

	@Override
	public Double call(String first, String second) {
		if (first != null && first.trim().length() !=0 && second != null && second.trim().length() !=0) {
			return 1d;
		}
		return 0d;		
	}

	

}
