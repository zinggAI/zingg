package zingg.common.core.executor.validate;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;
import zingg.common.client.util.ColName;
import zingg.common.core.executor.Matcher;

public class MatcherValidator<S, D, R, C, T> extends ExecutorValidator<S, D, R, C, T> {

	public static final Log LOG = LogFactory.getLog(MatcherValidator.class);
	protected int PREFIX_MATCH_LENGTH = 8;
	
	public MatcherValidator(Matcher<S, D, R, C, T> executor) {
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
		df = df.select("fnameId", "id", getClusterColName());
		df = df.withColumn("dupeRecIdFuzzyMatch",df.substr(df.col("id"),0,PREFIX_MATCH_LENGTH)).cache();
		ZFrame<D, R, C> df1 = df.withColumnRenamed("fnameId", "fnameId1").withColumnRenamed("dupeRecIdFuzzyMatch", "dupeRecIdFuzzyMatch1")
							.withColumnRenamed(getClusterColName(), getClusterColName() + "1").cache();
					
		
		ZFrame<D, R, C> gold = joinAndFilter("dupeRecIdFuzzyMatch", df, df1).cache();
		ZFrame<D, R, C> result = joinAndFilter(getClusterColName(), df, df1).cache();

		testAccuracy(gold, result);
	}

	protected void testAccuracy(ZFrame<D, R, C> gold, ZFrame<D, R, C> result) throws ZinggClientException{

		ZFrame<D, R, C> fn = gold.except(result);
		ZFrame<D, R, C> tp = gold.intersect(result);
		ZFrame<D, R, C> fp = result.except(gold);

		long fnCount = fn.count();
		long tpCount = tp.count();
		long fpCount = fp.count();
		double score1 = tpCount*1.0d/(tpCount+fpCount);
		double score2 = tpCount*1.0d/(tpCount+fnCount);
	
		LOG.info("False negative " + fnCount);
		LOG.info("True positive " + tpCount);
		LOG.info("False positive " + fpCount);
		LOG.info("precision " + score1);
		LOG.info("recall " + tpCount + " denom " + (tpCount+fnCount) + " overall " + score2);

		System.out.println("precision score " + score1);
		
		System.out.println("recall score " + score2);
		
		assertTrue(0.7 <= score1);
		assertTrue(0.7 <= score2);

		//assert on f1 score
		assertTrue(0.85 <= 2.0 * score2 / (score1 + score2));
	}


	public ZFrame<D, R, C> getOutputData() throws ZinggClientException {
		ZFrame<D, R, C> output = executor.getContext().getPipeUtil().read(false, false, executor.getArgs().getOutput()[0]);
		return output;
	}
	
	protected ZFrame<D, R, C> joinAndFilter(String colName, ZFrame<D, R, C> df, ZFrame<D, R, C> df1){
		C col1 = df.col(colName);
		C col2 = df1.col(colName + "1");
		ZFrame<D, R, C> joined = df.joinOnCol(df1, df.equalTo(col1, col2));
		return joined.filter(joined.gt(joined.col("fnameId"), joined.col("fnameId1")));
	}

	protected void setPrefixMatchLength(int length) {
		this.PREFIX_MATCH_LENGTH = length;
	}
}
