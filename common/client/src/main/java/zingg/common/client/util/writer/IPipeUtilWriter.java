package zingg.common.client.util.writer;

import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;
import zingg.common.client.pipe.Pipe;

public interface IPipeUtilWriter<D, R, C> {
    void write(ZFrame<D, R, C> toWriteOrig, Pipe<D, R, C>... pipes)
            throws ZinggClientException;
}
