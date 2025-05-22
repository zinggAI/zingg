package zingg.common.core.executor.processunit;

import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;

public interface IDataProcessor<D, R, C> {
    ZFrame<D, R, C> process(ZFrame<D, R, C> data) throws ZinggClientException, Exception;
}
