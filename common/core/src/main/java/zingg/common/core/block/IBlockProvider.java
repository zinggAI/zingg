package zingg.common.core.block;

import zingg.common.client.IArguments;
import zingg.common.client.ZinggClientException;
import zingg.common.client.data.BlockedData;
import zingg.common.client.data.IData;
import zingg.common.client.util.IModelHelper;
import zingg.common.core.util.BlockingTreeUtil;

public interface IBlockProvider<S, D, R, C, T> {
    BlockedData<D, R, C>[] getBlockedData(IData<D, R, C> testData, IArguments args, IModelHelper<D, R, C> modelHelper, BlockingTreeUtil<S, D,R,C,T> blockingTreeUtil) throws ZinggClientException, Exception;
}
