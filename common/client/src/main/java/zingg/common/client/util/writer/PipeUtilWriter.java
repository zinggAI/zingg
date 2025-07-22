package zingg.common.client.util.writer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;
import zingg.common.client.pipe.Pipe;
import zingg.common.client.util.DFWriter;

public abstract class PipeUtilWriter<D, R, C> implements IPipeUtilWriter<D, R, C> {
    private   final Log LOG = LogFactory.getLog(PipeUtilWriter.class);

    public void write(ZFrame<D, R, C> toWriteOrig, Pipe<D, R, C>... pipes) throws ZinggClientException {
        WriterStrategyFactory<D, R, C> strategyFactory = new WriterStrategyFactory<>(getWriter(toWriteOrig));
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
    protected abstract DFWriter<D,R,C> getWriter(ZFrame<D, R, C> input);
}
