package zingg.common.client.util;

import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;
import zingg.common.client.pipe.Pipe;

//spark session 
//dataset
public interface PipeUtilBase<S, D, R, C> {
	

	ZFrame<D, R, C> readInternal(Pipe<D, R, C> p, boolean addSource) throws ZinggClientException;

	ZFrame<D, R, C> readInternal(boolean addLineNo,
                                 boolean addSource, Pipe<D, R, C>... pipes) throws ZinggClientException;
	
	ZFrame<D,R,C> readInternal(boolean addExtraCol, boolean addLineNo,
                               boolean addSource, Pipe<D, R, C>... pipes) throws ZinggClientException;

	ZFrame<D, R, C> read(boolean addLineNo, boolean addSource, Pipe<D, R, C>... pipes)
		throws ZinggClientException;

	
	ZFrame<D, R, C> read(boolean addLineNo, int numPartitions,
                         boolean addSource, Pipe<D, R, C>... pipes)
		throws ZinggClientException;

	ZFrame<D,R,C> read(boolean addExtraCol, boolean addLineNo, int numPartitions,
                       boolean addSource, Pipe<D, R, C>... pipes) throws ZinggClientException;

	void write(ZFrame<D, R, C> toWriteOrig, Pipe<D, R, C>... pipes)
		throws ZinggClientException;

	String getPipesAsString(Pipe<D, R, C>[] pipes);

	S getSession();

	void setSession(S session);

	ZFrame<D,R,C> getZFrame(ZFrame<D, R, C> z);

	ZFrame<D,R,C> addLineNo(ZFrame<D, R, C> input);

	DFWriter<D,R,C> getWriter(ZFrame<D, R, C> input);
	DFReader<D,R,C> getReader();
	
}