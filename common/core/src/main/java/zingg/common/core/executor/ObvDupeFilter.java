package zingg.common.core.executor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import zingg.common.client.Arguments;
import zingg.common.client.ClientOptions;
import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;
import zingg.common.client.ZinggOptions;
import zingg.common.client.license.IZinggLicense;
import zingg.common.client.util.ColName;
import zingg.common.client.util.ColValues;
import zingg.common.core.Context;

public class ObvDupeFilter<S,D,R,C,T> extends ZinggBase<S, D, R, C, T> {
	
	private static final long serialVersionUID = 1L;
	public static final Log LOG = LogFactory.getLog(ObvDupeFilter.class); 
	
	public ObvDupeFilter(Context<S,D,R,C,T> context, ZinggOptions zinggOptions, ClientOptions clientOptions, Arguments args) {
		setContext(context);
		setZinggOptions(zinggOptions);
		setClientOptions(clientOptions);
		setArgs(args);
		setName(this.getClass().getName());
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
	 * 23		3		1				1
	 * 
	 * 
	 * @param blocked
	 * @return
	 */
	public ZFrame<D, R, C> getObvDupePairs(ZFrame<D, R, C> blocked) {
		
		System.out.println("getObvDupePairs input");
		blocked.show();
		
		String obviousDupeString = getArgs().getObviousDupeCondition();
		
		if (obviousDupeString == null || obviousDupeString.trim().isEmpty()) {
			return null;
		}

		ZFrame<D,R,C> prefixBlocked = getDSUtil().getPrefixedColumnsDS(blocked);		
		C gtCond = blocked.gt(prefixBlocked,ColName.ID_COL);
		
		ZFrame<D,R,C> onlyIds = null;
		
		// split on || (orSeperator)
		String[] obvDupeORConditions = obviousDupeString.trim().split(ZFrame.orSeperator);		
		// loop thru the values and build a filter condition		
		for (int i = 0; i < obvDupeORConditions.length; i++) {		
			
			C obvDupeDFFilter = blocked.getObviousDupesFilter(prefixBlocked,obvDupeORConditions[i],gtCond);
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
		onlyIds = massageAllEquals(onlyIds);
		onlyIds = onlyIds.cache();

		System.out.println("getObvDupePairs output");
		onlyIds.show();
		
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
		
		System.out.println("removeObvDupesFromBlocks input");
		blocks.show();
		LOG.debug("blocks count before removing obvDupePairs " + blocks.count());
		C reverseOBVDupeDFFilter = blocks.getReverseObviousDupesFilter(getArgs().getObviousDupeCondition(),null);
		if (reverseOBVDupeDFFilter != null) {
			// remove dupes as already considered in obvDupePairs
			blocks = blocks.filter(reverseOBVDupeDFFilter);				
		} 
		System.out.println("removeObvDupesFromBlocks output");
		blocks.show();
		LOG.debug("blocks count after removing obvDupePairs " + blocks.count());
		return blocks;
	}
	
	protected ZFrame<D,R,C>massageAllEquals(ZFrame<D,R,C>allEqual) {
		allEqual = allEqual.withColumn(ColName.PREDICTION_COL, ColValues.IS_MATCH_PREDICTION);
		allEqual = allEqual.withColumn(ColName.SCORE_COL, ColValues.FULL_MATCH_SCORE);
		return allEqual;
	}
	
	
	
	@Override
	public void execute() throws ZinggClientException {
		throw new UnsupportedOperationException();		
	}

}
