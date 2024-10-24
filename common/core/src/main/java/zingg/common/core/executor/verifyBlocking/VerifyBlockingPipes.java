package zingg.common.core.executor.verifyblocking;

import zingg.common.client.IArguments;
import zingg.common.client.pipe.FilePipe;
import zingg.common.client.pipe.Pipe;
import zingg.common.client.util.PipeUtilBase;

public class VerifyBlockingPipes<S,D,R,C>  {

    public VerifyBlockingPipes(){
    }

    public Pipe<D,R,C> getPipeForVerifyBlockingLocation(IArguments args, PipeUtilBase<S,D,R,C> pipeUtil, long timestamp, String type){
        Pipe<D, R, C> p = new Pipe<D, R, C>();
		p.setFormat(Pipe.FORMAT_PARQUET);
		p.setProp(FilePipe.LOCATION, getName(args,timestamp,type));
		p = pipeUtil.setOverwriteMode(p);
		return p;
    }    

    private String getName(IArguments args, long timestamp, String type){
        return args.getZinggModelDir() + "/blocks/" + timestamp + "/" + type;
    }
    
}


