package zingg.common.client.arguments.writer;

import zingg.common.client.arguments.model.IZArgs;
import zingg.common.client.ZinggClientException;

import java.nio.file.Paths;

public class JsonStringArgumentsWriter<A extends IZArgs> extends ArgumentsWriter<A> {

    @Override
    public void write(String path, IZArgs args) throws ZinggClientException {
        try {
            objectMapper.writeValue(Paths.get(path).toFile(), args);
        } catch (Exception exception) {
            throw new ZinggClientException("Error writing config to jsonfile", exception);
        }
    }
}
