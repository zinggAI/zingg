package zingg.common.core.similarity.function;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class BooleanSimilarityFunction extends SimFunction<Boolean>{

    private static final long serialVersionUID = 1L;
	public static final Log LOG = LogFactory
			.getLog(BooleanSimilarityFunction.class);

	public BooleanSimilarityFunction() {
		super("BooleanSimilarityFunction");
	}
    
    @Override
    public Double call(Boolean first, Boolean second) {
		if (first == null && second == null) return 1d;
        if (first == null || second == null) return 0d;	
        return (first.equals(second) ? 1d : 0d);
	}

}
