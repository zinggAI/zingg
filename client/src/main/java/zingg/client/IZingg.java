package zingg.client;

public interface IZingg<S,D,R,C,T> {

	public void init(Arguments args, String license)
			throws ZinggClientException;

	public void execute() throws ZinggClientException;

	public void cleanup() throws ZinggClientException;

	public ZinggOptions getZinggOptions();	

	public String getName();

	public void postMetrics();

	//** placing these methods for the assessModel phase */

	public ZFrame<D,R,C>  getMarkedRecords();

	public ZFrame<D,R,C>  getUnmarkedRecords();

	public Long getMarkedRecordsStat(ZFrame<D,R,C>  markedRecords, long value);

    public Long getMatchedMarkedRecordsStat(ZFrame<D,R,C>  markedRecords);

    public Long getUnmatchedMarkedRecordsStat(ZFrame<D,R,C>  markedRecords);

    public Long getUnsureMarkedRecordsStat(ZFrame<D,R,C>  markedRecords);

    public void setSession(S session); // method name will have to be changed in Client too

	public void setClientOptions(ClientOptions clientOptions);

	public ClientOptions getClientOptions(); 

}
