package zingg.common.client.main;

import zingg.common.client.ClientOptions;
import zingg.common.client.ZinggClientException;
import zingg.common.client.arguments.model.IZArgs;

public interface ZinggClientFactory<S> {
    ZinggClient<S> createClient(IZArgs arguments, ClientOptions options) throws ZinggClientException;
}
