package zingg.common.core.match.data;

import zingg.common.client.IArguments;
import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;
import zingg.common.core.data.IDataImpl;
import zingg.common.core.data.IData;
import zingg.common.client.util.PipeUtilBase;

import java.util.ArrayList;
import java.util.List;

public class DataGetter<S,D,R,C> implements IDataGetter<S,D,R,C>{
    @Override
    public IData<D, R, C> getData(IArguments args, PipeUtilBase<S, D, R, C> p) throws ZinggClientException{
        ZFrame<D,R,C>  data = p.read(true, true, args.getNumPartitions(), true, args.getData());
        return new IDataImpl<D, R, C>(new ArrayList<>(List.of(data)));
    }
  
    
}
