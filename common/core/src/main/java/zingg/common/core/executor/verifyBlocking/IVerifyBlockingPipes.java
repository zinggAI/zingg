package zingg.common.core.executor.verifyblocking;

import zingg.common.client.IArguments;
import zingg.common.client.pipe.Pipe;
import zingg.common.client.util.PipeUtilBase;

public interface IVerifyBlockingPipes<S,D,R,C>  {

    public Pipe<D,R,C> getPipeForVerifyBlockingLocation(IArguments args, PipeUtilBase<S,D,R,C> pipeUtil, long timestamp, String type);

    public Pipe<D, R, C> getCountsPipe(IArguments args, PipeUtilBase<S,D,R,C> pipeUtil, long timestamp);

    public void setCountsPipe(Pipe<D, R, C> countsPipe);

    public Pipe<D, R, C> getBlockSamplesPipe(IArguments args, PipeUtilBase<S,D,R,C> pipeUtil, long timestamp, String type);

    public void setBlockSamplesPipe(Pipe<D, R, C> blockSamplesPipe);

    public long getTimestamp();

    public void setTimestamp(long timestamp);

}


