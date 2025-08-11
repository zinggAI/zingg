package zingg.common.client.main;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import zingg.common.client.ClientOptions;
import zingg.common.client.arguments.ArgumentServiceImpl;
import zingg.common.client.arguments.model.Arguments;
import zingg.common.client.arguments.model.IZArgs;

public class ZinggApplication<S> {
    private final ConfigurationManager configurationManager;
    private final NotificationService notificationService;
    private final ZinggClientFactory<S> clientFactory;
    private static final Log LOG = LogFactory.getLog(ZinggApplication.class);

    public ZinggApplication(ZinggClientFactory<S> clientFactory) {
        this.clientFactory = clientFactory;
        this.configurationManager = new ConfigurationManager(new ArgumentServiceImpl<>(Arguments.class));
        this.notificationService = new NotificationService();
    }

    public void run(String... args) {
        ZinggClient<S> client = null;
        ClientOptions options = null;
        boolean success = true;
        try {
            options = new ClientOptions(args);
            IZArgs arguments = configurationManager.loadArguments(options.get(ClientOptions.CONF).getValue());
            arguments = configurationManager.buildArguments(arguments, options);
            client = clientFactory.createClient(arguments, options);
            client.init();
            client.execute();
            client.postMetrics();
            LOG.warn("Zingg processing has completed");
        } catch (Throwable throwable) {
            success = false;
            handleError(options, throwable);
        } finally {
//            cleanup(client, success);
        }
    }

    private void handleError(ClientOptions options, Throwable throwable) {
        if (options != null && options.get(ClientOptions.EMAIL) != null) {
            notificationService.sendErrorEmail(
                    options.get(ClientOptions.EMAIL).getValue(),
                    "Error running Zingg job",
                    throwable.getMessage()
            );
        }
        LOG.warn("Zingg encountered an error: " + throwable.getMessage());
    }
}
