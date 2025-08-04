package zingg.common.client.arguments;

import zingg.common.client.ZinggClientException;
import zingg.common.client.arguments.loader.ArgumentsLoader;
import zingg.common.client.arguments.loader.LoaderFactory;
import zingg.common.client.arguments.loader.LoaderType;
import zingg.common.client.arguments.model.Arguments;
import zingg.common.client.arguments.model.IZArgs;
import zingg.common.client.arguments.writer.ArgumentsWriter;
import zingg.common.client.arguments.writer.WriterFactory;
import zingg.common.client.arguments.writer.WriterType;

import java.rmi.NoSuchObjectException;

public class ArgumentServiceImpl<A extends IZArgs> implements IArgumentService<A> {
    private final Class<A> argsClass;
    private final LoaderFactory<A> loaderFactory;
    private final WriterFactory<A> writerFactory;
    private static final String JSON = "json";
    private static final String ENV = "env";

    @SuppressWarnings("unchecked")
    public ArgumentServiceImpl() {
        this((Class<A>) Arguments.class);
    }

    public ArgumentServiceImpl(Class<A> argsClass, LoaderFactory<A> loaderFactory, WriterFactory<A> writerFactory) {
        this.argsClass = argsClass;
        this.loaderFactory = loaderFactory;
        this.writerFactory = writerFactory;
    }

    public ArgumentServiceImpl(Class<A> argsClass) {
        this.argsClass = argsClass;
        this.loaderFactory = new LoaderFactory<>();
        this.writerFactory = new WriterFactory<>();
    }

    @Override
    public A loadArguments(String path) throws ZinggClientException, NoSuchObjectException {
        LoaderType loaderType = getLoaderType(path);
        ArgumentsLoader<A> argumentsLoader = loaderFactory.getArgumentsLoader(loaderType, argsClass);
        return argumentsLoader.load(path);
    }

    @Override
    public void writeArguments(String path, IZArgs args) throws ZinggClientException, NoSuchObjectException {
        ArgumentsWriter<A> argumentsWriter = writerFactory.getArgumentsWriter(WriterType.JSON);
        argumentsWriter.write(path, args);
    }

    protected LoaderType getLoaderType(String configInput) {
        if (configInput.endsWith(JSON)) {
            return LoaderType.FILE;
        } else if (configInput.endsWith(ENV)) {
            return LoaderType.TEMPLATE_FILE;
        }
        return LoaderType.JSON;
    }

}
