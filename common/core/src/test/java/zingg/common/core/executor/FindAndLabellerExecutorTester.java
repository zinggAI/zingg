package zingg.common.core.executor;

import zingg.common.client.ClientOptions;
import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;
import zingg.common.client.util.DFObjectUtil;
import zingg.common.core.executor.validate.ExecutorValidator;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public class FindAndLabellerExecutorTester<S, D, R, C, T> extends MatchThresholdBasedExecutorTester<S, D, R, C, T> {

    public FindAndLabellerExecutorTester(ZinggBase<S, D, R, C, T> ftdLabelerExecutor, ExecutorValidator<S, D, R, C, T> ftdLabelerValidator, String configFile, String modelId, DFObjectUtil<S, D, R, C> dfObjectUtil) throws ZinggClientException, IOException, InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchMethodException {
        super(ftdLabelerExecutor, ftdLabelerValidator, configFile, modelId, dfObjectUtil);
    }

    //need to execute until we get
    //required number of matches
    @Override
    public void initAndExecute(S session) throws ZinggClientException {
        executor.init(args,session, new ClientOptions());
        runUntilThreshold();
    }

    @Override
    protected void run() throws ZinggClientException {
        executor.execute();
        ZFrame<D, R, C> markedRecords = executor.getMarkedRecords();
        matchCount = getMatchRecordCount(markedRecords);
        notAMatchCount = getNotMatchRecordCount(markedRecords);
        MAX_RUN_COUNT--;
    }
}
