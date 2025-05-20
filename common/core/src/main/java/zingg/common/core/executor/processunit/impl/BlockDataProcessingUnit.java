package zingg.common.core.executor.processunit.impl;

import zingg.common.client.IArguments;
import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;
import zingg.common.client.data.BlockedData;
import zingg.common.client.data.IData;
import zingg.common.client.util.IModelHelper;
import zingg.common.core.block.IBlocker;
import zingg.common.core.executor.processunit.IDataProcessUnit;
import zingg.common.core.util.BlockingTreeUtil;

import java.util.ArrayList;
import java.util.List;

public class BlockDataProcessingUnit<S, D, R, C, T> implements IDataProcessUnit<D, R, C> {
    private final IBlocker<S, D, R, C, T> blocker;
    private final IArguments arguments;
    private final IModelHelper<D, R, C> modelHelper;
    private final BlockingTreeUtil<S, D, R, C, T> blockingTreeUtil;

    public BlockDataProcessingUnit(IBlocker<S, D, R, C, T> blocker, IArguments arguments,
                                   IModelHelper<D, R, C> modelHelper, BlockingTreeUtil<S, D, R, C, T> blockingTreeUtil) {
        this.blocker = blocker;
        this.arguments = arguments;
        this.modelHelper = modelHelper;
        this.blockingTreeUtil = blockingTreeUtil;
    }

    @Override
    public BlockedData<D, R, C> process(IData<D, R, C> data) throws ZinggClientException, Exception {
        List<ZFrame<D, R, C>> blockedZFrames = new ArrayList<>();
        for (ZFrame<D, R, C> zFrame : data.getData()) {
            ZFrame<D, R, C> currentBlocked = blocker.getBlocked(zFrame, arguments, modelHelper, blockingTreeUtil);
            blockedZFrames.add(currentBlocked);
        }
        return new BlockedData<>(blockedZFrames);
    }
}
