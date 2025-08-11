package zingg.common.client.util.writer.impl;

import zingg.common.client.ZFrame;
import zingg.common.client.pipe.FilePipe;
import zingg.common.client.pipe.Pipe;
import zingg.common.client.util.writer.Helper;
import zingg.common.client.util.writer.IDFWriter;
import zingg.common.client.util.writer.WriterStrategy;

public class FileWriterStrategy<D, R, C> implements WriterStrategy<D, R, C> {
    private final IDFWriter<D, R, C> dFWriter;

    public FileWriterStrategy(IDFWriter<D, R, C> dFWriter) {
        this.dFWriter = dFWriter;
    }

    @Override
    public void write(ZFrame<D, R, C> frame, Pipe<D, R, C> pipe) throws Exception {
        IDFWriter<D, R, C> writer = Helper.initializeWriterForPipe(pipe, dFWriter);
        writer.save(pipe.get(FilePipe.LOCATION));
    }
}

