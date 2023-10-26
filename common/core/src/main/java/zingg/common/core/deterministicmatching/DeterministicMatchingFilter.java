package zingg.common.core.deterministicmatching;

import java.util.HashMap;

import zingg.common.client.DeterministicMatching;
import zingg.common.client.ZFrame;
import zingg.common.client.util.ColName;

public class DeterministicMatchingFilter<D,R,C> {
	
	public DeterministicMatchingFilter() {
	
	}

	/**
	 * Returns a Column filter for the DF given the deterministicMatching condition
	 * 
	 * @param df1 :DF containing the self joined data e.g. fname, z_fname
	 * @param deterministicMatching deterministic matching conditions. Those in one MatchCondition are "AND" condition, will "OR" with other MatchCondition
	 * @param extraAndCond Any extra condition to be applied e.g. z_zid > z_z_id
	 * @return
	 */
	public C getDeterministicMatchingFilter(ZFrame<D, R, C> df1, DeterministicMatching[] deterministicMatching, C extraAndCond) {
		return getDeterministicMatchingFilter(df1,df1,deterministicMatching,extraAndCond);
	}
	/**
	 * Returns a Column filter for the DFs given the deterministicMatching condition
	 * 
	 * @param df1 : 1st DF to join
	 * @param dfToJoin : 2nd DF to join with (the one having cols with z_ as prefix)
	 * @param deterministicMatching deterministic matching conditions. Those in one MatchCondition are "AND" condition, will "OR" with other MatchCondition
	 * @param extraAndCond Any extra condition to be applied e.g. z_zid > z_z_id
	 * @return Column filter for the DFs given the deterministicMatching condition
	 */	
	public C getDeterministicMatchingFilter(ZFrame<D, R, C> df1, ZFrame<D, R, C> dfToJoin, DeterministicMatching[] deterministicMatching, C extraAndCond) {
		
		if (dfToJoin==null || deterministicMatching == null) {
			return null;
		}
		
		
		C filterExpr = getFilterExpr(df1, dfToJoin, deterministicMatching);
		
		filterExpr = addExtraAndCond(df1, extraAndCond, filterExpr);
		
		return filterExpr;
	}

	/**
	 * loop thru the values and build a filter condition
	 * @param df1 : 1st DF to join
	 * @param dfToJoin : 2nd DF to join with (the one having cols with z_ as prefix)
	 * @param deterministicMatching deterministic matching conditions. Those in one MatchCondition are "AND" condition, will "OR" with other MatchCondition
	 * @return Column filter for the DFs given the deterministicMatching condition
	 */
	private C getFilterExpr(ZFrame<D, R, C> df1, ZFrame<D, R, C> dfToJoin, DeterministicMatching[] deterministicMatching) {
		C filterExpr = null;
		
		for (int i = 0; i < deterministicMatching.length; i++) {
			
			C andCond = getAndCondition(df1, dfToJoin, deterministicMatching[i].getMatchCondition());

			filterExpr = addOrCond(df1, filterExpr, andCond);

		}
		return filterExpr;
	}
	/**
	 * Get the AND condition for particular match condition passed
	 * 
	 * @param df1 : 1st DF to join
	 * @param dfToJoin : 2nd DF to join with (the one having cols with z_ as prefix)
	 * @param andConditions : The match condition having various cols to be part of "AND" condition
	 * @return AND condition for particular match condition passed
	 */
	private C getAndCondition(ZFrame<D, R, C> df1, ZFrame<D, R, C> dfToJoin, HashMap<String, String>[] andConditions) {
		C andCond = null;
		if (andConditions != null) {
			for (int j = 0; j < andConditions.length; j++) {
				andCond = getAndCondForCol(df1, dfToJoin, andCond, andConditions[j].get(DeterministicMatching.fieldName));
			}
		}
		return andCond;
	}

	/**
	 * Form the "AND" cond for particular col and add to already existing and cond
	 * 
	 * @param df1 : 1st DF to join
	 * @param dfToJoin : 2nd DF to join with (the one having cols with z_ as prefix)
	 * @param andCond The condition constructed so far before calling for this column
	 * @param colName The col for which condition is required
	 * @return AND condition for particular col passed added to what already is there
	 */
	private C getAndCondForCol(ZFrame<D, R, C> df1, ZFrame<D, R, C> dfToJoin, C andCond, String colName) {
		
		if (colName==null) return null;
		
		C column = df1.col(colName);
		C columnWithPrefix = dfToJoin.col(ColName.COL_PREFIX + colName);

		C eqCond = getEqCond(df1, column, columnWithPrefix);

		andCond = (andCond != null) ? df1.and(andCond, eqCond) : eqCond;
		return andCond;
	}

	/**
	 * Form a condition like x = z_x along with null checks
	 * 
	 * @param df1
	 * @param column
	 * @param columnWithPrefix
	 * @return
	 */
	private C getEqCond(ZFrame<D, R, C> df1, C column, C columnWithPrefix) {
		C eqCond = df1.and(
				df1.and(
						df1.equalTo(column, columnWithPrefix),
						df1.isNotNull(column)
						),
				df1.isNotNull(columnWithPrefix)
				);
		return eqCond;
	}

	/**
	 * Combine multiple match conditions via OR
	 * @param df1
	 * @param filterExpr
	 * @param andCond
	 * @return
	 */
	private C addOrCond(ZFrame<D, R, C> df1, C filterExpr, C andCond) {
		if (andCond != null) {				
			filterExpr = (filterExpr != null) ? df1.or(filterExpr, andCond) : andCond;
		}
		return filterExpr;
	}

	/**
	 * Any extra AND condition like z_zid > z_z_zid is added to existing condition
	 * @param df1
	 * @param extraAndCond
	 * @param filterExpr
	 * @return
	 */
	private C addExtraAndCond(ZFrame<D, R, C> df1, C extraAndCond, C filterExpr) {
		if (extraAndCond != null) {
			filterExpr = (filterExpr != null)  ? df1.and(filterExpr, extraAndCond) :  extraAndCond;			
		}
		return filterExpr;
	}

	/**
	 * Used to filter out deterministic matchings by forming a NOT over deterministic matching filter condition
	 * @param df1
	 * @param deterministicMatching
	 * @param extraAndCond
	 * @return
	 */
	public C getReverseDeterministicMatchingFilter(ZFrame<D, R, C> df1,DeterministicMatching[] deterministicMatching, C extraAndCond) {
		return getReverseDeterministicMatchingFilter(df1,df1,deterministicMatching,extraAndCond);
	}
		
	/**
	 * Used to filter out deterministic matchings by forming a NOT over deterministic matching filter condition
	 * @param df1
	 * @param dfToJoin
	 * @param deterministicMatching
	 * @param extraAndCond
	 * @return
	 */
	public C getReverseDeterministicMatchingFilter(ZFrame<D, R, C> df1,ZFrame<D,R,C> dfToJoin, DeterministicMatching[] deterministicMatching, C extraAndCond) {
		return df1.not(getDeterministicMatchingFilter(df1,dfToJoin,deterministicMatching,extraAndCond));
	}
	
}
