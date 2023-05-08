package zingg.common.core.executor;

import java.io.Serializable;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import zingg.common.client.Arguments;
import zingg.common.client.ITrainingHelper;
import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;
import zingg.common.client.pipe.Pipe;
import zingg.common.client.util.ColName;
import zingg.common.client.util.ColValues;
import zingg.common.core.util.DSUtil;
import zingg.common.core.util.LabelMatchType;
import zingg.common.core.util.PipeUtilBase;

public class TrainingHelper<S,D,R,C>  implements Serializable, ITrainingHelper<S, D, R, C>{

	private static final long serialVersionUID = 1L;
	public static final Log LOG = LogFactory.getLog(TrainingHelper.class);
	private long positivePairsCount, negativePairsCount, notSurePairsCount;
	private long totalCount;

	private DSUtil<S,D,R,C> dsUtil;
	
	private PipeUtilBase<S,D,R,C> pipeUtil;
	
	
	public TrainingHelper() {
		super();
	}

	@Override
	public void setMarkedRecordsStat(ZFrame<D,R,C>  markedRecords) {
		if (markedRecords != null ) {
			positivePairsCount = getMatchedMarkedRecordsStat(markedRecords);
			negativePairsCount =  getUnmatchedMarkedRecordsStat(markedRecords);
			notSurePairsCount = getUnsureMarkedRecordsStat(markedRecords);
			totalCount = markedRecords.count() / 2;
		} 
	}
	
    @Override
	public Long getMarkedRecordsStat(ZFrame<D,R,C> markedRecords, long value) {
        return markedRecords.filter(markedRecords.equalTo(ColName.MATCH_FLAG_COL, value)).count() / 2;
    }

    @Override
	public Long getMatchedMarkedRecordsStat(ZFrame<D,R,C> markedRecords){
        return getMarkedRecordsStat(markedRecords, ColValues.MATCH_TYPE_MATCH);
    }
    
    @Override
	public Long getUnmatchedMarkedRecordsStat(ZFrame<D,R,C> markedRecords){
        return getMarkedRecordsStat(markedRecords, ColValues.MATCH_TYPE_NOT_A_MATCH);
    }

    @Override
	public Long getUnsureMarkedRecordsStat(ZFrame<D,R,C> markedRecords){
        return getMarkedRecordsStat(markedRecords, ColValues.MATCH_TYPE_NOT_SURE);
    }
	
	@Override
	public ZFrame<D,R,C>  getClusterIdsFrame(ZFrame<D,R,C>  lines) {
		return 	lines.select(ColName.CLUSTER_COLUMN).distinct();		
	}

	@Override
	public List<R>  getClusterIds(ZFrame<D,R,C>  lines) {
		return 	lines.collectAsList();		
	}

	@Override
	public List<C> getDisplayColumns(ZFrame<D,R,C>  lines, Arguments args) {
		return getDSUtil().getFieldDefColumns(lines, args, false, args.getShowConcise());
	}

	@Override
	public ZFrame<D,R,C>  getCurrentPair(ZFrame<D,R,C>  lines, int index, List<R>  clusterIds, ZFrame<D,R,C>  clusterLines) {
		return lines.filter(lines.equalTo(ColName.CLUSTER_COLUMN,
			clusterLines.getAsString(clusterIds.get(index), ColName.CLUSTER_COLUMN))).cache();
	}

	@Override
	public double getScore(ZFrame<D,R,C>  currentPair) {
		return currentPair.getAsDouble(currentPair.head(),ColName.SCORE_COL);
	}

	@Override
	public double getPrediction(ZFrame<D,R,C>  currentPair) {
		return currentPair.getAsDouble(currentPair.head(), ColName.PREDICTION_COL);
	}

	@Override
	public String getMsg1(int index, int totalPairs) {
		return String.format("\tCurrent labelling round  : %d/%d pairs labelled\n", index, totalPairs);
	}

	@Override
	public String getMsg2(double prediction, double score) {
		String msg2 = "";
		String matchType = LabelMatchType.get(prediction).msg;
		if (prediction == ColValues.IS_NOT_KNOWN_PREDICTION) {
			msg2 = String.format(
					"\tZingg does not do any prediction for the above pairs as Zingg is still collecting training data to build the preliminary models.");
		} else {
			msg2 = String.format("\tZingg predicts the above records %s with a similarity score of %.2f",
					matchType, Math.floor(score * 100) * 0.01);
		}
		return msg2;
	}
	
	@Override
	public void displayRecords(ZFrame<D, R, C> records, String preMessage, String postMessage) {
		//System.out.println();
		System.out.println(preMessage);
		records.show(false);
		System.out.println(postMessage);
		System.out.println("\tWhat do you think? Your choices are: ");
		System.out.println();
		
		System.out.println("\tNo, they do not match : 0");
		System.out.println("\tYes, they match       : 1");
		System.out.println("\tNot sure              : 2");
		System.out.println();
		System.out.println("\tTo exit               : 9");
		System.out.println();
		System.out.print("\tPlease enter your choice [0,1,2 or 9]: ");		
	}

	@Override
	public ZFrame<D,R,C> updateRecords(int matchValue, ZFrame<D,R,C> newRecords, ZFrame<D,R,C> updatedRecords) {
		newRecords = newRecords.withColumn(ColName.MATCH_FLAG_COL, matchValue);
		if (updatedRecords == null) {			
			updatedRecords = newRecords;
		} else {
			updatedRecords = updatedRecords.union(newRecords);
		}
		return updatedRecords;
	}

	
	

	@Override
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

	@Override
	public void printMarkedRecordsStat() {
		String msg = String.format(
				"\tLabelled pairs so far    : %d/%d MATCH, %d/%d DO NOT MATCH, %d/%d NOT SURE", positivePairsCount, totalCount,
				negativePairsCount, totalCount, notSurePairsCount, totalCount);
				
		System.out.println();		
		System.out.println();
		System.out.println();					
		System.out.println(msg);
	}
	
	@Override
	public void writeLabelledOutput(ZFrame<D,R,C> records, Arguments args) throws ZinggClientException {
		Pipe p = getOutputPipe(args);
		writeLabelledOutput(records,args,p);
	}

	@Override
	public void writeLabelledOutput(ZFrame<D,R,C> records, Arguments args, Pipe p) throws ZinggClientException {
		if (records == null) {
			LOG.warn("No records to be labelled.");
			return;
		}
		getPipeUtil().write(records, args,p);
	}
	
	protected Pipe getOutputPipe(Arguments args) {
		return getPipeUtil().getTrainingDataMarkedPipe(args);
	}

	public DSUtil<S,D,R,C> getDSUtil() {
        return dsUtil;
    }
    
	public void setDSUtil(DSUtil<S, D, R, C> dsUtil) {
		this.dsUtil = dsUtil;
	}
 
	public PipeUtilBase<S,D,R,C> getPipeUtil() {
        return pipeUtil;
    }

	public void setPipeUtil(PipeUtilBase<S, D, R, C> pipeUtil) {
		this.pipeUtil = pipeUtil;
	}
	
}


