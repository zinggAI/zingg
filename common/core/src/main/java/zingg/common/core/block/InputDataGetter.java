package zingg.common.core.block;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import zingg.common.client.IArguments;
import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;
import zingg.common.client.data.GenericData;
import zingg.common.client.data.IData;
import zingg.common.client.util.PipeUtilBase;
import zingg.common.core.match.data.IDataGetter;

public class InputDataGetter<S,D,R,C> implements IDataGetter<S,D,R,C>{

    private static final long serialVersionUID = 1L;
	private static final Log LOG = LogFactory.getLog(InputDataGetter.class);

    PipeUtilBase<S,D,R,C> pipeUtil;

    public InputDataGetter(PipeUtilBase<S,D,R,C> getPipe){
        this.pipeUtil = getPipe;
    }

    public ZFrame<D,R,C>  getTestData(IArguments args) throws ZinggClientException{
		 ZFrame<D,R,C>  data = pipeUtil.read(true, true, args.getNumPartitions(), true, args.getData());
		return data;
	}

    @Override
    public IData<D, R, C> getData(IArguments args, PipeUtilBase<S, D, R, C> p) throws ZinggClientException {
        ZFrame<D, R, C> inputDF = p.read(true, true, args.getNumPartitions(), true, args.getData());
        return new GenericData<D, R, C>(inputDF);
    }
    
}
