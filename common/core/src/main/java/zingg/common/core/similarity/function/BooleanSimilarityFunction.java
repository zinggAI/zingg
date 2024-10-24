package zingg.common.core.similarity.function;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class BooleanSimilarityFunction extends SimilarityFunctionExact<Boolean>{

    private static final long serialVersionUID = 1L;
	public static final Log LOG = LogFactory.getLog(BooleanSimilarityFunction.class);

	public BooleanSimilarityFunction() {
		super("BooleanSimilarityFunction");
	}
    
    @Override
    public Double call(Boolean first, Boolean second) {
		return (new SimilarityFunctionExact("BooleanSimilarityFunction")).call(first,second);
	}

}
