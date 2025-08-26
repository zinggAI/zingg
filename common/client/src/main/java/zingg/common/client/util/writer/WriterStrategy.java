package zingg.common.client.util.writer;

import zingg.common.client.ZFrame;

public interface WriterStrategy<D, R, C> {
    void write(ZFrame<D, R, C> frame) throws Exception;
}
