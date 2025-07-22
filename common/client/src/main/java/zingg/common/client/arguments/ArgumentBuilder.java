package zingg.common.client.arguments;

import zingg.common.client.ClientOptions;
import zingg.common.client.ZinggClientException;
import zingg.common.client.arguments.loader.LoaderType;
import zingg.common.client.arguments.model.IZArgs;

import java.rmi.NoSuchObjectException;

public class ArgumentBuilder<A extends IZArgs> {
    private final IArgumentService<A> argumentService;
    private static ArgumentBuilder<?> argumentBuilder = null;
    private static final String JSON = "json";
    private static final String ENV = "env";

    private ArgumentBuilder(Class<A> argsClass) {
        this.argumentService = new ArgumentServiceImpl<>(argsClass);
    }

    /**
     * This method is responsible for building arguments
     * @return arguments
     */
    public A buildArguments(ClientOptions clientOptions) throws NoSuchObjectException, ZinggClientException {
        String configInput = clientOptions.get(ClientOptions.CONF).getValue();
        LoaderType loaderType = getLoaderType(configInput);
        return argumentService.loadArguments(configInput, loaderType);
    }

    @SuppressWarnings("unchecked")
    public static <A extends IZArgs> ArgumentBuilder<A> getInstance(Class<A> argsClass) {
        if (argumentBuilder == null) {
            argumentBuilder = new ArgumentBuilder<>(argsClass);
        }
        return (ArgumentBuilder<A>) argumentBuilder;
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

