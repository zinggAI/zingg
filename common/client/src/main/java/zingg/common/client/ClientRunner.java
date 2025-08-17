package zingg.common.client;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import zingg.common.client.arguments.ArgumentServiceImpl;
import zingg.common.client.arguments.IArgumentService;
import zingg.common.client.arguments.model.Arguments;
import zingg.common.client.arguments.model.IZArgs;

import java.rmi.NoSuchObjectException;

public abstract class ClientRunner<S, D, R, C> {

    private static final Log LOG = LogFactory.getLog(ClientRunner.class);
    private final LifecycleManager<S, D, R, C> lifecycleManager = new LifecycleManager<>();
    private final IErrorNotifier errorNotifier = new EmailErrorNotifier();

    public void mainMethod(String[] args) {
        boolean success = true;
        Client<S, D, R, C> client = null;
        ClientOptions options = null;
        try {
            options = parseOptions(args);
            IZArgs loadedArgs = loadArguments(options);
            IZingg<S, D, R, C> zingg = createZingg(options);
            client = getClient(new BannerPrinter(), zingg, options, loadedArgs);
            lifecycleManager.run(client);
            LOG.warn("Zingg processing has completed");
        } catch (Throwable ex) {
            success = false;
            errorNotifier.notify(options, "Error running Zingg job", ex.getMessage());
        } finally {
            lifecycleManager.cleanup(client, success, options);
        }
    }

    protected ClientOptions parseOptions(String[] args) {
        return new ClientOptions(args);
    }
    protected IZArgs loadArguments(ClientOptions options) throws NoSuchObjectException, ZinggClientException {
        IArgumentService service = getArgumentService();
        IZArgs args = service.loadArguments(options.get(ClientOptions.CONF).getValue());
        return getArgumentAssembler().assemble(args, options);
    }
    protected IZingg<S, D, R, C> createZingg(ClientOptions options) throws ZinggClientException {
        return getZinggFactoryProvider().create(options.get(ClientOptions.PHASE).getValue());
    }
    protected IArgumentService getArgumentService() {
        return new ArgumentServiceImpl(Arguments.class);
    }
    protected  ArgumentsAssembler getArgumentAssembler() {
        return new ArgumentsAssembler();
    }
    protected ZinggFactoryProvider<S, D, R, C> getZinggFactoryProvider() {
        return new ZinggFactoryProvider<>("zingg.spark.core.executor.SparkZFactory");
    }

    protected abstract Client<S, D, R, C> getClient(BannerPrinter bannerPrinter, IZingg<S, D, R, C> zingg, ClientOptions clientOptions, IZArgs args);
}
