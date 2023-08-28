package zingg.common.core.executor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import zingg.common.client.Arguments;
import zingg.common.client.ClientOptions;
import zingg.common.client.ITrainingDataModel;
import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;
import zingg.common.client.ZinggOptions;
import zingg.common.client.pipe.Pipe;
import zingg.common.client.util.ColName;
import zingg.common.client.util.ColValues;
import zingg.common.core.Context;

public class TrainingDataModel<S,D,R,C,T> extends ZinggBase<S, D, R, C, T> implements ITrainingDataModel<S, D, R, C>{

	private static final long serialVersionUID = 1L;
	public static final Log LOG = LogFactory.getLog(TrainingDataModel.class);
	private long positivePairsCount, negativePairsCount, notSurePairsCount;
	private long totalCount;
	
	public TrainingDataModel(Context<S,D,R,C,T> context, ClientOptions clientOptions) {
		setContext(context);
		setZinggOptions(zinggOptions);
		setClientOptions(clientOptions);
		setName(this.getClass().getName());
	}
	
	
	public void setMarkedRecordsStat(ZFrame<D,R,C>  markedRecords) {
		if (markedRecords != null ) {
			positivePairsCount = getMatchedMarkedRecordsStat(markedRecords);
			negativePairsCount =  getUnmatchedMarkedRecordsStat(markedRecords);
			notSurePairsCount = getUnsureMarkedRecordsStat(markedRecords);
			totalCount = markedRecords.count() / 2;
		} 
	}
	
	
	
	public ZFrame<D,R,C> updateRecords(int matchValue, ZFrame<D,R,C> newRecords, ZFrame<D,R,C> updatedRecords) {
		newRecords = newRecords.withColumn(ColName.MATCH_FLAG_COL, matchValue);
		if (updatedRecords == null) {			
			updatedRecords = newRecords;
		} else {
			updatedRecords = updatedRecords.union(newRecords);
		}
		return updatedRecords;
	}

	
	

	
	public void updateLabellerStat(int selected_option, int increment) {
		totalCount += increment;
		if (selected_option == ColValues.MATCH_TYPE_MATCH) {
			positivePairsCount += increment;
		}
		else if (selected_option == ColValues.MATCH_TYPE_NOT_A_MATCH) {
			negativePairsCount += increment;
		}
		else if (selected_option == ColValues.MATCH_TYPE_NOT_SURE) {
			notSurePairsCount += increment;
		}	
	}

	
	public void writeLabelledOutput(ZFrame<D,R,C> records, Arguments args) throws ZinggClientException {
		Pipe p = getOutputPipe(args);
		writeLabelledOutput(records,args,p);
	}

	
	public void writeLabelledOutput(ZFrame<D,R,C> records, Arguments args, Pipe p) throws ZinggClientException {
		if (records == null) {
			LOG.warn("No labelled records");
			return;
		}
		getPipeUtil().write(records, args,p);
	}
	
	public Pipe getOutputPipe(Arguments args) {
		return getPipeUtil().getTrainingDataMarkedPipe(args);
	}
	
	
	@Override
	public void execute() throws ZinggClientException {
		throw new UnsupportedOperationException();		
	}


	@Override
	public ITrainingDataModel<S, D, R, C> getTrainingDataModel() throws UnsupportedOperationException {
		return this;
	}

	@Override
	public long getPositivePairsCount() {
		return positivePairsCount;
	}

	@Override
	public long getNegativePairsCount() {
		return negativePairsCount;
	}

	@Override
	public long getNotSurePairsCount() {
		return notSurePairsCount;
	}

	@Override
	public long getTotalCount() {
		return totalCount;
	}
	
	
	
	
}
