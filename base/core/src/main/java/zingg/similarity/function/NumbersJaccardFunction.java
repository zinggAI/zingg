package zingg.similarity.function;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class NumbersJaccardFunction extends StringSimilarityDistanceFunction {

	public static final Log LOG = LogFactory
			.getLog(NumbersJaccardFunction.class);


	public NumbersJaccardFunction() {
		super("JaccSimFunction");
		gap = new SJacc();
	}

}
