package zingg.common.client.arguments;

import zingg.common.client.ZinggClientException;
import zingg.common.client.arguments.loader.LoaderType;
import zingg.common.client.arguments.model.IZArgs;
import zingg.common.client.arguments.writer.WriterType;

import java.rmi.NoSuchObjectException;

public interface IArgumentService<A extends IZArgs> {
    A loadArguments(String path, LoaderType loaderType) throws ZinggClientException, NoSuchObjectException;
    void writeArguments(String path, IZArgs args, WriterType writerType) throws ZinggClientException, NoSuchObjectException;
}
