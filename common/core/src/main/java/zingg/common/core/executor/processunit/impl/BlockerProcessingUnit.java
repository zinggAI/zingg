package zingg.common.core.executor.processunit.impl;

import zingg.common.client.IArguments;
import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;
import zingg.common.client.util.IModelHelper;
import zingg.common.core.block.IBlocker;
import zingg.common.core.executor.processunit.IDataProcessUnit;
import zingg.common.core.util.BlockingTreeUtil;

public class BlockerProcessingUnit<S, D, R, C, T> implements IDataProcessUnit<D, R, C> {
    private final IBlocker<S, D, R, C, T> blocker;
    private final IArguments arguments;
    private final IModelHelper<D, R, C> modelHelper;
    private final BlockingTreeUtil<S, D, R, C, T> blockingTreeUtil;

    public BlockerProcessingUnit(IBlocker<S, D, R, C, T> blocker, IArguments arguments,
                                 IModelHelper<D, R, C> modelHelper, BlockingTreeUtil<S, D, R, C, T> blockingTreeUtil) {
        this.blocker = blocker;
        this.arguments = arguments;
        this.modelHelper = modelHelper;
        this.blockingTreeUtil = blockingTreeUtil;
    }

    @Override
    public ZFrame<D, R, C> process(ZFrame<D, R, C> data) throws ZinggClientException, Exception {
        return blocker.getBlocked(data, arguments, modelHelper, blockingTreeUtil);
    }
}
