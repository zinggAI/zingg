package zingg.common.core.executor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import zingg.common.client.Arguments;
import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;
import zingg.common.client.util.ColName;
import zingg.common.client.util.ColValues;
import zingg.common.core.Context;

public class ObvDupeFilter<S,D,R,C,T> extends ZinggBase<S, D, R, C, T> {
	
	private static final long serialVersionUID = 1L;
	public static final Log LOG = LogFactory.getLog(ObvDupeFilter.class); 

	public static final String orSeperator = "\\|";	
	public static final String andSeperator = "\\&";	

	
	public ObvDupeFilter(Context<S,D,R,C,T> context, Arguments args) {
		setContext(context);
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
	 * 23		3		1.0				1.0
	 * 
	 * 
	 * @param blocked
	 * @return
	 */
	public ZFrame<D, R, C> getObvDupePairs(ZFrame<D, R, C> blocked) {
		
		String obviousDupeString = getArgs().getObviousDupeCondition();
		
		if (obviousDupeString == null || obviousDupeString.trim().isEmpty()) {
			return null;
		}

		ZFrame<D,R,C> prefixBlocked = getDSUtil().getPrefixedColumnsDS(blocked);		
		C gtCond = blocked.gt(prefixBlocked,ColName.ID_COL);
		
		ZFrame<D,R,C> onlyIds = null;
		
		// split on || (orSeperator)
		String[] obvDupeORConditions = obviousDupeString.trim().split(orSeperator);		
		// loop thru the values and build a filter condition		
		for (int i = 0; i < obvDupeORConditions.length; i++) {		
			
			C obvDupeDFFilter = getObviousDupesFilter(blocked,prefixBlocked,obvDupeORConditions[i],gtCond);
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
		String obviousDupeString = getArgs().getObviousDupeCondition();
		if (obviousDupeString == null || obviousDupeString.trim().isEmpty()) {
			return blocks;
		}
		C reverseOBVDupeDFFilter = getReverseObviousDupesFilter(blocks,obviousDupeString,null);
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
	public C getObviousDupesFilter(ZFrame<D, R, C> df1, String obviousDupeString, C extraAndCond) {
		return getObviousDupesFilter(df1,df1,obviousDupeString,extraAndCond);
	}
	
	/**
	 * 
	 * obviousDupeString format col1 & col2 | col3 | col4 & col5
	 * 
	 * @param obviousDupeString
	 * @return
	 */
	
	public C getObviousDupesFilter(ZFrame<D, R, C> df1, ZFrame<D, R, C> dfToJoin, String obviousDupeString, C extraAndCond) {
		
		if (dfToJoin==null || obviousDupeString == null || obviousDupeString.trim().isEmpty()) {
			return null;
		}
		
		// split on || (orSeperator)
		String[] obvDupeORConditions = new String[] {};
		
		obvDupeORConditions = obviousDupeString.trim().split(orSeperator);

		// loop thru the values and build a filter condition
		C filterExpr = null;
		
		for (int i = 0; i < obvDupeORConditions.length; i++) {
			
			// parse on &(andSeperator) for obvDupeCond[i] and form a column filter
			// expression [keep adding to filterExpr]
			// if number of columns in and condition = 1 => something like uid or ssn =>
			// direct match if equal
			C andCond = null;
			String orCondStr = obvDupeORConditions[i];
			
			if (orCondStr != null && !orCondStr.isEmpty()) {

				String[] andConditions = orCondStr.trim().split(andSeperator);

				if (andConditions != null) {
					for (int j = 0; j < andConditions.length; j++) {

						String andCondStr = andConditions[j];

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

							if (andCond != null) {
								andCond = df1.and(andCond, eqCond);
							} else {
								andCond = eqCond;
							}

						}
					}
				}
			}

			if (andCond != null) {
				if (filterExpr != null) {
					filterExpr = df1.or(filterExpr, andCond);
				} else {
					filterExpr = andCond;
				}
			}

		}
		
		if (extraAndCond != null) {
			if (filterExpr != null) {
				filterExpr = df1.and(filterExpr, extraAndCond);
			} else {
				filterExpr = extraAndCond;
			}
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
	
	public C getReverseObviousDupesFilter(ZFrame<D, R, C> df1,String obviousDupeString, C extraAndCond) {
		return getReverseObviousDupesFilter(df1,df1,obviousDupeString,extraAndCond);
	}
		
	/**
	 * 
	 * obviousDupeString format col1 & col2 | col3 | col4 & col5
	 * 
	 * @param obviousDupeString
	 * @return
	 */
	
	public C getReverseObviousDupesFilter(ZFrame<D, R, C> df1,ZFrame<D,R,C> dfToJoin, String obviousDupeString, C extraAndCond) {
		return df1.not(getObviousDupesFilter(df1,dfToJoin,obviousDupeString,extraAndCond));
	}
	
	
	@Override
	public void execute() throws ZinggClientException {
		throw new UnsupportedOperationException();		
	}

}
