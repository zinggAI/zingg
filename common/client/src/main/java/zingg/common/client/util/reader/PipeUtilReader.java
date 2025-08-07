package zingg.common.client.util.reader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;
import zingg.common.client.pipe.Pipe;
import zingg.common.client.util.ColName;

public abstract class PipeUtilReader<S, D, R, C> implements IPipeUtilReader<D, R, C> {

    private static final Log LOG = LogFactory.getLog(PipeUtilReader.class);
    private final S session;

    public PipeUtilReader(S session) {
        this.session = session;
    }

    @SafeVarargs
    @Override
    public final ZFrame<D, R, C> read(boolean addLineNo, boolean addSource, Pipe<D, R, C>... pipes) throws ZinggClientException {
        ZFrame<D, R, C> rows = readMultiplePipes(false, addLineNo, addSource, pipes);
        return rows.cache();
    }

    @SafeVarargs
    @Override
    public final ZFrame<D, R, C> read(boolean addLineNo, int numPartitions, boolean addSource, Pipe<D, R, C>... pipes) throws ZinggClientException {
        return read(false, addLineNo, numPartitions, addSource, pipes);
    }

    @SafeVarargs
    @Override
    public final ZFrame<D, R, C> read(boolean addExtraCol, boolean addLineNo, int numPartitions, boolean addSource, Pipe<D, R, C>... pipes) throws ZinggClientException {
        ZFrame<D, R, C> rows = readMultiplePipes(addExtraCol, addLineNo, addSource, pipes);
        return rows.repartition(numPartitions).cache();
    }

    protected ZFrame<D, R, C> readSinglePipe(Pipe<D, R, C> pipe, boolean addSource) throws ZinggClientException {
        try {
            LOG.warn("Reading " + pipe);
            DFReader<D, R, C> reader = Helper.initializeReaderForPipe(pipe, getReader());
            ReadStrategy<D, R, C> strategy = getReadStrategy(pipe);
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

    @SafeVarargs
    protected final ZFrame<D, R, C> readMultiplePipes(boolean addExtraCol, boolean addLineNo, boolean addSource, Pipe<D, R, C>... pipes) throws ZinggClientException {
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
    protected ReadStrategy<D, R, C> getReadStrategy(Pipe<D, R, C> pipe) {
        return new ReadStrategyFactory<D, R, C>().getStrategy(pipe);
    }
}
