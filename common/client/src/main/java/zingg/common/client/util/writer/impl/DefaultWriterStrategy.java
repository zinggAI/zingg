package zingg.common.client.util.writer.impl;

import zingg.common.client.ZFrame;
import zingg.common.client.pipe.Pipe;
import zingg.common.client.util.writer.DFWriter;
import zingg.common.client.util.writer.WriterStrategy;

public class DefaultWriterStrategy<D, R, C> implements WriterStrategy<D, R, C> {
    private final DFWriter<D, R, C> dfWriter;

    public DefaultWriterStrategy(DFWriter<D, R, C> dfWriter) {
        this.dfWriter = dfWriter;
    }

    @Override
    public void write(ZFrame<D, R, C> frame, Pipe<D, R, C> pipe) throws Exception {
        DFWriter<D, R, C> writer = dfWriter
                .format(pipe.getFormat());
        writer.setMode(pipe.getMode() != null ? pipe.getMode() : "Append");
        for (String key : pipe.getProps().keySet()) {
            writer = writer.option(key, pipe.get(key));
        }
        writer.save();
    }
}

