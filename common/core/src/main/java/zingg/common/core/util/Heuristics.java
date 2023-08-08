/*
 * Zingg
 * Copyright (C) 2021-Present  Zingg Labs,inc
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package zingg.common.core.util;

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
