package zingg.common.client.arguments.loader;

import zingg.common.client.arguments.loader.template.TemplateFileArgumentLoader;
import zingg.common.client.arguments.model.IZArgs;

import java.rmi.NoSuchObjectException;

public class LoaderFactory<A extends IZArgs> {

    public ArgumentsLoader<A> getArgumentsLoader(LoaderType loaderType, Class<A> argsClass) throws NoSuchObjectException {
        switch (loaderType) {
            case FILE:
                return new FileArgumentLoader<A>(argsClass);
            case JSON:
                return new JsonArgumentLoader<A>(argsClass);
            case TEMPLATE_FILE:
                return new TemplateFileArgumentLoader<A>(argsClass);
            case DEFAULT:
                return new DefaultArgumentLoader<>(argsClass);
            default:
                throw new NoSuchObjectException("No such loader exists: " + loaderType.name());
        }
    }
}
