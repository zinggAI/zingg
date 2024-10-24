package zingg.common.core.block;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import zingg.common.client.IArguments;
import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;
import zingg.common.client.util.PipeUtilBase;

public class InputDataGetter<S,D,R,C> {

    private static final long serialVersionUID = 1L;
	private static final Log LOG = LogFactory.getLog(InputDataGetter.class);

    public InputDataGetter(){
    }

    public ZFrame<D,R,C>  getTestData(PipeUtilBase<S,D,R,C> getPipe, IArguments args) throws ZinggClientException{
		 ZFrame<D,R,C>  data = getPipe.read(true, true, args.getNumPartitions(), true, args.getData());
		return data;
	}
    
}
