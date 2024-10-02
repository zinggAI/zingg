package zingg.common.core.executor;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;
import zingg.common.client.util.ColName;

public class FindAndLabelTester<S, D, R, C, T> extends ExecutorTester<S, D, R, C, T> {

    public static final Log LOG = LogFactory.getLog(FindAndLabelTester.class);
	
	public FindAndLabelTester(FindAndLabeller<S, D, R, C, T> executor) {
		super(executor);
	}

    @Override
    public void validateResults() throws ZinggClientException {
        // check that unmarked data has at least 10 rows
        ZFrame<D, R, C> df = executor.getContext().getPipeUtil().read(false, false, executor.getContext().getPipeUtil().getTrainingDataUnmarkedPipe(executor.getArgs()));
		
		long trainingDataCount = df.count();
		assertTrue(trainingDataCount > 10);
		LOG.info("trainingDataCount : "+ trainingDataCount);

        // check that marked data has at least 1 match row and 1 unmatch row
		ZFrame<D, R, C> dfMarked = executor.getContext().getPipeUtil().
				read(false, false, executor.getContext().getPipeUtil().getTrainingDataMarkedPipe(executor.getArgs()));
		
		C matchCond = dfMarked.equalTo(ColName.MATCH_FLAG_COL, 1);
		C notMatchCond = dfMarked.equalTo(ColName.MATCH_FLAG_COL, 0);
		
		long matchCount = dfMarked.filter(matchCond).count();
		assertTrue(matchCount > 1);
		long unmatchCount = dfMarked.filter(notMatchCond).count();
		assertTrue(unmatchCount > 1);
		LOG.info("matchCount : "+ matchCount + ", unmatchCount : " + unmatchCount);

    }
    
}
