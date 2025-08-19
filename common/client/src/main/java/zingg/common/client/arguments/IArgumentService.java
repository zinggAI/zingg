package zingg.common.client.arguments;

import zingg.common.client.ZinggClientException;
import zingg.common.client.arguments.model.IZArgs;

import java.rmi.NoSuchObjectException;

public interface IArgumentService<A extends IZArgs> {
    A loadArguments(String path) throws ZinggClientException, NoSuchObjectException;
    void writeArguments(String path, IZArgs args) throws ZinggClientException, NoSuchObjectException;
}
