package zingg.common.client.util;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;
import zingg.common.client.pipe.Pipe;
import zingg.common.client.util.reader.PipeUtilReader;
import zingg.common.client.util.writer.PipeUtilWriter;

public abstract class PipeUtil<S,D,R,C> implements PipeUtilBase<S,D,R,C> {

	protected S session;

	public final Log LOG = LogFactory.getLog(PipeUtil.class);
	protected final PipeUtilWriter<D, R, C> pipeUtilWriter;
	protected final PipeUtilReader<S, D, R, C> pipeUtilReader;

	public PipeUtil(S spark, PipeUtilWriter<D, R, C> pipeUtilWriter, PipeUtilReader<S, D, R, C> pipeUtilReader) {
		this.session = spark;
		this.pipeUtilWriter = pipeUtilWriter;
		this.pipeUtilReader = pipeUtilReader;
	}

	@Override
	public S getSession(){
		return this.session;
	}

	@Override
	public void setSession(S session){
		this.session = session;
	}

	@Override
	public ZFrame<D,R,C> read(boolean addLineNo, boolean addSource, Pipe<D,R,C>... pipes) throws ZinggClientException {
		return pipeUtilReader.read(addLineNo, addSource, pipes);
	}

	@Override
	public  ZFrame<D,R,C> read(boolean addLineNo, int numPartitions,
			boolean addSource, Pipe<D,R,C>... pipes) throws ZinggClientException {
		return pipeUtilReader.read(false, addLineNo, numPartitions, addSource, pipes);
	}

	@Override
	public  ZFrame<D,R,C> read(boolean addExtraCol, boolean addLineNo, int numPartitions,
			boolean addSource, Pipe<D,R,C>... pipes) throws ZinggClientException {
		return pipeUtilReader.read(addExtraCol, addLineNo, numPartitions, addSource, pipes);
	}

	@Override
	public void write(ZFrame<D, R, C> toWriteOrig, Pipe<D, R, C>... pipes) throws ZinggClientException {
		pipeUtilWriter.write(toWriteOrig, pipes);
	}


	@Override
	public String getPipesAsString(Pipe<D,R,C>[] pipes) {
		return Arrays.stream(pipes)
			.map(Pipe::getFormat)
			.collect(Collectors.toList())
			.stream().reduce((p1, p2) -> p1 + "," + p2)
			.map(Object::toString)
			.orElse("");
	}
}