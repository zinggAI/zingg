package zingg.common.client.arguments.loader;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import zingg.common.client.arguments.model.IZArgs;
import zingg.common.client.ZinggClientException;

public abstract class ArgumentsLoader<A extends IZArgs> {
    protected final Class<A> argsClass;
    protected final ObjectMapper objectMapper;

    public ArgumentsLoader(Class<A> argsClass) {
        this.argsClass = argsClass;
        this.objectMapper = new ObjectMapper()
                .configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
    }

    public abstract A load(String path) throws ZinggClientException;
}
