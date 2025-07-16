package zingg.common.client.util.writer.impl;

import zingg.common.client.ZFrame;
import zingg.common.client.pipe.Pipe;
import zingg.common.client.util.writer.WriterStrategy;

public class InMemoryWriterStrategy<D, R, C> implements WriterStrategy<D, R, C> {
    @Override
    public void write(ZFrame<D, R, C> frame, Pipe<D, R, C> pipe) {
        pipe.setDataset(frame);
    }
}
