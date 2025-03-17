package zingg.common.core.pairs;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import zingg.common.client.IArguments;
import zingg.common.client.ZFrame;
import zingg.common.client.model.IInputData;
import zingg.common.client.model.LinkInputData;
import zingg.common.client.util.ColName;
import zingg.common.client.util.DSUtil;

public class SelfPairBuilderSourceSensitive<S, D, R, C> extends SelfPairBuilder<S, D, R, C> {

	public static final Log LOG = LogFactory.getLog(SelfPairBuilderSourceSensitive.class);    

	public SelfPairBuilderSourceSensitive(DSUtil<S, D, R, C> dsUtil, IArguments args) {
		super(dsUtil, args);
	}
	
	@Override
	public ZFrame<D,R,C> getPairs(IInputData<D,R,C> blockedInput, IInputData<D,R,C> bAll) throws Exception{
		// THIS LOG IS NEEDED FOR PLAN CALCULATION USING COUNT, DO NOT REMOVE
		LOG.info("in getBlocks, blocked count is " + blockedInput.getTotalInput().count());
		return getDSUtil().joinWithItselfSourceSensitive((LinkInputData<D, R, C>) blockedInput, ColName.HASH_COL, args).cache();
	}

}
