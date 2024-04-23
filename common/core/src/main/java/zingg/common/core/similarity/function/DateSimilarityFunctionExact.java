package zingg.common.core.similarity.function;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DateSimilarityFunctionExact extends SimFunction<Date> {
	private static final long serialVersionUID = 1L;
	public static final Log LOG = LogFactory
			.getLog(DateSimilarityFunctionExact.class);

	public DateSimilarityFunctionExact() {
		super("DateSimilarityFunctionExact");
	}

	@Override
	public Double call(Date first, Date second) {
		if (first == null || second == null) return 1d;
		double score = first.equals(second) ? 1d : 0d;
		return score;		
	}
}
