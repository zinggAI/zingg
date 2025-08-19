package zingg.common.client.arguments.writer;

import zingg.common.client.arguments.model.IZArgs;
import zingg.common.client.ZinggClientException;

public class JsonStringArgumentsWriter<A extends IZArgs> extends ArgumentsWriter<A> {

    @Override
    public void write(String sink, IZArgs args) throws ZinggClientException {
        try {
            sink = objectMapper.writeValueAsString(args);
        } catch (Exception exception) {
            throw new ZinggClientException("Error writing config to string", exception);
        }
    }
}
