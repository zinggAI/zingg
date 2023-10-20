package zingg.common.core.obviousdupes;

import java.util.HashMap;

import zingg.common.client.ObviousDupes;
import zingg.common.client.ZFrame;
import zingg.common.client.util.ColName;
import zingg.common.client.util.ColValues;

public class ObvDupeFilterHelper<S,D,R,C,T> {
	
	public ObvDupeFilterHelper() {
	
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

	protected ZFrame<D,R,C>massageAllEquals(ZFrame<D,R,C>allEqual) {
		allEqual = allEqual.withColumn(ColName.PREDICTION_COL, ColValues.IS_MATCH_PREDICTION);
		allEqual = allEqual.withColumn(ColName.SCORE_COL, ColValues.FULL_MATCH_SCORE);
		return allEqual;
	}
	
}
