package zingg.common.core.executor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import zingg.common.client.ClientOptions;
import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;
import zingg.common.core.executor.validate.ExecutorValidator;

import java.io.IOException;

public class FtdMatchCombinedExecutorTester<S, D, R, C, T> extends ExecutorTester<S, D, R, C, T> {

    private static final Log LOG = LogFactory.getLog(FtdMatchCombinedExecutorTester.class);
    private final ZinggBase<S, D, R, C, T> matchExecutor;
    private final ExecutorValidator<S, D, R, C, T> matchValidator;
    //max how many times we want to run
    //to avoid infinite loops in rare cases
    private int MAX_RUN_COUNT = 15;
    private int REQUIRED_MATCH_COUNT = 10;

    //setting matcher properties here
    //ftd properties are already set by super
    public FtdMatchCombinedExecutorTester(ZinggBase<S, D, R, C, T> ftdExecutor, ExecutorValidator<S, D, R, C, T> ftdValidator, String configFile,
                                          ZinggBase<S, D, R, C, T> matchExecutor, ExecutorValidator<S, D, R, C, T> matchValidator) throws ZinggClientException, IOException {
        super(ftdExecutor, ftdValidator, configFile);
        this.matchExecutor = matchExecutor;
        this.matchValidator = matchValidator;
    }

    //need to execute until we get
    //required number of matches
    @Override
    public void initAndExecute(S session) throws ZinggClientException {
        executor.init(args,session, new ClientOptions());
        matchExecutor.init(args, session, new ClientOptions());
        runUntilThreshold();
    }

    @Override
    public void validateResults() throws ZinggClientException {
        validator.validateResults();
        matchValidator.validateResults();
    }

    //run until max run count reached or
    //required match count reached, whichever
    //reaches earlier
    private void runUntilThreshold() throws ZinggClientException {
        long matchCount = 0;
        while(MAX_RUN_COUNT > 0 && matchCount < REQUIRED_MATCH_COUNT) {
            executor.execute();
            matchExecutor.execute();
            ZFrame<D, R, C> markedRecords = matchExecutor.getMarkedRecords();
            matchCount += getMatchRecordCount(markedRecords);
            MAX_RUN_COUNT--;
        }
        LOG.info("total number of matches discovered, " + matchCount);
    }

    private long getMatchRecordCount(ZFrame<D, R, C> markedRecords) {
        return markedRecords.filter(markedRecords.equalTo("z_isMatch", 1.0)).count();
    }

}
