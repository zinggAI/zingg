package zingg.common.core.DeterministicMatching;

import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import zingg.common.client.Arguments;
import zingg.common.client.DeterministicMatching;
import zingg.common.client.ZFrame;
import zingg.common.client.util.ColName;
import zingg.common.client.util.ColValues;
import zingg.common.core.util.DSUtil;

public class DeterministicMatchingUtil<S,D,R,C> implements Serializable {
	
	private static final long serialVersionUID = 1L;
	public static final Log LOG = LogFactory.getLog(DeterministicMatchingUtil.class); 

    protected Arguments args;	
    protected DSUtil<S, D, R, C> dsUtil;
    protected DeterministicMatchingFilter<D,R,C> obvDupeFilter;

	public DeterministicMatchingUtil(DSUtil<S, D, R, C> dsUtil, Arguments args) {
		this.dsUtil = dsUtil;
		this.args = args;
		this.obvDupeFilter = new DeterministicMatchingFilter<D,R,C>();
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
		
		DeterministicMatching[] DeterministicMatching = args.getDeterministicMatching();
		
		// no condition specified
		if (DeterministicMatching == null || DeterministicMatching.length==0) {
			return null;
		}

		ZFrame<D,R,C> prefixBlocked = dsUtil.getPrefixedColumnsDS(blocked);		
		C gtCond = blocked.gt(prefixBlocked,ColName.ID_COL);
		
		ZFrame<D,R,C> onlyIds = null;
		
		// loop thru the values and build a filter condition
			// instead of using one big condition with AND , OR
			// we are breaking it down and than doing UNION / DISTINCT in end
			// this is being done due to performance issues
			// please do not condense it into one big condition with and / or
			// ((col(ssn).eq(col(z_ssn)).or(col(dob).eq(z_col(dob)) => does not work due to performance
			// as we are doing a kind of cartesian join across all data in table to find obv dupe
			// in reverse i.e. in blocks it works as the data to be compared is within the row
			// but here in blocked we don't use hash and we have to search across the table
			// col(ssn).eq(col(z_ssn)) separately
			// col(dob).eq(z_col(dob) separately
			// union / distinct in end works
		for (int i = 0; i < DeterministicMatching.length; i++) {		
			
			C obvDupeDFFilter = obvDupeFilter.getDeterministicMatchingFilter(blocked,prefixBlocked,new DeterministicMatching[] {DeterministicMatching[i]},gtCond);
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
		DeterministicMatching[] DeterministicMatching = args.getDeterministicMatching();
		if (DeterministicMatching == null || DeterministicMatching.length == 0) {
			return blocks;
		}
		C reverseOBVDupeDFFilter = obvDupeFilter.getReverseDeterministicMatchingFilter(blocks,DeterministicMatching,null);
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
