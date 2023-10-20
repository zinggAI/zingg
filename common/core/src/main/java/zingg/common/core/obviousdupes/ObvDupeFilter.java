package zingg.common.core.obviousdupes;

import java.io.Serializable;
import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import zingg.common.client.Arguments;
import zingg.common.client.ObviousDupes;
import zingg.common.client.ZFrame;
import zingg.common.client.util.ColName;
import zingg.common.client.util.ColValues;
import zingg.common.core.Context;

public class ObvDupeFilter<S,D,R,C,T> implements Serializable {
	
	private static final long serialVersionUID = 1L;
	public static final Log LOG = LogFactory.getLog(ObvDupeFilter.class); 

    protected Arguments args;	
    protected Context<S,D,R,C,T> context;
    protected String name;

	public ObvDupeFilter(Context<S,D,R,C,T> context, Arguments args) {
		this.context = context;
		this.args = args;
		this.name = this.getClass().getName();
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
		
		ObviousDupes[] obviousDupes = getArgs().getObviousDupes();
		
		// no condition specified
		if (obviousDupes == null || obviousDupes.length==0) {
			return null;
		}

		ZFrame<D,R,C> prefixBlocked = getContext().getDSUtil().getPrefixedColumnsDS(blocked);		
		C gtCond = blocked.gt(prefixBlocked,ColName.ID_COL);
		
		ZFrame<D,R,C> onlyIds = null;
		
		// loop thru the values and build a filter condition		
		for (int i = 0; i < obviousDupes.length; i++) {		
			
			C obvDupeDFFilter = getObviousDupesFilter(blocked,prefixBlocked,new ObviousDupes[] {obviousDupes[i]},gtCond);
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
		ObviousDupes[] obviousDupes = getArgs().getObviousDupes();
		if (obviousDupes == null || obviousDupes.length == 0) {
			return blocks;
		}
		C reverseOBVDupeDFFilter = getReverseObviousDupesFilter(blocks,obviousDupes,null);
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
	
	protected ZFrame<D,R,C>massageAllEquals(ZFrame<D,R,C>allEqual) {
		allEqual = allEqual.withColumn(ColName.PREDICTION_COL, ColValues.IS_MATCH_PREDICTION);
		allEqual = allEqual.withColumn(ColName.SCORE_COL, ColValues.FULL_MATCH_SCORE);
		return allEqual;
	}
	
	/**
	 * 
	 * obviousDupeString format col1 & col2 | col3 | col4 & col5
	 * 
	 * @param obviousDupeString
	 * @return
	 */
	public C getObviousDupesFilter(ZFrame<D, R, C> df1, ObviousDupes[] obviousDupes, C extraAndCond) {
		return getObviousDupesFilter(df1,df1,obviousDupes,extraAndCond);
	}
	
	/**
	 * 
	 * obviousDupeString format col1 & col2 | col3 | col4 & col5
	 * 
	 * @param obviousDupeString
	 * @return
	 */
	
	public C getObviousDupesFilter(ZFrame<D, R, C> df1, ZFrame<D, R, C> dfToJoin, ObviousDupes[] obviousDupes, C extraAndCond) {
		
		if (dfToJoin==null || obviousDupes == null || obviousDupes.length == 0) {
			return null;
		}
		
		// loop thru the values and build a filter condition
		C filterExpr = null;
		
		for (int i = 0; i < obviousDupes.length; i++) {
			
			C andCond = null;

			HashMap<String,String>[] andConditions = obviousDupes[i].getMatchCondition();

			if (andConditions != null) {
				for (int j = 0; j < andConditions.length; j++) {

					String andCondStr = andConditions[j].get(ObviousDupes.fieldName);

					if (andCondStr != null && !andCondStr.trim().isEmpty()) {

						String colName = andCondStr.trim();
						C column = df1.col(colName);
						C columnWithPrefix = dfToJoin.col(ColName.COL_PREFIX + colName);

						C eqCond = df1.and(
								df1.and(
										df1.equalTo(column, columnWithPrefix),
										df1.isNotNull(column)
										),
								df1.isNotNull(columnWithPrefix)
								);

						andCond = (andCond != null) ? df1.and(andCond, eqCond) : eqCond;

					}
				}
			}

			if (andCond != null) {				
				filterExpr = (filterExpr != null) ? df1.or(filterExpr, andCond) : andCond;
			}

		}
		
		if (extraAndCond != null) {
			filterExpr = (filterExpr != null)  ? df1.and(filterExpr, extraAndCond) :  extraAndCond;			
		}
		
		return filterExpr;
	}

	/**
	 * 
	 * obviousDupeString format col1 & col2 | col3 | col4 & col5
	 * 
	 * @param obviousDupeString
	 * @return
	 */
	
	public C getReverseObviousDupesFilter(ZFrame<D, R, C> df1,ObviousDupes[] obviousDupes, C extraAndCond) {
		return getReverseObviousDupesFilter(df1,df1,obviousDupes,extraAndCond);
	}
		
	/**
	 * 
	 * obviousDupeString format col1 & col2 | col3 | col4 & col5
	 * 
	 * @param obviousDupeString
	 * @return
	 */
	
	public C getReverseObviousDupesFilter(ZFrame<D, R, C> df1,ZFrame<D,R,C> dfToJoin, ObviousDupes[] obviousDupes, C extraAndCond) {
		return df1.not(getObviousDupesFilter(df1,dfToJoin,obviousDupes,extraAndCond));
	}

	public Arguments getArgs() {
		return args;
	}

	public void setArgs(Arguments args) {
		this.args = args;
	}

	public Context<S, D, R, C, T> getContext() {
		return context;
	}

	public void setContext(Context<S, D, R, C, T> context) {
		this.context = context;
	}

}
