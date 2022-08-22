package zingg.client;
import org.apache.spark.sql.SparkSession;

public interface IZingg<D,R,C> {

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

    public void setSpark(SparkSession session);

	public void setClientOptions(ClientOptions clientOptions);

	public ClientOptions getClientOptions(); 

}
