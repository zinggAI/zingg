package zingg.common.core.pairs;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import zingg.common.client.IArguments;
import zingg.common.client.ZFrame;
import zingg.common.core.data.IData;
import zingg.common.client.util.ColName;
import zingg.common.client.util.DSUtil;

public class SelfPairBuilderSourceSensitive<S, D, R, C> extends SelfPairBuilder<S, D, R, C> {

	public static final Log LOG = LogFactory.getLog(SelfPairBuilderSourceSensitive.class);    

	public SelfPairBuilderSourceSensitive(DSUtil<S, D, R, C> dsUtil, IArguments args) {
		super(dsUtil, args);
	}
	
	@Override
	public ZFrame<D,R,C> getPairs(IData<D, R, C> blockedInput, IData<D,R,C> bAll) throws Exception{
		ZFrame<D, R, C> blockedOne = blockedInput.getByIndex(0);
		ZFrame<D, R, C> blockedTwo = blockedInput.getByIndex(1);
		// THIS LOG IS NEEDED FOR PLAN CALCULATION USING COUNT, DO NOT REMOVE
		LOG.info("in getBlocks, blocked count is " + blockedOne.count() + blockedTwo.count());
		return getDSUtil().joinWithItselfSourceSensitive(blockedOne, blockedTwo, ColName.HASH_COL, args).cache();
	}

}
