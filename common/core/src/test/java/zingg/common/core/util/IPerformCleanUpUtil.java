package zingg.common.core.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import zingg.common.core.ZinggException;

public interface IPerformCleanUpUtil<S> {

    public static final Log LOG = LogFactory.getLog(IPerformCleanUpUtil.class);

    default void performCleanup(TestType t) {
		ICleanUpUtil<S> cleanUpUtil = getCleanupUtil();
		boolean cleanupDone = cleanUpUtil.performCleanup(getSession(), t, getModelId());
		if (!cleanupDone) {
			LOG.error("Unable to perform cleanup!!");
			throw new ZinggException("Unable to perform cleanup");
		}
	}

	public abstract ICleanUpUtil<S> getCleanupUtil();

    public abstract String getModelId();

    public abstract S getSession();
    
}
