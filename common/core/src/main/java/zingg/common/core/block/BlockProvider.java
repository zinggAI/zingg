package zingg.common.core.block;

import zingg.common.client.IArguments;
import zingg.common.client.ZinggClientException;
import zingg.common.client.data.BlockedData;
import zingg.common.client.data.IData;
import zingg.common.client.data.InputType;
import zingg.common.client.data.LinkInputData;
import zingg.common.client.util.IModelHelper;
import zingg.common.core.util.BlockingTreeUtil;

public class BlockProvider<S, D, R, C, T> implements IBlockProvider<S, D, R, C, T> {

    private final IBlocker<S,D,R,C,T> blocker;

    public BlockProvider(IBlocker<S,D,R,C,T> blocker) {
        this.blocker = blocker;
    }

    @Override
    public BlockedData<D, R, C>[] getBlockedData(IData<D, R, C> testData, IArguments args, IModelHelper<D, R, C> modelHelper,
                                                 BlockingTreeUtil<S, D, R, C, T> blockingTreeUtil) throws ZinggClientException, Exception {

        if (InputType.SINGLE.equals(testData.getInputType())) {
            return new BlockedData[]{blocker.getBlocked(testData.getData(), args, modelHelper, blockingTreeUtil)};
        } else {
            BlockedData<D, R, C> blockedDataOne = blocker.getBlocked(((LinkInputData<D, R, C>)testData).getPrimaryInput(), args, modelHelper, blockingTreeUtil);
            BlockedData<D, R, C> blockedDataTwo = blocker.getBlocked(((LinkInputData<D, R, C>)testData).getSecondaryInput(), args, modelHelper, blockingTreeUtil);
            return new BlockedData[]{blockedDataOne, blockedDataTwo};
        }

    }
}
