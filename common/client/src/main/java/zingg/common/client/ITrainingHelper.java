package zingg.common.client;

import java.util.List;

import zingg.common.client.pipe.Pipe;

public interface ITrainingHelper<S, D, R, C> {

	public void setMarkedRecordsStat(ZFrame<D, R, C> markedRecords);

	public Long getMarkedRecordsStat(ZFrame<D, R, C> markedRecords, long value);

	public Long getMatchedMarkedRecordsStat(ZFrame<D, R, C> markedRecords);

	public Long getUnmatchedMarkedRecordsStat(ZFrame<D, R, C> markedRecords);

	public Long getUnsureMarkedRecordsStat(ZFrame<D, R, C> markedRecords);

	public ZFrame<D, R, C> getClusterIdsFrame(ZFrame<D, R, C> lines);

	public List<R> getClusterIds(ZFrame<D, R, C> lines);

	public List<C> getDisplayColumns(ZFrame<D, R, C> lines, Arguments args);

	public ZFrame<D, R, C> getCurrentPair(ZFrame<D, R, C> lines, int index, List<R> clusterIds, ZFrame<D, R, C> clusterLines);

	public double getScore(ZFrame<D, R, C> currentPair);

	public double getPrediction(ZFrame<D, R, C> currentPair);

	public String getMsg1(int index, int totalPairs);

	public String getMsg2(double prediction, double score);

	public void displayRecords(ZFrame<D, R, C> records, String preMessage, String postMessage);

	public ZFrame<D, R, C> updateRecords(int matchValue, ZFrame<D, R, C> newRecords, ZFrame<D, R, C> updatedRecords);

	public void updateLabellerStat(int selected_option, int increment);

	public void printMarkedRecordsStat();

	public void writeLabelledOutput(ZFrame<D, R, C> records, Arguments args) throws ZinggClientException;
	
	public void writeLabelledOutput(ZFrame<D,R,C> records, Arguments args, Pipe p) throws ZinggClientException;
	
}