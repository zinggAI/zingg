package zingg.common.client.util;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;
import zingg.common.client.pipe.Pipe;
import zingg.common.client.util.reader.ReadStrategy;
import zingg.common.client.util.reader.ReadStrategyFactory;
import zingg.common.client.util.writer.WriterStrategy;
import zingg.common.client.util.writer.WriterStrategyFactory;

public abstract class PipeUtil<S,D,R,C> implements PipeUtilBase<S,D,R,C>{

	protected S session;

	public  final Log LOG = LogFactory.getLog(PipeUtil.class);

	public PipeUtil(S spark) {
		this.session = spark;
	}
	
	public S getSession(){
		return this.session;
	}

	public void setSession(S session){
		this.session = session;
	}

	public DFReader<D,R,C> getReader(Pipe<D,R,C> p) {
		DFReader<D,R,C> reader = getReader();
		reader = reader.format(p.getFormat());
		if (p.getSchema() != null) {
			reader = reader.setSchema(p.getSchema());
		}
		
		for (String key : p.getProps().keySet()) {
			reader = reader.option(key, p.get(key));
		}
		reader = reader.option("mode", "PERMISSIVE");
		return reader;
	}

	protected ZFrame<D, R, C> read(DFReader<D, R, C> reader, Pipe<D, R, C> pipe, boolean addSource) throws ZinggClientException {
		try {
			LOG.warn("Reading " + pipe);

			ReadStrategyFactory<D, R, C> factory = new ReadStrategyFactory<>();
			ReadStrategy<D, R, C> strategy = factory.getStrategy(pipe);

			ZFrame<D, R, C> input = strategy.read(reader, pipe);

			if (addSource) {
				input = input.withColumn(ColName.SOURCE_COL, pipe.getName());
			}

			pipe.setDataset(input);
			return getZFrame(input);

		} catch (Exception ex) {
			LOG.warn(ex.getMessage());
			throw new ZinggClientException("Could not read data.", ex);
		}
	}


	public  ZFrame<D,R,C> readInternal(Pipe<D,R,C> p, 
		boolean addSource) throws ZinggClientException {
		DFReader<D,R,C> reader = getReader(p);
		return read(reader, p, addSource);		
	}

	public ZFrame<D,R,C> readInternal(boolean addLineNo,
			boolean addSource, Pipe<D,R,C>... pipes) throws ZinggClientException {
		return readInternal(false, addLineNo,addSource, pipes);
	}

	public ZFrame<D,R,C> readInternal(boolean addExtraCol, boolean addLineNo,
			boolean addSource, Pipe<D,R,C>... pipes) throws ZinggClientException {
		ZFrame<D,R,C> input = null;

		for (Pipe p : pipes) {
			if (input == null) {
				input = readInternal(p, addSource);
				if (LOG.isDebugEnabled()) {
					LOG.debug("input size is " + input.count());		
				}		
			} else {
				if(!addExtraCol) {
					input = input.union(readInternal(p, addSource));
				} else {
					input = input.unionByName(readInternal(p, addSource),true);
				}
			}
		}
		// we will probably need to create row number as string with pipename/id as
		// suffix
		if (addLineNo)
			input = addLineNo(input);
		// we need to transform the input here by using stop words
		return input;
	}	

	public ZFrame<D,R,C> read(boolean addLineNo, boolean addSource, Pipe<D,R,C>... pipes) throws ZinggClientException {
		ZFrame<D,R,C> rows = readInternal(addLineNo, addSource, pipes);
		rows = rows.cache();
		return rows;
	}

	public  ZFrame<D,R,C> read(boolean addLineNo, int numPartitions,
			boolean addSource, Pipe<D,R,C>... pipes) throws ZinggClientException {
		return read(false, addLineNo, numPartitions, addSource, pipes);
	}

	public  ZFrame<D,R,C> read(boolean addExtraCol, boolean addLineNo, int numPartitions,
			boolean addSource, Pipe<D,R,C>... pipes) throws ZinggClientException {
		ZFrame<D,R,C> rows = readInternal(addExtraCol, addLineNo, addSource, pipes);
		rows = rows.repartition(numPartitions);
		rows = rows.cache();
		return rows;
	}

	public void write(ZFrame<D, R, C> toWriteOrig, Pipe<D, R, C>... pipes) throws ZinggClientException {
		WriterStrategyFactory<D, R, C> strategyFactory = new WriterStrategyFactory<>(this::getWriter);
		try {
			for (Pipe<D, R, C> pipe : pipes) {
				LOG.warn("Writing output " + pipe);
				WriterStrategy<D, R, C> strategy = strategyFactory.getStrategy(pipe);
				strategy.write(toWriteOrig, pipe);

				if (Pipe.FORMAT_INMEMORY.equals(pipe.getFormat())) {
					return;
				}
			}
		} catch (Exception ex) {
			throw new ZinggClientException(ex.getMessage());
		}
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