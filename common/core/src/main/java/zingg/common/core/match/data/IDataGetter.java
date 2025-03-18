package zingg.common.core.match.data;

import zingg.common.client.IArguments;
import zingg.common.client.ZinggClientException;
import zingg.common.client.model.AData;
import zingg.common.client.util.PipeUtilBase;

public interface IDataGetter<S,D,R,C>{
    AData<D, R, C> getData(IArguments arg, PipeUtilBase<S,D,R,C> p) throws ZinggClientException;
}
