package zingg.common.client;

import zingg.common.client.arguments.model.IZArgs;
import zingg.common.client.listener.ListenerManager;
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

    protected void initializeListeners() {
        ListenerManager listenerManager = new ListenerManager();
        listenerManager.initializeListeners();
    }

    public S getSession() {
        return sessionManager.get();
    }
    public SessionManager<S> getSessionManager() {
        return sessionManager;
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


	public void addListener(Class<? extends IEvent> eventClass, IEventListener listener) {
        EventsListener.getInstance().addListener(eventClass, listener);
    }

    public void initializeListeners() {
        addListener(ZinggStartEvent.class, new ZinggStartListener());
        addListener(ZinggStopEvent.class, new ZinggStopListener());
    }
    
    public abstract S getSession();
    
    public void setSession(S s) {
    	this.session = s;
    }

	public abstract PipeUtilBase<S, D, R, C> getPipeUtil();

	public void setPipeUtil(PipeUtilBase<S, D, R, C> pipeUtil) {
		this.pipeUtil = pipeUtil;
	}

	public String getProductName(){
		return "Zingg AI";
	}

	public String getProductVersion(){
		return "0.6.0";
	}

	public Long getMarkedRecordsStat(ZFrame<D,R,C>  markedRecords, long value) {
		return zingg.getMarkedRecordsStat(markedRecords, value);
	}

	public Long getMatchedMarkedRecordsStat(ZFrame<D,R,C>  markedRecords) {
		return zingg.getMatchedMarkedRecordsStat(markedRecords);
	}

	public Long getUnmatchedMarkedRecordsStat(ZFrame<D,R,C>  markedRecords) {
		return zingg.getUnmatchedMarkedRecordsStat(markedRecords);
	}

	public Long getUnsureMarkedRecordsStat(ZFrame<D,R,C>  markedRecords) {
		return zingg.getUnsureMarkedRecordsStat(markedRecords);
	}

	public ZFrame<D,R,C>  getMarkedRecords() {
		return zingg.getMarkedRecords();
	}

	public ZFrame<D,R,C>  getUnmarkedRecords() {
		return zingg.getUnmarkedRecords();
	}

	public ITrainingDataModel<S, D, R, C> getTrainingDataModel() throws UnsupportedOperationException {
		return zingg.getTrainingDataModel();
	}

}