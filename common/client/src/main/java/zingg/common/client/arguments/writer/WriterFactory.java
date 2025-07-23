package zingg.common.client.arguments.writer;

import zingg.common.client.arguments.model.IZArgs;

import java.rmi.NoSuchObjectException;

public class WriterFactory {

    public static <A extends IZArgs> ArgumentsWriter<A> getArgumentsWriter(WriterType writerType) throws NoSuchObjectException {
        switch (writerType) {
            case FILE:
                return new FileArgumentsWriter<A>();
            case JSON:
                return new JsonStringArgumentsWriter<A>();
            default:
                throw new NoSuchObjectException("No such writer exists: " + writerType.name());
        }
    }
}