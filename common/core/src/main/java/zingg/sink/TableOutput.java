package zingg.sink;

public class TableOutput {
	
	public int jobId;
	public long timestamp;
	public long clusterId;
	public String record;
	
	
	public TableOutput(int jobId, long timestamp, long clusterId, String record) {
		super();
		this.jobId = jobId;
		this.timestamp = timestamp;
		this.clusterId = clusterId;
		this.record = record;
	}
	
	public int getJobId() {
		return jobId;
	}
	public void setJobId(int jobId) {
		this.jobId = jobId;
	}
	public long getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	public long getClusterId() {
		return clusterId;
	}
	public void setClusterId(long clusterId) {
		this.clusterId = clusterId;
	}
	public String getRecord() {
		return record;
	}
	public void setRecord(String record) {
		this.record = record;
	}
	
	

}
