package zingg.common.client.util.writer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;
import zingg.common.client.pipe.Pipe;

public abstract class PipeUtilWriter<D, R, C> implements IPipeUtilWriter<D, R, C> {
    private final Log LOG = LogFactory.getLog(PipeUtilWriter.class);

    @SafeVarargs
    public final void write(ZFrame<D, R, C> toWriteOrig, Pipe<D, R, C>... pipes) throws ZinggClientException {
        try {
            for (Pipe<D, R, C> pipe : pipes) {
                IDFWriter<D, R, C> writer = getWriter(toWriteOrig, pipe);
                LOG.warn("Writing output " + pipe);
                writer.write(pipe);
            }
        } catch (Exception ex) {
            throw new ZinggClientException("Error writing output", ex);
        }
    }
    protected abstract IDFWriter<D,R,C> getWriter(ZFrame<D, R, C> input, Pipe<D, R, C> pipe);
}
