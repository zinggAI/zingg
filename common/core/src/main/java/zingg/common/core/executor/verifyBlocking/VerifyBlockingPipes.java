package zingg.common.core.executor.verifyblocking;

import zingg.common.client.IArguments;
import zingg.common.client.pipe.FilePipe;
import zingg.common.client.pipe.Pipe;
import zingg.common.client.util.PipeUtilBase;

public class VerifyBlockingPipes<S,D,R,C> implements IVerifyBlockingPipeUtil<S,D,R,C> {

    protected PipeUtilBase<S,D,R,C> pipeUtil;
    protected long timestamp;
    Pipe<D,R,C> countsPipe;
    Pipe<D,R,C> blockSamplesPipe; 
    
    public VerifyBlockingPipes(PipeUtilBase<S,D,R,C> pipeUtil, long timestamp) {
    	setPipeUtil(pipeUtil);
        setTimestamp(timestamp);
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
    public Pipe<D,R,C> getPipeForVerifyBlockingLocation(IArguments args, String type){
        Pipe<D,R,C> p = new Pipe<D,R,C>();
		p.setFormat(Pipe.FORMAT_PARQUET);
		p.setProp(FilePipe.LOCATION, getName(args,timestamp,type));
		p = pipeUtil.setOverwriteMode(p);
		return p;
    }

    private String getName(IArguments args, long timestamp, String type){
        return args.getZinggModelDir() + "/blocks/" + timestamp + "/" + type;
    }


    @Override
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }


    @Override
    public void setPipeUtil(PipeUtilBase<S, D, R, C> pipeUtil) {
        this.pipeUtil = pipeUtil;
    }

    
}
