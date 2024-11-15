package zingg.common.core.executor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import zingg.common.client.ClientOptions;
import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;
import zingg.common.core.executor.validate.ExecutorValidator;

import java.io.IOException;

public class FtdLabelCombinedExecutorTester<S, D, R, C, T> extends ExecutorTester<S, D, R, C, T> {

    private static final Log LOG = LogFactory.getLog(FtdLabelCombinedExecutorTester.class);
    private final ZinggBase<S, D, R, C, T> labelExecutor;
    private final ExecutorValidator<S, D, R, C, T> labelValidator;
    //max how many times we want to run
    //to avoid infinite loops in rare cases
    private int MAX_RUN_COUNT = 6;
    private final static int REQUIRED_MATCH_COUNT = 20;
    private final static int REQUIRED_NOT_A_MATCH_COUNT = 20;

    //setting labeller properties here
    //ftd properties are already set by super
    public FtdLabelCombinedExecutorTester(ZinggBase<S, D, R, C, T> ftdExecutor, ExecutorValidator<S, D, R, C, T> ftdValidator, String configFile,
                                          ZinggBase<S, D, R, C, T> labelExecutor, ExecutorValidator<S, D, R, C, T> labelValidator) throws ZinggClientException, IOException {
        super(ftdExecutor, ftdValidator, configFile);
        this.labelExecutor = labelExecutor;
        this.labelValidator = labelValidator;
    }

    //need to execute until we get
    //required number of matches
    @Override
    public void initAndExecute(S session) throws ZinggClientException {
        executor.init(args,session, new ClientOptions());
        labelExecutor.init(args, session, new ClientOptions());
        runUntilThreshold();
    }

    @Override
    public void validateResults() throws ZinggClientException {
        validator.validateResults();
        labelValidator.validateResults();
    }

    //run until max run count reached or
    //required match count reached, whichever
    //reaches earlier
    private void runUntilThreshold() throws ZinggClientException {
        long matchCount = 0;
        long notAMatchCount = 0;
        while(MAX_RUN_COUNT > 0 && (matchCount < REQUIRED_MATCH_COUNT || notAMatchCount < REQUIRED_NOT_A_MATCH_COUNT)) {
            executor.execute();
            labelExecutor.execute();
            ZFrame<D, R, C> markedRecords = labelExecutor.getMarkedRecords();
            matchCount = getMatchRecordCount(markedRecords);
            notAMatchCount = getNotMatchRecordCount(markedRecords);
            MAX_RUN_COUNT--;
        }
        LOG.info("total number of matches discovered, " + matchCount);
        LOG.info("total number of non-matches discovered, " + notAMatchCount);
    }

    private long getMatchRecordCount(ZFrame<D, R, C> markedRecords) {
        return markedRecords.filter(markedRecords.equalTo("z_isMatch", 1.0)).count();
    }

    private long getNotMatchRecordCount(ZFrame<D, R, C> markedRecords) {
        return markedRecords.filter(markedRecords.equalTo("z_isMatch", 0.0)).count();
    }

}
