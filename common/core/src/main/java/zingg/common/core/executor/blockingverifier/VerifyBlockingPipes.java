package zingg.common.core.executor.blockingverifier;

import zingg.common.client.IArguments;
import zingg.common.client.pipe.Pipe;
import zingg.common.client.util.IModelHelper;
import zingg.common.client.util.PipeUtilBase;

public abstract class VerifyBlockingPipes<S,D,R,C> implements IVerifyBlockingPipes<S,D,R,C> {

    protected PipeUtilBase<S,D,R,C> pipeUtil;
    protected long timestamp;
    Pipe<D,R,C> countsPipe;
    Pipe<D,R,C> blockSamplesPipe; 
    IModelHelper<D,R,C> modelHelper;
    

    public VerifyBlockingPipes(PipeUtilBase<S,D,R,C> pipeUtil, long timestamp, IModelHelper<D, R, C> mh) {
    	setPipeUtil(pipeUtil);
        setTimestamp(timestamp);
        setModelHelper(mh);
    }


    @Override
    public Pipe<D, R, C> getCountsPipe(IArguments args) {
        if(countsPipe == null) {
            countsPipe = getPipeForVerifyBlockingLocation(args, "counts");
        }
        return countsPipe;
    }

    @Override
    public void setCountsPipe(Pipe<D, R, C> countsPipe) {
        this.countsPipe = countsPipe;
    }

    @Override
    public Pipe<D, R, C> getBlockSamplesPipe(IArguments args, String type) {
        blockSamplesPipe = getPipeForVerifyBlockingLocation(args, type);
        return blockSamplesPipe;
    }


    @Override
    public void setBlockSamplesPipe(Pipe<D, R, C> blockSamplesPipe) {
        this.blockSamplesPipe = blockSamplesPipe;
    }

    @Override
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public PipeUtilBase<S,D,R,C> getPipeUtil(){
        return pipeUtil;
    }


    @Override
    public void setPipeUtil(PipeUtilBase<S, D, R, C> pipeUtil) {
        this.pipeUtil = pipeUtil;
    }

    @Override
    public IModelHelper<D, R, C> getModelHelper() {
        return this.modelHelper;
    }

    @Override
    public void setModelHelper(IModelHelper<D, R, C> mh) {
        this.modelHelper = mh;
    }

    
}
