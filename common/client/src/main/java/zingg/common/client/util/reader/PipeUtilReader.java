package zingg.common.client.util.reader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;
import zingg.common.client.pipe.Pipe;
import zingg.common.client.util.ColName;
import zingg.common.client.util.DFReader;

import java.util.Map;

public abstract class PipeUtilReader<S, D, R, C> implements IPipeUtilReader<D, R, C> {

    private static final Log LOG = LogFactory.getLog(PipeUtilReader.class);
    private final S session;

    public PipeUtilReader(S session) {
        this.session = session;
    }

    @Override
    public ZFrame<D, R, C> read(boolean addLineNo, boolean addSource, Pipe<D, R, C>... pipes) throws ZinggClientException {
        ZFrame<D, R, C> rows = readMultiplePipes(false, addLineNo, addSource, pipes);
        return rows.cache();
    }

    @Override
    public ZFrame<D, R, C> read(boolean addLineNo, int numPartitions, boolean addSource, Pipe<D, R, C>... pipes) throws ZinggClientException {
        return read(false, addLineNo, numPartitions, addSource, pipes);
    }

    @Override
    public ZFrame<D, R, C> read(boolean addExtraCol, boolean addLineNo, int numPartitions, boolean addSource, Pipe<D, R, C>... pipes) throws ZinggClientException {
        ZFrame<D, R, C> rows = readMultiplePipes(addExtraCol, addLineNo, addSource, pipes);
        return rows.repartition(numPartitions).cache();
    }

    protected DFReader<D, R, C> buildReaderFromPipe(Pipe<D, R, C> pipe) {
        DFReader<D, R, C> reader = getReader().format(pipe.getFormat());
        if (pipe.getSchema() != null) {
            reader = reader.setSchema(pipe.getSchema());
        }
        for (Map.Entry<String, String> entry : pipe.getProps().entrySet()) {
            reader = reader.option(entry.getKey(), entry.getValue());
        }
        return reader.option("mode", "PERMISSIVE");
    }

    protected ZFrame<D, R, C> readSinglePipe(Pipe<D, R, C> pipe, boolean addSource) throws ZinggClientException {
        try {
            LOG.warn("Reading " + pipe);
            DFReader<D, R, C> reader = buildReaderFromPipe(pipe);
            ReadStrategy<D, R, C> strategy = new ReadStrategyFactory<D, R, C>().getStrategy(pipe);
            ZFrame<D, R, C> frame = strategy.read(reader, pipe);
            if (addSource) {
                frame = frame.withColumn(ColName.SOURCE_COL, pipe.getName());
            }
            pipe.setDataset(frame);
            return frame;
        } catch (Exception ex) {
            LOG.warn("Error while reading: " + ex.getMessage());
            throw new ZinggClientException("Could not read data.", ex);
        }
    }

    protected ZFrame<D, R, C> readMultiplePipes(boolean addExtraCol, boolean addLineNo, boolean addSource, Pipe<D, R, C>... pipes) throws ZinggClientException {
        ZFrame<D, R, C> result = null;
        for (Pipe<D, R, C> pipe : pipes) {
            ZFrame<D, R, C> current = readSinglePipe(pipe, addSource);
            if (result == null) {
                result = current;
            } else {
                result = addExtraCol ? result.unionByName(current, true) : result.union(current);
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug("Pipe " + pipe.getName() + " size: " + current.count());
            }
        }
        if (addLineNo && result != null) {
            result = addLineNo(result);
        }
        return result;
    }

    protected S getSession() {
        return this.session;
    }

    protected abstract ZFrame<D, R, C> addLineNo(ZFrame<D, R, C> data);
    protected abstract DFReader<D, R, C> getReader();
}
