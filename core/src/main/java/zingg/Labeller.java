package zingg;

import java.util.List;
import java.util.Scanner;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.functions;

import zingg.client.Arguments;
import zingg.client.ZFrame;
import zingg.client.ZinggClientException;
import zingg.client.ZinggOptions;
import zingg.client.pipe.Pipe;
import zingg.client.util.ColName;
import zingg.client.util.ColValues;
import zingg.util.LabelMatchType;

public abstract class Labeller<S,D,R,C,T1,T2> extends ZinggBase<S,D,R,C,T1,T2> {

	protected static String name = "zingg.Labeller";
	public static final Log LOG = LogFactory.getLog(Labeller.class);
	long positivePairsCount, negativePairsCount, notSurePairsCount;
	long totalCount;

	public Labeller() {
		setZinggOptions(ZinggOptions.LABEL);
	}

	public void execute() throws ZinggClientException {
		try {
			LOG.info("Reading inputs for labelling phase ...");
			getMarkedRecordsStat(getMarkedRecords());
			ZFrame<D,R,C>  unmarkedRecords = getUnmarkedRecords();
			processRecordsCli(unmarkedRecords);
			LOG.info("Finished labelling phase");
		} catch (Exception e) {
			e.printStackTrace();
			throw new ZinggClientException(e.getMessage());
		}
	}

	protected void getMarkedRecordsStat(ZFrame<D,R,C>  markedRecords) {
		if (markedRecords != null ) {
			positivePairsCount = getMatchedMarkedRecordsStat(markedRecords);
			negativePairsCount =  getUnmatchedMarkedRecordsStat(markedRecords);
			notSurePairsCount = getUnsureMarkedRecordsStat(markedRecords);
			totalCount = markedRecords.count() / 2;
		} 
	}
	
	public ZFrame<D,R,C> getUnmarkedRecords() {
		ZFrame<D,R,C> unmarkedRecords = null;
		ZFrame<D,R,C> markedRecords = null;
		try {
			unmarkedRecords = getPipeUtil().read(false, false, getPipeUtil().getTrainingDataUnmarkedPipe(args));
			try {
				markedRecords = getPipeUtil().read(false, false, getPipeUtil().getTrainingDataMarkedPipe(args));
			} catch (Exception e) {
				LOG.warn("No record has been marked yet");
			}
			if (markedRecords != null ) {
				unmarkedRecords = unmarkedRecords.join(markedRecords,ColName.CLUSTER_COLUMN, false,
						"left_anti");
				getMarkedRecordsStat(markedRecords);
			} 
		} catch (Exception e) {
			LOG.warn("No unmarked record for labelling");
		}
		return unmarkedRecords;
	}

	public List<R>  getClusterIds(ZFrame<D,R,C>  lines) {
		return 	lines.select(ColName.CLUSTER_COLUMN).distinct().collectAsList();		
	}

	public List<C> getDisplayColumns(ZFrame<D,R,C>  lines, Arguments args) {
		return getDSUtil().getFieldDefColumns(lines, args, false, args.getShowConcise());
	}

	public ZFrame<D,R,C>  getCurrentPair(ZFrame<D,R,C>  lines, int index, List<R>  clusterIds) {
		return lines.filter(lines.col(ColName.CLUSTER_COLUMN).equalTo(
			clusterIds.get(index).getAs(ColName.CLUSTER_COLUMN))).cache();
	}

	public double getScore(ZFrame<D,R,C>  currentPair) {
		return currentPair.head().getAs(ColName.SCORE_COL);
	}

	public double getPrediction(ZFrame<D,R,C>  currentPair) {
		return currentPair.head().getAs(ColName.PREDICTION_COL);
	}

	public String getMsg1(int index, int totalPairs) {
		return String.format("\tCurrent labelling round  : %d/%d pairs labelled\n", index, totalPairs);
	}

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

