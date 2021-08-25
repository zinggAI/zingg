package zingg.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import zingg.block.Block;

public class Heuristics {
	
	public static final Log LOG = LogFactory.getLog(Heuristics.class);
	
	public static long getMaxBlockSize(long totalCount) {
		long maxSize = 8;
		/*if  (totalCount > 100 && totalCount < 500){
			maxSize = totalCount / 5;
		}
		else {*/	
			maxSize = (long) (0.001 * totalCount);
			LOG.debug("**Block size found **" + maxSize);
			if (maxSize > 100) maxSize = 100;
			if (maxSize <= 8) maxSize = 8;
		//}
		LOG.info("**Block size **" + maxSize + " and total count was " + totalCount);
		LOG.info("Heuristics suggest " + maxSize);
		return maxSize;
	}

}
