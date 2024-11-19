package zingg.common.core.executor;

import zingg.common.client.ClientOptions;
import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;
import zingg.common.client.util.DFObjectUtil;
import zingg.common.core.executor.validate.ExecutorValidator;

import java.io.IOException;

public class FtdAndLabelCombinedExecutorTester<S, D, R, C, T> extends MatchThresholdBasedExecutorTester<S, D, R, C, T> {

    private final ZinggBase<S, D, R, C, T> labelExecutor;
    private final ExecutorValidator<S, D, R, C, T> labelValidator;

    //setting labeller properties here
    //ftd properties are already set by super
    public FtdAndLabelCombinedExecutorTester(ZinggBase<S, D, R, C, T> ftdExecutor, ExecutorValidator<S, D, R, C, T> ftdValidator, String configFile,
                                             ZinggBase<S, D, R, C, T> labelExecutor, ExecutorValidator<S, D, R, C, T> labelValidator, String modelId, DFObjectUtil<S,D,R,C> dfObjectUtil) throws ZinggClientException, IOException {
        super(ftdExecutor, ftdValidator,configFile,modelId,dfObjectUtil);
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


    @Override
    protected void run() throws ZinggClientException {
        executor.execute();
        labelExecutor.execute();
        ZFrame<D, R, C> markedRecords = labelExecutor.getMarkedRecords();
        matchCount = getMatchRecordCount(markedRecords);
        notAMatchCount = getNotMatchRecordCount(markedRecords);
        MAX_RUN_COUNT--;
    }

}
