package zingg.common.client;

import zingg.common.client.arguments.model.IZArgs;
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
        banner.print(getProductName(), getProductVersion(), arguments.getCollectMetrics());
        zingg.init(arguments, sessionManager.get(), options);
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

    public abstract PipeUtilBase<S, D, R, C> getPipeUtil();
    public String getProductName(){ return "Zingg AI"; }
    public String getProductVersion(){ return "0.6.0"; }

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
