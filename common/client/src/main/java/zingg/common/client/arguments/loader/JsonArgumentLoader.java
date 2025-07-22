package zingg.common.client.arguments.loader;

import zingg.common.client.arguments.model.IZArgs;
import zingg.common.client.ZinggClientException;

public class JsonArgumentLoader<A extends IZArgs> extends ArgumentsLoader<A> {

    public JsonArgumentLoader(Class<A> argsClass) {
        super(argsClass);
    }

    @Override
    public A load(String jsonString) throws ZinggClientException {
        try {
            return objectMapper.readValue(jsonString, argsClass);
        } catch (Exception exception) {
            throw new ZinggClientException("Error parsing config from string: ", exception);
        }
    }
}
