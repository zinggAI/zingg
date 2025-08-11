package zingg.common.client.main;

import zingg.common.client.ClientOptions;
import zingg.common.client.ZinggClientException;
import zingg.common.client.arguments.ArgumentServiceImpl;
import zingg.common.client.arguments.model.Arguments;
import zingg.common.client.arguments.model.IZArgs;

public abstract class AbstractZinggClientFactory<S,D,R,C,T> implements ZinggClientFactory<S> {
    protected final String zFactoryClassName;
    protected final ConfigurationManager configurationManager;

    public AbstractZinggClientFactory(String zFactoryClassName) {
        this.zFactoryClassName = zFactoryClassName;
        this.configurationManager = new ConfigurationManager(new ArgumentServiceImpl<>(Arguments.class));
    }

    @Override
    public ZinggClient<S> createClient(IZArgs arguments, ClientOptions options) throws ZinggClientException {
        IZArgs enhancedArgs = configurationManager.buildArguments(arguments, options);
        return createClientBuilder()
                .withArguments(enhancedArgs)
                .withOptions(options)
                .withZinggFactory(zFactoryClassName)
                .build();
    }

    protected abstract ZinggClientBuilder<S,D,R,C,T> createClientBuilder();
}
