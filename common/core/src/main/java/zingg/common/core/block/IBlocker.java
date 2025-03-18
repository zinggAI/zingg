package zingg.common.core.block;

import zingg.common.client.IArguments;
import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;
import zingg.common.client.data.BlockedData;
import zingg.common.client.util.IModelHelper;
import zingg.common.core.util.BlockingTreeUtil;

public interface IBlocker<S,D,R,C,T> {

    BlockedData<D, R, C> getBlocked(ZFrame<D,R,C> testData, IArguments args, IModelHelper<D,R,C> imh, BlockingTreeUtil<S,D,R,C,T> bTreeUtil) throws Exception, ZinggClientException;
    
    
}
