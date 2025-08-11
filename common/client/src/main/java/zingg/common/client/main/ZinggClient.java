package zingg.common.client.main;

import zingg.common.client.ZinggClientException;
import zingg.common.client.arguments.model.IZArgs;

public interface ZinggClient<S> {
    void init() throws ZinggClientException;
    void execute() throws ZinggClientException;
    void stop() throws ZinggClientException;
    void postMetrics() throws ZinggClientException;
    IZArgs getArguments();
    S getSession();
}
