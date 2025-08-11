package zingg.common.client.main;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import zingg.common.client.ClientOptions;
import zingg.common.client.IZingg;
import zingg.common.client.ZinggClientException;
import zingg.common.client.arguments.model.IZArgs;
import zingg.common.client.event.events.ZinggStartEvent;
import zingg.common.client.event.events.ZinggStopEvent;

public abstract class ZinggClientImpl<S,D,R,C,T> implements ZinggClient<S> {
    private static final Log LOG = LogFactory.getLog(ZinggClientImpl.class);

    protected final IZArgs arguments;
    protected final IZingg<S,D,R,C> zingg;
    protected final ClientOptions options;
    protected final SessionManager<S> sessionManager;
    protected final EventManager eventManager;
    protected final MetricsService<S,D,R,C> metricsService;
    protected final NotificationService notificationService;

    protected ZinggClientImpl(ZinggClientBuilder<S,D,R,C,T> builder) {
        this.arguments = builder.arguments;
        this.zingg = builder.zingg;
        this.options = builder.options;
        this.sessionManager = builder.sessionManager;
        this.eventManager = builder.eventManager;
        this.metricsService = new MetricsService<>(zingg);
        this.notificationService = new NotificationService();
    }

    @Override
    public void init() throws ZinggClientException {
        BannerService.printBanner(arguments.getCollectMetrics());
        zingg.setClientOptions(options);
        zingg.init(arguments, sessionManager.getSession(), options);
        eventManager.fireEvent(new ZinggStartEvent());
    }

    @Override
    public void execute() throws ZinggClientException {
        zingg.execute();
    }

    public void postMetrics() throws ZinggClientException {
        metricsService.postMetrics();
    }

    @Override
    public void stop() throws ZinggClientException {
        eventManager.fireEvent(new ZinggStopEvent());
        zingg.cleanup();
    }

    @Override
    public IZArgs getArguments() {
        return arguments;
    }

    @Override
    public S getSession() {
        return sessionManager.getSession();
    }
}
