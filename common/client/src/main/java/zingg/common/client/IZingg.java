package zingg.common.client;

public interface IZingg<S,D,R,C> extends IZinggSuper<S, D, R, C>{

	public void init(IZArgs args, S session, ClientOptions options)
			throws ZinggClientException;

	public void execute() throws ZinggClientException;

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
