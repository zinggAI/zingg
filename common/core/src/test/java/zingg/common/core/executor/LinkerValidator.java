package zingg.common.core.executor;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;

public class LinkerValidator<S, D, R, C, T> extends MatcherValidator<S, D, R, C, T> {

    public static final Log LOG = LogFactory.getLog(LinkerValidator.class);
	
	public LinkerValidator(Matcher<S, D, R, C, T> executor) {
		super(executor);
	}

	@Override
	public void validateResults() throws ZinggClientException {
		assessAccuracy();		
	}

    @Override
	protected void assessAccuracy() throws ZinggClientException {
		ZFrame<D, R, C> df1  = getOutputData().withColumn("z_zsource", "test1");
		df1 = df1.select("z_zsource", getClusterColName());
		
		ZFrame<D, R, C> df2 = getOutputData().withColumn("z_zsource", "test2");
        df2 = df2.select("z_zsource", getClusterColName());
					
		ZFrame<D, R, C> gold = joinAndFilter("z_zsource", df1, df2).cache();
		ZFrame<D, R, C> result = joinAndFilter(getClusterColName(), df1, df2).cache();

        testAccuracy(gold, result);	
	}

	@Override
	protected ZFrame<D, R, C> joinAndFilter(String colName, ZFrame<D, R, C> df, ZFrame<D, R, C> df1){
		C col1 = df.col(colName);
		C col2 = df1.col(colName);
		ZFrame<D, R, C> joined = df.joinOnCol(df1, df.equalTo(col1, col2));
		return joined;
	}
    
}
