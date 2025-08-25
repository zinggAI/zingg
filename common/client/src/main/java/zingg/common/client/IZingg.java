package zingg.common.client;

import zingg.common.client.arguments.model.IZArgs;

public interface IZingg<S,D,R,C> extends IZinggModelInfo<S, D, R, C> {

	void init(IZArgs args, S session, ClientOptions options)
			throws ZinggClientException;

	void execute() throws ZinggClientException;

	void cleanup() throws ZinggClientException;

	String getName();

	void postMetrics();

	void setClientOptions(ClientOptions clientOptions);

	ClientOptions getClientOptions();

	void setSession(S session);

	IZArgs getArgs();

	void setArgs(IZArgs a);
	
	
}
