package zingg.common.core.executor.blockingverifier;

import zingg.common.client.IArguments;
import zingg.common.client.pipe.Pipe;
import zingg.common.client.util.IModelHelper;
import zingg.common.client.util.PipeUtilBase;

public interface IVerifyBlockingPipes<S,D,R,C>  {

    public Pipe<D,R,C> getPipeForVerifyBlockingLocation(IArguments args, String type);

    public Pipe<D, R, C> getCountsPipe(IArguments args);

    public void setCountsPipe(Pipe<D, R, C> countsPipe);

    public Pipe<D, R, C> getBlockSamplesPipe(IArguments args, String type);

    public void setBlockSamplesPipe(Pipe<D, R, C> blockSamplesPipe);

    public void setTimestamp(long timestamp);

    public void setPipeUtil(PipeUtilBase<S,D,R,C> pipeUtil);

    public IModelHelper<D,R, C> getModelHelper();

    public void setModelHelper(IModelHelper<D,R,C> imh);

}



