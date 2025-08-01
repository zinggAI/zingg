package zingg.common.client.arguments;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import zingg.common.client.ClientOptions;
import zingg.common.client.ZinggClientException;
import zingg.common.client.arguments.loader.LoaderFactory;
import zingg.common.client.arguments.loader.LoaderType;
import zingg.common.client.arguments.model.IZArgs;
import zingg.common.client.arguments.writer.WriterFactory;

public class ArgumentBuilder<A extends IZArgs> {
    private final IArgumentService<A> argumentService;
    private static final String JSON = "json";
    private static final String ENV = "env";
    private static final Log LOG = LogFactory.getLog(ArgumentBuilder.class);

    public ArgumentBuilder(Class<A> argsClass) {
        this.argumentService = new ArgumentServiceImpl<>(argsClass);
    }

    public ArgumentBuilder(Class<A> argsClass, LoaderFactory<A> loaderFactory, WriterFactory<A> writerFactory) {
        this.argumentService = new ArgumentServiceImpl<>(argsClass, loaderFactory, writerFactory);
    }

    /**
     * This method is responsible for building arguments
     * @return arguments
     */
    public A buildArguments(ClientOptions clientOptions) throws ZinggClientException {
        try {
            String configInput = clientOptions.get(ClientOptions.CONF).getValue();
            LoaderType loaderType = getLoaderType(configInput);
            LOG.info("Building arguments for type " + loaderType + " ....");
            return argumentService.loadArguments(configInput, loaderType);
        } catch (Throwable exception) {
            LOG.info("Exception occurred while building arguments " + exception);
            throw new ZinggClientException("Failed to build arguments");
        }
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

