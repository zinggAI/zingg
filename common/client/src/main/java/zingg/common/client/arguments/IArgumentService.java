package zingg.common.client.arguments;

import zingg.common.client.ZinggClientException;
import zingg.common.client.arguments.model.IZArgs;

import java.lang.reflect.InvocationTargetException;
import java.rmi.NoSuchObjectException;

public interface IArgumentService<A extends IZArgs> {
    A loadArguments(String path) throws ZinggClientException, NoSuchObjectException, InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchMethodException;
    void writeArguments(String path, IZArgs args) throws ZinggClientException, NoSuchObjectException;
    A loadArguments() throws NoSuchObjectException, ZinggClientException, InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchMethodException;
}
