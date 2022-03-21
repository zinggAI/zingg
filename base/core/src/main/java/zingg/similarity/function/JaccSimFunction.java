package zingg.similarity.function;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.ml.util.Identifiable$;

import com.wcohen.ss.api.*;

public class JaccSimFunction extends StringSimilarityDistanceFunction {

	public static final Log LOG = LogFactory.getLog(JaccSimFunction.class);

	public JaccSimFunction() {
		this("JaccSimFunction");
	}
	
	public JaccSimFunction(String s) {
		super(s);
		gap = new SJacc();
	}
	
	@Override
	 public String getUid() {
   	if (uid == null) {
   		uid = Identifiable$.MODULE$.randomUID("JaccSimFunction");
   	}
   	return uid;
   }

	
	
}
