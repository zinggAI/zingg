package zingg.common.core.obviousdupes;

import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import zingg.common.client.Arguments;
import zingg.common.client.ObviousDupes;
import zingg.common.client.ZFrame;
import zingg.common.client.util.ColName;
import zingg.common.client.util.ColValues;
import zingg.common.core.util.DSUtil;

public class ObviousDupesUtil<S,D,R,C> implements Serializable {
	
	private static final long serialVersionUID = 1L;
	public static final Log LOG = LogFactory.getLog(ObviousDupesUtil.class); 

    protected Arguments args;	
    protected DSUtil<S, D, R, C> dsUtil;
    protected ObviousDupesFilter<D,R,C> obvDupeFilter;

	public ObviousDupesUtil(DSUtil<S, D, R, C> dsUtil, Arguments args) {
		this.dsUtil = dsUtil;
		this.args = args;
		this.obvDupeFilter = new ObviousDupesFilter<D,R,C>();
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

		ZFrame<D,R,C> prefixBlocked = dsUtil.getPrefixedColumnsDS(blocked);		
		C gtCond = blocked.gt(prefixBlocked,ColName.ID_COL);
		
		ZFrame<D,R,C> onlyIds = null;
		
		// loop thru the values and build a filter condition		
		for (int i = 0; i < obviousDupes.length; i++) {		
			
			C obvDupeDFFilter = obvDupeFilter.getObviousDupesFilter(blocked,prefixBlocked,new ObviousDupes[] {obviousDupes[i]},gtCond);
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
		onlyIds = massageObvDupes(onlyIds);
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
		C reverseOBVDupeDFFilter = obvDupeFilter.getReverseObviousDupesFilter(blocks,obviousDupes,null);
		// remove dupes as already considered in obvDupePairs
		blocks = blocks.filter(reverseOBVDupeDFFilter);				
		LOG.debug("blocks count after removing obvDupePairs " + blocks.count());
		return blocks;
	}
	
	public ZFrame<D, R, C> removeObvDupesFromBlocks(ZFrame<D, R, C> blocks,ZFrame<D, R, C> obvDupePairs) {
		
		if(obvDupePairs==null || obvDupePairs.isEmpty()) {
			return blocks;
		}
		
		return removeObvDupesFromBlocks(blocks);
		
	}
	
	/**
	 * Add prediction and score cols
	 * @param obvDupes
	 * @return
	 */
	public ZFrame<D,R,C> massageObvDupes(ZFrame<D,R,C> obvDupes) {
		obvDupes = obvDupes.withColumn(ColName.PREDICTION_COL, ColValues.IS_MATCH_PREDICTION);
		obvDupes = obvDupes.withColumn(ColName.SCORE_COL, ColValues.FULL_MATCH_SCORE);
		return obvDupes;
	}
	
}
