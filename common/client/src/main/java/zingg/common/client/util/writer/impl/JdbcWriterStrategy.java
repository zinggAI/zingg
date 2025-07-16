package zingg.common.client.util.writer.impl;

import zingg.common.client.ZFrame;
import zingg.common.client.pipe.Pipe;
import zingg.common.client.util.DFWriter;
import zingg.common.client.util.writer.DFWriterFactory;
import zingg.common.client.util.writer.WriterStrategy;

public class JdbcWriterStrategy<D, R, C> implements WriterStrategy<D, R, C> {
    private final DFWriterFactory<D, R, C> writerFactory;

    public JdbcWriterStrategy(DFWriterFactory<D, R, C> writerFactory) {
        this.writerFactory = writerFactory;
    }

    @Override
    public void write(ZFrame<D, R, C> frame, Pipe<D, R, C> pipe) throws Exception {
        DFWriter<D, R, C> writer = writerFactory.getWriter(frame)
                .format(pipe.getFormat());
        writer.setMode(pipe.getMode() != null ? pipe.getMode() : "Append");

        for (String key : pipe.getProps().keySet()) {
            writer = writer.option(key, pipe.get(key));
        }

        writer.save();
    }
}

