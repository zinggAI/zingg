package zingg.client;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

public interface IZingg {

	public void init(Arguments args, String license)
			throws ZinggClientException;

	public void execute() throws ZinggClientException;

	public void cleanup() throws ZinggClientException;

	public ZinggOptions getZinggOptions();	

	public String getName();

	public void postMetrics();

	//** placing these methods for the assessModel phase */

	public Dataset<Row> getMarkedRecords();

	public Dataset<Row> getUnmarkedRecords();

	public Long getMarkedRecordsStat(Dataset<Row> markedRecords, long value);

    public Long getMatchedMarkedRecordsStat(Dataset<Row> markedRecords);

    public Long getUnmatchedMarkedRecordsStat(Dataset<Row> markedRecords);

    public Long getUnsureMarkedRecordsStat(Dataset<Row> markedRecords);

}
