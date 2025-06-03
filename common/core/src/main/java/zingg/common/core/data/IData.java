package zingg.common.core.data;

import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;
import zingg.common.core.executor.processunit.IDataProcessor;

import java.util.List;

public interface IData<D, R, C> {
    String getName();
    void setName(String name);
    ZFrame<D, R, C> getByIndex(int index);
    List<ZFrame<D, R, C>> getData();
    long count();
    IData<D, R, C> cache();
    IData<D, R, C> compute(IDataProcessor<D, R, C> dataProcessUnit) throws ZinggClientException, Exception;
}
