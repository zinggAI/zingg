package zingg.common.core.similarity.function;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class BigramJaccSimFn extends StringSimilarityDistanceFunction {

	public static final Log LOG = LogFactory.getLog(BigramJaccSimFn.class);

	private BigramJaccard gap;

	public BigramJaccSimFn() {
		super("BigramJaccSimFn");
		gap = new BigramJaccard();
	}

	
	
}