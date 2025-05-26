package zingg.common.core.block;

import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;
import zingg.common.core.executor.processunit.IDataProcessor;

public interface IBlocker<S,D,R,C,T> extends IDataProcessor<D, R, C> {
    ZFrame<D, R, C> getBlocked(ZFrame<D,R,C> testData) throws Exception, ZinggClientException;
}
