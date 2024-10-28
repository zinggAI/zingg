package zingg.common.core.executor.verifyblocking;

import zingg.common.client.IArguments;
import zingg.common.client.pipe.Pipe;
import zingg.common.client.util.PipeUtilBase;

public interface IVerifyBlockingPipes<S,D,R,C>  {

    public Pipe<D,R,C> getPipeForVerifyBlockingLocation(IArguments args, PipeUtilBase<S,D,R,C> pipeUtil, long timestamp, String type);
    
}


