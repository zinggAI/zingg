package zingg.common.client.util.writer.impl;

import zingg.common.client.ZFrame;
import zingg.common.client.util.writer.IDFWriter;
import zingg.common.client.util.writer.WriterStrategy;

public class DefaultWriterStrategy<D, R, C> implements WriterStrategy<D, R, C> {
    private final IDFWriter<D, R, C> dFWriter;

    public DefaultWriterStrategy(IDFWriter<D, R, C> dFWriter) {
        this.dFWriter = dFWriter;
    }

    @Override
    public void write(ZFrame<D, R, C> frame) throws Exception {
        dFWriter.save();
    }
}

