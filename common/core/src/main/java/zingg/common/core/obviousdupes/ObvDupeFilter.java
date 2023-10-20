package zingg.common.core.obviousdupes;

import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import zingg.common.client.Arguments;
import zingg.common.client.ObviousDupes;
import zingg.common.client.ZFrame;
import zingg.common.client.util.ColName;
import zingg.common.core.Context;

public class ObvDupeFilter<S,D,R,C,T> implements Serializable {
	
	private static final long serialVersionUID = 1L;
	public static final Log LOG = LogFactory.getLog(ObvDupeFilter.class); 

    protected Arguments args;	
    protected Context<S,D,R,C,T> context;
    protected String name;
    protected ObvDupeFilterHelper<S,D,R,C,T> obvDupeFilterHelper;

	public ObvDupeFilter(Context<S,D,R,C,T> context, Arguments args) {
		this.context = context;
		this.args = args;
		this.name = this.getClass().getName();
		this.obvDupeFilterHelper = new ObvDupeFilterHelper<S,D,R,C,T>();
	}

	/**
	 * Input data example :
	 * 
	 * Z_ZID	FNAME	LNAME	DOB			Z_HASH	Z_ZSOURCE	
	 * 3		Érik	Guay	19830807	-798	customers
	 * 11		xani	green	19390410	890		customers
	 * 19		sachin	step	19461101 	700		customers
	 * 23		Érika	Charles	19830807	991		customers
	 * 
	 * Output data example (if say DOB is obv dupe condition):
	 * Z_ZID	Z_Z_ZID	Z_PREDICTION	Z_SCORE
	 * 23		3		1.0				1.0
	 * 
	 * 
	 * @param blocked
	 * @return
	 */
	public ZFrame<D, R, C> getObvDupePairs(ZFrame<D, R, C> blocked) {
		
		ObviousDupes[] obviousDupes = args.getObviousDupes();
		
		// no condition specified
		if (obviousDupes == null || obviousDupes.length==0) {
			return null;
		}

		ZFrame<D,R,C> prefixBlocked = context.getDSUtil().getPrefixedColumnsDS(blocked);		
		C gtCond = blocked.gt(prefixBlocked,ColName.ID_COL);
		
		ZFrame<D,R,C> onlyIds = null;
		
		// loop thru the values and build a filter condition		
		for (int i = 0; i < obviousDupes.length; i++) {		
			
			C obvDupeDFFilter = obvDupeFilterHelper.getObviousDupesFilter(blocked,prefixBlocked,new ObviousDupes[] {obviousDupes[i]},gtCond);
			ZFrame<D,R,C> onlyIdsTemp =  blocked
					.joinOnCol(prefixBlocked, obvDupeDFFilter).select(ColName.ID_COL, ColName.COL_PREFIX + ColName.ID_COL);
			
			if(onlyIds==null) {
				onlyIds = onlyIdsTemp;
			} else {
				onlyIds = onlyIds.unionAll(onlyIdsTemp);
			}
			
		}
		
		// remove duplicate pairs
		onlyIds = onlyIds.distinct();		
		onlyIds = obvDupeFilterHelper.massageAllEquals(onlyIds);
		onlyIds = onlyIds.cache();

		return onlyIds;
	}

	/**
	 * Input data format :
	 * 
	 * Z_ZID	FNAME	LNAME	DOB			Z_HASH	Z_ZSOURCE	Z_Z_ZID	Z_FNAME	Z_LNAME	Z_DOB		Z_Z_HASH Z_Z_ZSOURCE
	 * 3		Érik	Guay	19830807	-798	customers	23		Érika	Charles	19830807	-798	customers
	 * 11		xani	green	19390410	890		customers	19		x		g		19461101 	890		customers
	 * 
	 * Output data example (if say DOB is obv dupe condition):
	 * Z_ZID	FNAME	LNAME	DOB			Z_HASH	Z_ZSOURCE	Z_Z_ZID	Z_FNAME	Z_LNAME	Z_DOB		Z_Z_HASH Z_Z_ZSOURCE
	 * 11		xani	green	19390410	890		customers	19		x		g		19461101 	890		 customers
	 * 
	 * 
	 * @param blocked
	 * @return
	 */
	public ZFrame<D, R, C> removeObvDupesFromBlocks(ZFrame<D, R, C> blocks) {
		
		LOG.debug("blocks count before removing obvDupePairs " + blocks.count());
		ObviousDupes[] obviousDupes = args.getObviousDupes();
		if (obviousDupes == null || obviousDupes.length == 0) {
			return blocks;
		}
		C reverseOBVDupeDFFilter = obvDupeFilterHelper.getReverseObviousDupesFilter(blocks,obviousDupes,null);
		if (reverseOBVDupeDFFilter != null) {
			// remove dupes as already considered in obvDupePairs
			blocks = blocks.filter(reverseOBVDupeDFFilter);				
		} 
		LOG.debug("blocks count after removing obvDupePairs " + blocks.count());
		return blocks;
	}
	
	public ZFrame<D, R, C> removeObvDupesFromBlocks(ZFrame<D, R, C> blocks,ZFrame<D, R, C> obvDupePairs) {
		
		if(obvDupePairs==null || obvDupePairs.isEmpty()) {
			return blocks;
		}
		
		return removeObvDupesFromBlocks(blocks);
		
	}
	
}
