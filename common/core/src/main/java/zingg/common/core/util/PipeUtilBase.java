package zingg.common.core.util;

import zingg.common.client.Arguments;
import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;
import zingg.common.client.pipe.Pipe;

//spark session 
//dataset
public interface PipeUtilBase<S, D, R, C> {
	

	public ZFrame<D, R, C> readInternal(Pipe<D, R, C> p, boolean addSource) throws ZinggClientException;

	public  ZFrame<D, R, C> readInternal(boolean addLineNo,
		boolean addSource, Pipe<D, R, C>... pipes) throws ZinggClientException;

	public  ZFrame<D, R, C> read(boolean addLineNo, boolean addSource, Pipe<D, R, C>... pipes) 
		throws ZinggClientException;

	
	public  ZFrame<D, R, C> read(boolean addLineNo, int numPartitions,
		boolean addSource, Pipe<D, R, C>... pipes)
		throws ZinggClientException;


	public void write(ZFrame<D, R, C> toWriteOrig, Arguments args, Pipe<D, R, C>... pipes)
		throws ZinggClientException;

	
	public Pipe<D, R, C> getTrainingDataUnmarkedPipe(Arguments args);

	public Pipe<D, R, C> getTrainingDataMarkedPipe(Arguments args);
	
	public Pipe<D, R, C> getModelDocumentationPipe(Arguments args);
	
	public Pipe<D, R, C> getBlockingTreePipe(Arguments args);

	public Pipe<D, R, C> getStopWordsPipe(Arguments args, String string);

	public String getPipesAsString(Pipe<D,R,C>[] pipes);

	public S getSession();

	public void setSession(S session);

	public Pipe<D,R,C> setOverwriteMode(Pipe<D,R,C> p);

	public ZFrame<D,R,C> getZFrame(ZFrame<D,R,C> z);

	public ZFrame<D,R,C> addLineNo (ZFrame<D,R,C> input);

	public DFWriter<D,R,C> getWriter(ZFrame<D,R,C> input);
	public DFReader<D,R,C> getReader();
	
}