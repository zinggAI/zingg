package zingg.common.client.arguments.loader;

import zingg.common.client.arguments.model.IZArgs;
import zingg.common.client.ZinggClientException;

import java.io.File;

public class FileArgumentLoader<A extends IZArgs> extends ArgumentsLoader<A> {

    public FileArgumentLoader(Class<A> argsClass) {
        super(argsClass);
    }

    @Override
    public A load(String filePath) throws ZinggClientException {
        try {
            return objectMapper.readValue(new File(filePath), argsClass);
        } catch (Exception exception) {
            throw new ZinggClientException("Error reading config from file: " + filePath);
        }
    }
}
