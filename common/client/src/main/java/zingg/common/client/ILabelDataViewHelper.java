package zingg.common.client;

import java.util.List;

public interface ILabelDataViewHelper<S, D, R, C> {

	ZFrame<D, R, C> getClusterIdsFrame(ZFrame<D, R, C> lines);

	List<R> getClusterIds(ZFrame<D, R, C> lines);

	List<C> getDisplayColumns(ZFrame<D, R, C> lines, Arguments args);

	ZFrame<D, R, C> getCurrentPair(ZFrame<D, R, C> lines, int index, List<R> clusterIds, ZFrame<D, R, C> clusterLines);

	double getScore(ZFrame<D, R, C> currentPair);

	double getPrediction(ZFrame<D, R, C> currentPair);

	String getMsg1(int index, int totalPairs);

	String getMsg2(double prediction, double score);

	void displayRecords(ZFrame<D, R, C> records, String preMessage, String postMessage);

	void printMarkedRecordsStat(long positivePairsCount, long negativePairsCount, long notSurePairsCount,
			long totalCount);

}