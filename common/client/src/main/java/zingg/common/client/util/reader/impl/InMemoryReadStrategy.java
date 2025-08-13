package zingg.common.client.util.reader.impl;

import zingg.common.client.ZFrame;
import zingg.common.client.pipe.Pipe;
import zingg.common.client.util.reader.IDFReader;
import zingg.common.client.util.reader.ReadStrategy;

public class InMemoryReadStrategy<D, R, C> implements ReadStrategy<D, R, C> {
    @Override
    public ZFrame<D, R, C> read(IDFReader<D, R, C> reader, Pipe<D, R, C> pipe) {
        return pipe.getDataset();
    }
}
