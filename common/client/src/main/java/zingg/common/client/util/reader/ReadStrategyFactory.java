package zingg.common.client.util.reader;

import zingg.common.client.pipe.Pipe;
import zingg.common.client.util.reader.impl.DefaultReadStrategy;

public class ReadStrategyFactory<D, R, C> {
    public ReadStrategy<D, R, C> getStrategy(Pipe<D, R, C> pipe) {
        return new DefaultReadStrategy<>();
    }
}

