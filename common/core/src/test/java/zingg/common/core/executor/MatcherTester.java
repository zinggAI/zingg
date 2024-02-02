package zingg.common.core.executor;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;
import zingg.common.client.util.ColName;

public abstract class MatcherTester<S, D, R, C, T> extends ExecutorTester<S, D, R, C, T> {

	public static final Log LOG = LogFactory.getLog(MatcherTester.class);
	
	public MatcherTester(Matcher<S, D, R, C, T> executor) {
		super(executor);
	}

	@Override
	public void validateResults() throws ZinggClientException {
		assessAccuracy();		
	}
	
	public String getClusterColName() {
		return ColName.CLUSTER_COLUMN;	
	}

	protected void assessAccuracy() throws ZinggClientException {
		ZFrame<D, R, C> df  = getOutputData();
		
		df = df.withColumn("fnameId",df.concat(df.col("fname"), df.col("id")));
		df = df.select("fnameId", getClusterColName());
		df = df.withColumn("dupeFnameId",substr(df,df.col("fnameId"),0,8)).cache();
		ZFrame<D, R, C> df1 = df.withColumnRenamed("fnameId", "fnameId1").withColumnRenamed("dupeFnameId", "dupeFnameId1")
							.withColumnRenamed(getClusterColName(), getClusterColName() + "1").cache();
					
		
		ZFrame<D, R, C> gold = joinAndFilter("dupeFnameId", df, df1).cache();
		ZFrame<D, R, C> result = joinAndFilter(getClusterColName(), df, df1).cache();

		ZFrame<D, R, C> fn = gold.except(result);
		ZFrame<D, R, C> tp = intersect(gold,result);
		ZFrame<D, R, C> fp = result.except(gold);

		long fnCount = fn.count();
		long tpCount = tp.count();
		long fpCount = fp.count();
		
		LOG.info("False negative " + fnCount);
		LOG.info("True positive " + tpCount);
		LOG.info("False positive " + fpCount);
		LOG.info("precision " + (tpCount*1.0d/(tpCount+fpCount)));
		LOG.info("recall " + tpCount + " denom " + (tpCount+fnCount) + " overall " + (tpCount*1.0d/(tpCount+fnCount)));

		assertTrue(0.8 < (tpCount*1.0d/(tpCount+fpCount)));
		assertTrue(0.8 < (tpCount*1.0d/(tpCount+fnCount)));
	}

	public ZFrame<D, R, C> getOutputData() throws ZinggClientException {
		ZFrame<D, R, C> output = executor.getContext().getPipeUtil().read(false, false, executor.getArgs().getOutput()[0]);
		return output;
	}
	
	protected ZFrame<D, R, C> joinAndFilter(String colName, ZFrame<D, R, C> df, ZFrame<D, R, C> df1){
		C col1 = df.col(colName);
		C col2 = df1.col(colName+"1");
		ZFrame<D, R, C> joined = df.joinOnCol(df1, df.equalTo(col1, col2));
		return joined.filter(gt(joined, joined.col("fnameId"), joined.col("fnameId1")));
	}
	
	
	// returns df1.intersect(df2)
	public abstract ZFrame<D, R, C> intersect(ZFrame<D, R, C> df1, ZFrame<D, R, C> df2);
	
	// return df1.substr(col,startPos,len)
	public abstract C substr(ZFrame<D, R, C> df1, C col, int startPos, int len);
	
	// return df1.gt(c1, c2)
	public abstract C gt(ZFrame<D, R, C> df1, C column1, C column2);

}
