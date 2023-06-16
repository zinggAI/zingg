package zingg.common.core.similarity.function;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DateSimilarityFunction extends SimFunction<Date> {
	
	public static final Log LOG = LogFactory.getLog(DateSimilarityFunction.class);


	public DateSimilarityFunction() {
		super("DateSimilarityFunction");
	}

	@Override
	public Double call(Date first, Date second) {
		if (first == null || second == null) return 1d;
		long timeDiffInMillis = first.getTime() - second.getTime();
		//added 1 to avoid 0 division
		long timeAddinMillis = 1+first.getTime() + second.getTime();
		//we want similarity, hence we subtract from 1 so that closer values have higher score
		// similar to DoubleSimilarityFunction
		double diff = 1-Math.abs(timeDiffInMillis/(1.0*timeAddinMillis));
		return diff;
	}	

}


