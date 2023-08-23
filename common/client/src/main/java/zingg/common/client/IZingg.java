package zingg.common.client;

import zingg.common.client.license.IZinggLicense;

public interface IZingg<S,D,R,C> {

	public void init(Arguments args, IZinggLicense license)
			throws ZinggClientException;

	public void execute() throws ZinggClientException;

	public void cleanup() throws ZinggClientException;

	//public ZinggOptions getZinggOptions();	

	public String getName();

	public void postMetrics();

	//** placing these methods for the assessModel phase */

	public ZFrame<D,R,C>  getMarkedRecords();

	public ZFrame<D,R,C>  getUnmarkedRecords();

	public Long getMarkedRecordsStat(ZFrame<D,R,C>  markedRecords, long value);

    public Long getMatchedMarkedRecordsStat(ZFrame<D,R,C>  markedRecords);

    public Long getUnmatchedMarkedRecordsStat(ZFrame<D,R,C>  markedRecords);

    public Long getUnsureMarkedRecordsStat(ZFrame<D,R,C>  markedRecords);

    //public void setSession(S session); // method name will have to be changed in Client too

	
	public void setClientOptions(ClientOptions clientOptions);

	public ClientOptions getClientOptions(); 

	public void setSession(S session);
	
	public ITrainingDataModel<S, D, R, C> getTrainingDataModel() throws UnsupportedOperationException;
	
	public ILabelDataViewHelper<S, D, R, C> getLabelDataViewHelper() throws UnsupportedOperationException;
	
}
