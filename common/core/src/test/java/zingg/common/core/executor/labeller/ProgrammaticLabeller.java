package zingg.common.core.executor.labeller;

import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;
import zingg.common.client.options.ZinggOptions;
import zingg.common.client.util.ColName;
import zingg.common.client.util.ColValues;
import zingg.common.client.util.DFObjectUtil;
import zingg.common.core.context.Context;
import zingg.common.core.executor.Labeller;

public class ProgrammaticLabeller<S,D,R,C,T> extends Labeller<S,D,R,C,T> {
	
	private static final long serialVersionUID = 1L;
	
	public ProgrammaticLabeller(Context<S,D,R,C,T> context) {
		setZinggOption(ZinggOptions.LABEL);
		setContext(context);
	}

	@Override
	public ZFrame<D, R, C> processRecordsCli(ZFrame<D, R, C> lines)
			throws ZinggClientException {
		
		// now get a list of all those rows which have same cluster and match due to fname => mark match
		ZFrame<D, R, C> lines2 = getDSUtil().getPrefixedColumnsDS(lines);
		
		// construct AND condition
		C clusterCond = getJoinCondForCol(lines, lines2, ColName.CLUSTER_COLUMN,true);
		C fnameCond = getJoinCondForCol(lines, lines2, "FNAME",true);
		C idCond = getJoinCondForCol(lines, lines2, "ID",false);
		C filterCond = lines2.and(lines2.and(clusterCond,idCond),fnameCond);
		
		ZFrame<D, R, C> filtered = lines.joinOnCol(lines2, filterCond).cache();
		
		ZFrame<D, R, C> matches = filtered.select(ColName.CLUSTER_COLUMN).distinct().withColumn(ColName.MATCH_FLAG_COL, ColValues.IS_MATCH_PREDICTION).cache();

		ZFrame<D, R, C> nonMatches = lines.select(ColName.CLUSTER_COLUMN).except(matches.select(ColName.CLUSTER_COLUMN)).distinct().withColumn(ColName.MATCH_FLAG_COL, ColValues.IS_NOT_A_MATCH_PREDICTION).cache();
		
		ZFrame<D, R, C> all = matches.unionAll(nonMatches);
		
		ZFrame<D, R, C> linesMatched = lines;
		linesMatched = linesMatched.drop(ColName.MATCH_FLAG_COL);
		linesMatched = linesMatched.joinOnCol(all, ColName.CLUSTER_COLUMN);
		linesMatched = linesMatched.select(lines.columns()); // make same order
		
		return linesMatched;
	}

	@Override
	protected DFObjectUtil<S, D, R, C> getDfObjectUtil() {
		// won't be needed during test runs
		return null;
	}

	private C getJoinCondForCol(ZFrame<D, R, C> df1, ZFrame<D, R, C> dfToJoin,String colName, boolean equal) {
		C column = df1.col(colName);
		C columnWithPrefix = dfToJoin.col(ColName.COL_PREFIX + colName);
		C equalTo = df1.equalTo(column,columnWithPrefix);
		if (equal) {
			return equalTo;
		} else {
			return df1.not(equalTo);
		}
	}

	
}
