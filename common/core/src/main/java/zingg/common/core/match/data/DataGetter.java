package zingg.common.core.match.data;

import zingg.common.client.IArguments;
import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;
import zingg.common.client.model.IInputData;
import zingg.common.client.model.MatchInputData;
import zingg.common.client.util.PipeUtilBase;

public class DataGetter<S,D,R,C> implements IDataGetter<S,D,R,C>{

    @Override
    public IInputData<D, R, C> getData(IArguments args, PipeUtilBase<S, D, R, C> p) throws ZinggClientException{
        ZFrame<D,R,C>  data = p.read(true, true, args.getNumPartitions(), true, args.getData());
        return new MatchInputData<D, R, C>(data);
    }
  
    
}
