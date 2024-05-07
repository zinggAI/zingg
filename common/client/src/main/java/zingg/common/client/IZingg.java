package zingg.common.client;

public interface IZingg<S,D,R,C, A extends IZArgs<A>> {

	public void init(A args, S session)
			throws ZinggClientException;

	public void execute() throws ZinggClientException;

	public void cleanup() throws ZinggClientException;

	//public ZinggOptions getZinggOptions();	

	public String getName();

	public void postMetrics();

		
	public void setClientOptions(ClientOptions clientOptions);

	public ClientOptions getClientOptions(); 

	public void setSession(S session);
	
	
}