	public void processRecordsCli(ZFrame<D,R,C>  lines) throws ZinggClientException {
		LOG.info("Processing Records for CLI Labelling");
		if (lines != null && lines.count() > 0) {
			printMarkedRecordsStat();

			lines = lines.cache();
			List<C> displayCols = getDisplayColumns(lines, args);
			List<R>  clusterIDs = getClusterIds(lines);
			try {
				double score;
				double prediction;
				ZFrame<D,R,C>  updatedRecords = null;
				int selected_option = -1;
				String msg1, msg2;
				int totalPairs = clusterIDs.size();

				for (int index = 0; index < totalPairs; index++) {
					ZFrame<D,R,C>  currentPair = getCurrentPair(lines, index, clusterIDs);

					score = getScore(currentPair);
					prediction = getPrediction(currentPair);

					msg1 = getMsg1(index, totalPairs);
					msg2 = getMsg2(prediction, score);
					//String msgHeader = msg1 + msg2;

					selected_option = displayRecordsAndGetUserInput(getDSUtil().select(currentPair, displayCols), msg1, msg2);
					updateLabellerStat(selected_option, 1);
					printMarkedRecordsStat();
					if (selected_option == 9) {
						LOG.info("User has quit in the middle. Updating the records.");
						break;
					}
					updatedRecords = updateRecords(selected_option, currentPair, updatedRecords);
				}
				writeLabelledOutput(updatedRecords);
				LOG.warn("Processing finished.");
			} catch (Exception e) {
				if (LOG.isDebugEnabled()) {
					e.printStackTrace();
				}
				LOG.warn("Labelling error has occured " + e.getMessage());
				throw new ZinggClientException("An error has occured while Labelling.", e);
			}
		} else {
			LOG.info("It seems there are no unmarked records at this moment. Please run findTrainingData job to build some pairs to be labelled and then run this labeler.");
		}
	}

	
	protected int displayRecordsAndGetUserInput(ZFrame<D,R,C> records, String preMessage, String postMessage) {
		//System.out.println();
		System.out.println(preMessage);
		records.show(false);
		System.out.println(postMessage);
		System.out.println("\tWhat do you think? Your choices are: ");
		int selection = readCliInput();
		return selection;
	}

	protected ZFrame<D,R,C> updateRecords(int matchValue, ZFrame<D,R,C> newRecords, ZFrame<D,R,C> updatedRecords) {
		newRecords = newRecords.withColumn(ColName.MATCH_FLAG_COL, matchValue);
		if (updatedRecords == null) {
			updatedRecords = newRecords;
		} else {
			updatedRecords = updatedRecords.union(newRecords);
		}
		return updatedRecords;
	}

	
	

	int readCliInput() {
		Scanner sc = new Scanner(System.in);
		System.out.println();
		
		System.out.println("\tNo, they do not match : 0");
		System.out.println("\tYes, they match       : 1");
		System.out.println("\tNot sure              : 2");
		System.out.println();
		System.out.println("\tTo exit               : 9");
		System.out.println();
		System.out.print("\tPlease enter your choice [0,1,2 or 9]: ");

		while (!sc.hasNext("[0129]")) {
			sc.next();
			System.out.println("Nope, please enter one of the allowed options!");
		}
		String word = sc.next();
		int selection = Integer.parseInt(word);
		// sc.close();

		return selection;
	}

	protected void updateLabellerStat(int selected_option, int increment) {
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

	protected void printMarkedRecordsStat() {
		String msg = String.format(
				"\tLabelled pairs so far    : %d/%d MATCH, %d/%d DO NOT MATCH, %d/%d NOT SURE", positivePairsCount, totalCount,
				negativePairsCount, totalCount, notSurePairsCount, totalCount);
				
		System.out.println();		
		System.out.println();
		System.out.println();					
		System.out.println(msg);
	}

	protected void writeLabelledOutput(ZFrame<D,R,C> records) throws ZinggClientException {
		if (records == null) {
			LOG.warn("No records to be labelled.");
			return;
		}		
		getPipeUtil().write(records, args,getOutputPipe());
	}

	protected Pipe getOutputPipe() {
		return getPipeUtil().getTrainingDataMarkedPipe(args);
	}
}


