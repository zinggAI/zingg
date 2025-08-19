package zingg.common.client.arguments.writer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import zingg.common.client.arguments.model.IZArgs;
import zingg.common.client.ZinggClientException;

public abstract class ArgumentsWriter<A extends IZArgs> {
    protected final ObjectMapper objectMapper;

    public ArgumentsWriter() {
        this.objectMapper = new ObjectMapper()
                .configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    public abstract void write(String sink, IZArgs args) throws ZinggClientException;
}
