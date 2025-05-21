package zingg.common.core.data;

import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;
import zingg.common.core.executor.processunit.IDataProcessUnit;

import java.util.List;

public interface IData<D, R, C> {
    List<ZFrame<D, R, C>> getData();
    InputType getInputType();
    long count();
    IData<D, R, C> compute(IDataProcessUnit<D, R, C> dataProcessUnit) throws ZinggClientException, Exception;
}
