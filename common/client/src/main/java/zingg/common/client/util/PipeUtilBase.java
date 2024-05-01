package zingg.common.client.util;

import zingg.common.client.IArguments;
import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;
import zingg.common.client.pipe.Pipe;

//spark session 
//dataset
public interface PipeUtilBase<S, D, R, C> {
	

	public ZFrame<D, R, C> readInternal(Pipe<D, R, C> p, boolean addSource) throws ZinggClientException;

	public  ZFrame<D, R, C> readInternal(boolean addLineNo,
		boolean addSource, Pipe<D, R, C>... pipes) throws ZinggClientException;
	
	public ZFrame<D,R,C> readInternal(boolean addExtraCol, boolean addLineNo,
			boolean addSource, Pipe<D,R,C>... pipes) throws ZinggClientException;

	public  ZFrame<D, R, C> read(boolean addLineNo, boolean addSource, Pipe<D, R, C>... pipes) 
		throws ZinggClientException;

	
	public  ZFrame<D, R, C> read(boolean addLineNo, int numPartitions,
		boolean addSource, Pipe<D, R, C>... pipes)
		throws ZinggClientException;

	public  ZFrame<D,R,C> read(boolean addExtraCol, boolean addLineNo, int numPartitions,
			boolean addSource, Pipe<D,R,C>... pipes) throws ZinggClientException;

	public void write(ZFrame<D, R, C> toWriteOrig, Pipe<D, R, C>... pipes)
		throws ZinggClientException;

	
	public Pipe<D, R, C> getTrainingDataUnmarkedPipe(IArguments args);

	public Pipe<D, R, C> getTrainingDataMarkedPipe(IArguments args);
	
	public Pipe<D, R, C> getModelDocumentationPipe(IArguments args);
	
	public Pipe<D, R, C> getBlockingTreePipe(IArguments args);

	public Pipe<D, R, C> getStopWordsPipe(IArguments args, String string);

	public String getPipesAsString(Pipe<D,R,C>[] pipes);

	public S getSession();

	public void setSession(S session);

	public Pipe<D,R,C> setOverwriteMode(Pipe<D,R,C> p);

	public ZFrame<D,R,C> getZFrame(ZFrame<D,R,C> z);

	public ZFrame<D,R,C> addLineNo (ZFrame<D,R,C> input);

	public DFWriter<D,R,C> getWriter(ZFrame<D,R,C> input);
	public DFReader<D,R,C> getReader();
	
}