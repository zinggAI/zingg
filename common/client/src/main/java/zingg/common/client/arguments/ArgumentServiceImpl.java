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

    @SuppressWarnings("unchecked")
    public ArgumentServiceImpl() {
        this((Class<A>) Arguments.class);
    }

    public ArgumentServiceImpl(Class<A> argsClass) {
        this.argsClass = argsClass;
    }

    @Override
    public A loadArguments(String path, LoaderType loaderType) throws ZinggClientException, NoSuchObjectException {
        ArgumentsLoader<A> argumentsLoader = LoaderFactory.getArgumentsLoader(loaderType, argsClass);
        return argumentsLoader.load(path);
    }

    @Override
    public void writeArguments(String path, IZArgs args, WriterType writerType) throws ZinggClientException, NoSuchObjectException {
        ArgumentsWriter<A> argumentsWriter = WriterFactory.getArgumentsWriter(WriterType.JSON);
        argumentsWriter.write(path, args);
    }

}
