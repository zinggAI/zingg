package zingg.common.client.main;

import zingg.common.client.ClientOptions;
import zingg.common.client.IZingg;
import zingg.common.client.IZinggFactory;
import zingg.common.client.ZinggClientException;
import zingg.common.client.arguments.model.IZArgs;
import zingg.common.client.options.ZinggOptions;

public abstract class ZinggClientBuilder<S,D,R,C,T> {
    protected IZArgs arguments;
    protected IZingg<S,D,R,C> zingg;
    protected ClientOptions options;
    protected SessionManager<S> sessionManager;
    protected EventManager eventManager;
    protected String zFactoryClassName;

    public ZinggClientBuilder<S,D,R,C,T> withArguments(IZArgs arguments) {
        this.arguments = arguments;
        return this;
    }

    public ZinggClientBuilder<S,D,R,C,T> withOptions(ClientOptions options) {
        this.options = options;
        return this;
    }

    public ZinggClientBuilder<S,D,R,C,T> withZinggFactory(String factoryClassName) {
        this.zFactoryClassName = factoryClassName;
        return this;
    }

    public ZinggClientBuilder<S,D,R,C,T> withSession(S session) {
        this.sessionManager = new SessionManager<>(new DefaultSessionFactory<>());
        this.sessionManager.setSession(session);
        return this;
    }

    public ZinggClient<S> build() throws ZinggClientException {
        validate();
        setupZingg();
        setupEventManager();
        return createClient();
    }

    private void validate() throws ZinggClientException {
        if (arguments == null || options == null) {
            throw new ZinggClientException("Arguments and options are required");
        }
    }

    private void setupZingg() throws ZinggClientException {
        try {
            IZinggFactory factory = getZinggFactory();
            String phase = options.get(ClientOptions.PHASE).value.trim();
            this.zingg = factory.get(ZinggOptions.getByValue(phase));
        } catch (Exception e) {
            throw new ZinggClientException("Failed to setup Zingg instance", e);
        }
    }

    protected abstract ZinggClient<S> createClient();
}
