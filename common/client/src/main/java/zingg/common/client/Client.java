package zingg.common.client;

import zingg.common.client.arguments.model.IZArgs;
import zingg.common.client.event.events.IEvent;
import zingg.common.client.event.events.ZinggStartEvent;
import zingg.common.client.event.events.ZinggStopEvent;
import zingg.common.client.event.listeners.EventsListener;
import zingg.common.client.event.listeners.IEventListener;
import zingg.common.client.event.listeners.ZinggStartListener;
import zingg.common.client.event.listeners.ZinggStopListener;
import zingg.common.client.util.PipeUtilBase;

public abstract class Client<S,D,R,C> {

    protected ClientOptions options;
    protected IZArgs arguments;
    protected IZingg<S,D,R,C> zingg;
    protected final SessionManager<S> sessionManager;
    protected final BannerPrinter banner;

    public Client(SessionManager<S> sessionManager,
                  BannerPrinter banner) {
        this.sessionManager = sessionManager;
        this.banner = banner;
    }

    public void init() throws ZinggClientException {
        banner.print(arguments.getCollectMetrics());
        zingg.init(arguments, sessionManager.get(), options);
        initializeListeners();
    }

    public void execute() throws ZinggClientException {
        zingg.execute();
    }

    public void postMetrics() {
        zingg.postMetrics();
    }

    public void stop() throws ZinggClientException {
        zingg.cleanup();
    }

    public void addListener(Class<? extends IEvent> eventClass, IEventListener listener) {
        EventsListener.getInstance().addListener(eventClass, listener);
    }

    public void initializeListeners() {
        addListener(ZinggStartEvent.class, new ZinggStartListener());
        addListener(ZinggStopEvent.class, new ZinggStopListener());
    }

    public abstract PipeUtilBase<S, D, R, C> getPipeUtil();
    public void setArguments(IZArgs arguments) {
        this.arguments = arguments;
    }
    public void setOptions(ClientOptions options) {
        this.options = options;
    }
    public void setZingg(IZingg<S, D, R, C> zingg) {
        this.zingg = zingg;
    }
    public ClientOptions getOptions() {
        return options;
    }
    public IZArgs getArguments() {
        return arguments;
    }
}
