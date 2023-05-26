package zingg.common.core.executor;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import zingg.common.client.Arguments;
import zingg.common.client.ClientOptions;
import zingg.common.client.ILabelDataViewHelper;
import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;
import zingg.common.client.ZinggOptions;
import zingg.common.client.util.ColName;
import zingg.common.client.util.ColValues;
import zingg.common.core.Context;
import zingg.common.core.util.LabelMatchType;

public class LabelDataViewHelper<S,D,R,C,T> extends ZinggBase<S, D, R, C, T> implements ILabelDataViewHelper<S, D, R, C> {

	private static final long serialVersionUID = 1L;
	public static final Log LOG = LogFactory.getLog(LabelDataViewHelper.class);
	
	public LabelDataViewHelper(Context<S,D,R,C,T> context, ZinggOptions zinggOptions, ClientOptions clientOptions) {
		setContext(context);
		setZinggOptions(zinggOptions);
		setClientOptions(clientOptions);
		setName(this.getClass().getName());
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
	public void printMarkedRecordsStat(long positivePairsCount,long negativePairsCount,long notSurePairsCount,long totalCount) {
		String msg = String.format(
				"\tLabelled pairs so far    : %d/%d MATCH, %d/%d DO NOT MATCH, %d/%d NOT SURE", positivePairsCount, totalCount,
				negativePairsCount, totalCount, notSurePairsCount, totalCount);
				
		System.out.println();		
		System.out.println();
		System.out.println();					
		System.out.println(msg);
	}
	
	
	
	@Override
	public void execute() throws ZinggClientException {
		throw new UnsupportedOperationException();		
	}

	@Override
	public ILabelDataViewHelper<S, D, R, C> getLabelDataViewHelper() throws UnsupportedOperationException {
		return this;
	}
	
}
