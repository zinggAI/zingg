package zingg.common.client.arguments.loader;

import zingg.common.client.ZinggClientException;
import zingg.common.client.arguments.model.IZArgs;

import java.lang.reflect.InvocationTargetException;

public class DefaultArgumentLoader<A extends IZArgs> extends ArgumentsLoader<A> {
    public DefaultArgumentLoader(Class<A> argsClass) {
        super(argsClass);
    }

    @Override
    public A load(String path) throws ZinggClientException, InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        return super.load();
    }
}
