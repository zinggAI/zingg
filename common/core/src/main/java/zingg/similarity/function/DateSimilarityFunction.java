package zingg.similarity.function;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DateSimilarityFunction extends SimFunction<Date> {
	
	public static final Log LOG = LogFactory.getLog(DateSimilarityFunction.class);


	public DateSimilarityFunction() {
		super("DateSimilarityFunction");
		// TODO Auto-generated constructor stub
	}

	@Override
	public Double call(Date first, Date second) {
		double diff = Math.abs(first.getTime() - second.getTime())/(24*60*60*1000);
		return diff;
	}


	
	

}


