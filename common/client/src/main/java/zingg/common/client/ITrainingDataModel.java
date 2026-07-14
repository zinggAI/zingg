package zingg.common.client;

import zingg.common.client.arguments.model.IArguments;
import zingg.common.client.pipe.Pipe;

public interface ITrainingDataModel<S, D, R, C> {

	public void setMarkedRecordsStat(ZFrame<D, R, C> markedRecords);

	public ZFrame<D, R, C> updateRecords(int matchValue, ZFrame<D, R, C> newRecords, ZFrame<D, R, C> updatedRecords);

	public void updateLabellerStat(int selected_option, int increment);

	public void writeLabelledOutput(ZFrame<D, R, C> records, IArguments args) throws ZinggClientException;
	
	public void writeLabelledOutput(ZFrame<D,R,C> records, IArguments args, Pipe p) throws ZinggClientException;
	
	public long getPositivePairsCount();

	public long getNegativePairsCount();
	
	public long getNotSurePairsCount() ;

	public long getTotalCount();
	
	
}