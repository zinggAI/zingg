package zingg.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Heuristics {
	
	public static final Log LOG = LogFactory.getLog(Heuristics.class);
	public static final long MIN_SIZE = 8L;
	public static long getMaxBlockSize(long totalCount, long blockSizeFromConfig) {
		long maxSize = MIN_SIZE;
		/*if  (totalCount > 100 && totalCount < 500){
			maxSize = totalCount / 5;
		}
		else {*/	
			maxSize = (long) (0.001 * totalCount);
			LOG.debug("**Block size found **" + maxSize);
			if (maxSize > blockSizeFromConfig) maxSize = blockSizeFromConfig;
			if (maxSize <= MIN_SIZE) maxSize = MIN_SIZE;
		//}
		LOG.info("**Block size **" + maxSize + " and total count was " + totalCount);
		LOG.info("Heuristics suggest " + maxSize);
		return maxSize;
	}

}
