package zingg.common.client.util.writer;

import zingg.common.client.ZFrame;
import zingg.common.client.util.DFWriter;

public interface DFWriterFactory<D, R, C> {
    DFWriter<D, R, C> getWriter(ZFrame<D, R, C> frame);
}

