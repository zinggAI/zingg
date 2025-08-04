package zingg.common.core.executor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;
import zingg.common.client.util.DFObjectUtil;
import zingg.common.core.ZinggException;
import zingg.common.core.executor.validate.ExecutorValidator;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public abstract class MatchThresholdBasedExecutorTester<S, D, R, C, T> extends ExecutorTester<S, D, R, C, T>{

    private static final Log LOG = LogFactory.getLog(MatchThresholdBasedExecutorTester.class);
    //max how many times we want to run
    //to avoid infinite loops in rare cases
    protected int MAX_RUN_COUNT = 6;
    protected int REQUIRED_MATCH_COUNT = 20;
    protected int REQUIRED_NOT_A_MATCH_COUNT = 20;
    protected long matchCount = 0;
    protected long notAMatchCount = 0;

    public MatchThresholdBasedExecutorTester(ZinggBase<S, D, R, C, T> executor, ExecutorValidator<S, D, R, C, T> validator, String configFile, String modelId, DFObjectUtil<S, D, R, C> dfObjectUtil)
            throws ZinggClientException, IOException, InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchMethodException {
        super(executor, validator, configFile, modelId, dfObjectUtil);
    }

    protected void runUntilThreshold() {
        try {
            //run until either max runs are exhausted
            //or match count and notAMatchCount
            //reaches threshold value
            while(MAX_RUN_COUNT > 0 && (matchCount < REQUIRED_MATCH_COUNT || notAMatchCount < REQUIRED_NOT_A_MATCH_COUNT)) {
                run();
            }
            LOG.info("total number of matches discovered, " + matchCount);
            LOG.info("total number of non-matches discovered, " + notAMatchCount);
        } catch (Exception | ZinggClientException exception) {
            throw new ZinggException("Exception occurred while running threshold based runner, " + exception.getMessage());
        }
    }

    protected long getMatchRecordCount(ZFrame<D, R, C> markedRecords) {
        return markedRecords.filter(markedRecords.equalTo("z_isMatch", 1.0)).count();
    }

    protected long getNotMatchRecordCount(ZFrame<D, R, C> markedRecords) {
        return markedRecords.filter(markedRecords.equalTo("z_isMatch", 0.0)).count();
    }

    protected void setRequiredMatchCount(int count) {
        this.REQUIRED_MATCH_COUNT = count;
    }
    protected void setRequiredNotAMatchCount(int count) {
        this.REQUIRED_NOT_A_MATCH_COUNT = count;
    }
    protected void setMaxRunCount(int count) {
        this.MAX_RUN_COUNT = count;
    }

    protected abstract void run() throws ZinggClientException;
}
