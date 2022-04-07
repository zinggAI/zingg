package zingg.util;

import zingg.client.Arguments;
import zingg.client.ZFrame;
import zingg.client.pipe.Pipe;

//spark session 
//dataset
public interface PipeUtilBase<S, D, R, C> {
	

	public ZFrame<D, R, C> readInternal(Pipe p, boolean addSource);

	public  ZFrame<D, R, C> readInternal(boolean addLineNo,
			boolean addSource, Pipe... pipes);

	public  ZFrame<D, R, C> read(boolean addLineNo, boolean addSource, Pipe... pipes);

	
	public  ZFrame<D, R, C> read(boolean addLineNo, int numPartitions,
			boolean addSource, Pipe... pipes);

	public void write(ZFrame<D, R, C> toWriteOrig, Arguments args, Pipe... pipes);

	
	public Pipe getTrainingDataUnmarkedPipe(Arguments args);

	public Pipe getTrainingDataMarkedPipe(Arguments args);
	
	public Pipe getModelDocumentationPipe(Arguments args);
	
	public Pipe getBlockingTreePipe(Arguments args);

	public String getPipesAsString(Pipe[] pipes);

	public S getSession();

	public void setSession(S session);
}