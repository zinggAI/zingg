package zingg.common.client.arguments.writer;

import zingg.common.client.arguments.model.IZArgs;

import java.rmi.NoSuchObjectException;

public class WriterFactory<A extends IZArgs> {

    public ArgumentsWriter<A> getArgumentsWriter(WriterType writerType) throws NoSuchObjectException {
        switch (writerType) {
            case FILE:
                return new FileArgumentsWriter<A>();
            default:
                throw new NoSuchObjectException("No such writer exists: " + writerType.name());
        }
    }
}