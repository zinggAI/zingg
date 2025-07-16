package zingg.common.client.util.reader;

import zingg.common.client.pipe.Pipe;
import zingg.common.client.util.reader.impl.FileReadStrategy;
import zingg.common.client.util.reader.impl.InMemoryReadStrategy;

public class ReadStrategyFactory<D, R, C> {
    public ReadStrategy<D, R, C> getStrategy(Pipe<D, R, C> pipe) {
        if (Pipe.FORMAT_INMEMORY.equals(pipe.getFormat())) {
            return new InMemoryReadStrategy<>();
        } else {
            return new FileReadStrategy<>();
        }
    }
}

