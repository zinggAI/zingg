package zingg.similarity.function;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
	
	
	
}
