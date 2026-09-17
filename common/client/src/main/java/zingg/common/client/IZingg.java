package zingg.common.client;

import java.util.Map;

import zingg.common.client.arguments.model.IZArgs;

public interface IZingg<S,D,R,C> extends IZinggModelInfo<S, D, R, C> {

	public void init(IZArgs args, S session, ClientOptions options)
			throws ZinggClientException;

	public void execute() throws ZinggClientException;

	/**
	 * Record labels collected outside this process, keyed by cluster id.
	 * Only the labelling phases support this.
	 */
	public void applyLabels(Map<String, Integer> clusterLabels) throws ZinggClientException;

	public void cleanup() throws ZinggClientException;

	//public ZinggOptions getZinggOptions();	

	public String getName();

	public void postMetrics();

		
	public void setClientOptions(ClientOptions clientOptions);

	public ClientOptions getClientOptions(); 

	public void setSession(S session);

	public IZArgs getArgs();

	public void setArgs(IZArgs a);
	
	
}
