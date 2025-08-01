package zingg.common.client.util.writer;

import zingg.common.client.ZFrame;
import zingg.common.client.pipe.Pipe;

public interface WriterStrategy<D, R, C> {
    void write(ZFrame<D, R, C> frame, Pipe<D, R, C> pipe) throws Exception;
}
