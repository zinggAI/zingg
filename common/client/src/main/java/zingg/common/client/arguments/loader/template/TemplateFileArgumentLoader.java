package zingg.common.client.arguments.loader.template;

import zingg.common.client.arguments.loader.ArgumentsLoader;
import zingg.common.client.arguments.model.IZArgs;
import zingg.common.client.ZinggClientException;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class TemplateFileArgumentLoader<A extends IZArgs> extends ArgumentsLoader<A> {
    private final EnvironmentVariableSubstitutor substitutor;

    public TemplateFileArgumentLoader(Class<A> argsClass) {
        super(argsClass);
        this.substitutor = new EnvironmentVariableSubstitutor();
    }

    @Override
    public A load(String path) throws ZinggClientException {
        try {
            String content = Files.readString(Paths.get(path), StandardCharsets.UTF_8);
            String substituted = substitutor.substitute(content, System.getenv());
            return objectMapper.readValue(substituted, argsClass);
        } catch (Exception | ZinggClientException exception) {
            throw new ZinggClientException("Unable to load template from: " + path, exception);
        }
    }
}
