package zingg.common.client.util.reader;

import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;
import zingg.common.client.pipe.Pipe;

public interface IPipeUtilReader<D, R, C> {
    ZFrame<D, R, C> read(boolean addLineNo, boolean addSource, Pipe<D, R, C>... pipes)
            throws ZinggClientException;

    ZFrame<D, R, C> read(boolean addLineNo, int numPartitions,
                         boolean addSource, Pipe<D, R, C>... pipes)
            throws ZinggClientException;

    ZFrame<D,R,C> read(boolean addExtraCol, boolean addLineNo, int numPartitions,
                       boolean addSource, Pipe<D, R, C>... pipes) throws ZinggClientException;
}
