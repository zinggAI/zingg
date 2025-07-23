package zingg.common.client.arguments.writer;

import com.fasterxml.jackson.databind.SerializationFeature;
import zingg.common.client.arguments.model.IZArgs;
import zingg.common.client.ZinggClientException;

import java.io.File;

public class FileArgumentsWriter<A extends IZArgs> extends ArgumentsWriter<A> {

    @Override
    public void write(String filePath, IZArgs args) throws ZinggClientException {
        try {
            objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
            objectMapper.writeValue(new File(filePath), args);
        } catch (Exception exception) {
            throw new ZinggClientException("Error writing config to file: " + filePath);
        }
    }
}
