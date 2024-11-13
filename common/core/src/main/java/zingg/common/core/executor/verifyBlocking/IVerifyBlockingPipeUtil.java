package zingg.common.core.executor.verifyblocking;

import zingg.common.client.IArguments;
import zingg.common.client.pipe.Pipe;
import zingg.common.client.util.PipeUtilBase;

public interface IVerifyBlockingPipeUtil<S,D,R,C>  {

    public Pipe<D,R,C> getPipeForVerifyBlockingLocation(IArguments args, String type);

    public Pipe<D, R, C> getCountsPipe(IArguments args);

    public void setCountsPipe(Pipe<D, R, C> countsPipe);

    public Pipe<D, R, C> getBlockSamplesPipe(IArguments args, String type);

    public void setBlockSamplesPipe(Pipe<D, R, C> blockSamplesPipe);

    public void setTimestamp(long timestamp);

    public void setPipeUtil(PipeUtilBase<S,D,R,C> pipeUtil);

}


