package zingg.common.core.pairs;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import zingg.common.client.IArguments;
import zingg.common.client.ZFrame;
import zingg.common.core.data.IData;
import zingg.common.client.util.ColName;
import zingg.common.client.util.DSUtil;

public class SelfPairBuilder<S, D, R, C> implements IPairBuilder<S, D, R, C> {

	protected DSUtil<S, D, R, C> dsUtil;
	public static final Log LOG = LogFactory.getLog(SelfPairBuilder.class);    
	protected IArguments args;
	
	public SelfPairBuilder(DSUtil<S, D, R, C> dsUtil, IArguments args) {
		this.dsUtil = dsUtil;
		this.args = args;
	}
	
	@Override
	public ZFrame<D, R, C> getPairs(IData<D, R, C> blockedInput, IData<D,R,C> bAll) throws Exception {
		ZFrame<D, R, C> blockedInputData = blockedInput.getPrimary();
		ZFrame<D, R, C> blocked = blockedInputData.repartition(args.getNumPartitions(), blockedInputData.col(ColName.HASH_COL)).cache();
		ZFrame<D,R,C>joinH =  getDSUtil().joinWithItself(blocked, ColName.HASH_COL, true);
		joinH = joinH.filter(joinH.gt(ColName.ID_COL));
		/*ZFrame<D,R,C>joinH = blocked.as("first").joinOnCol(blocked.as("second"), ColName.HASH_COL)
			.selectExpr("first.z_zid as z_zid", "second.z_zid as z_z_zid");
		*/
		//joinH.show();
		/*joinH = joinH.filter(joinH.gt(ColName.ID_COL));	
		if (LOG.isDebugEnabled()) LOG.debug("Num comparisons " + joinH.count());
		joinH = joinH.repartition(args.getNumPartitions(), joinH.col(ColName.ID_COL));
		bAll = bAll.repartition(args.getNumPartitions(), bAll.col(ColName.ID_COL));
		joinH = joinH.joinOnCol(bAll, ColName.ID_COL);
		LOG.warn("Joining with actual values");
		//joinH.show();
		bAll = getDSUtil().getPrefixedColumnsDS(bAll);
		//bAll.show();
		joinH = joinH.repartition(args.getNumPartitions(), joinH.col(ColName.COL_PREFIX + ColName.ID_COL));
		joinH = joinH.joinOnCol(bAll, ColName.COL_PREFIX + ColName.ID_COL);
		LOG.warn("Joining again with actual values");
		*/
		//joinH.show();
		return joinH;
	}

	public DSUtil<S, D, R, C> getDSUtil() {
		return dsUtil;
	}

	public void setDSUtil(DSUtil<S, D, R, C> dsUtil) {
		this.dsUtil = dsUtil;
	}

	
	
}
