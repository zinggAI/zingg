package zingg;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.functions;

import zingg.client.ZinggClientException;
import zingg.client.ZinggOptions;
import zingg.client.pipe.Pipe;
import zingg.client.util.ColName;
import zingg.client.util.ColValues;
import zingg.util.DSUtil;
import zingg.util.PipeUtil;
import zingg.util.LabelMatchType;

public class Labeller extends ZinggBase {

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
			Dataset<Row> unmarkedRecords = getUnmarkedRecords();
			processRecordsCli(unmarkedRecords);
			LOG.info("Finished labelling phase");
		} catch (Exception e) {
			e.printStackTrace();
			throw new ZinggClientException(e.getMessage());
		}
	}

	public Dataset<Row> getUnmarkedRecords() throws ZinggClientException {
		Dataset<Row> unmarkedRecords = null;
		Dataset<Row> markedRecords = null;
		try {
			unmarkedRecords = PipeUtil.read(spark, false, false, PipeUtil.getTrainingDataUnmarkedPipe(args));
			try {
				markedRecords = PipeUtil.read(spark, false, false, PipeUtil.getTrainingDataMarkedPipe(args));
			} catch (ZinggClientException e) {
				LOG.warn("No record has been marked yet");
			}
			if (markedRecords != null ) {
				unmarkedRecords = unmarkedRecords.join(markedRecords,
						unmarkedRecords.col(ColName.CLUSTER_COLUMN).equalTo(markedRecords.col(ColName.CLUSTER_COLUMN)),
						"left_anti");
						getMarkedRecordsStat(markedRecords);
			} 
		} catch (ZinggClientException e) {
			LOG.warn("No unmarked record for labelling");
		}
		return unmarkedRecords;
	}

	protected void getMarkedRecordsStat(Dataset<Row> markedRecords) {
		positivePairsCount = markedRecords.filter(markedRecords.col(ColName.MATCH_FLAG_COL).equalTo(ColValues.MATCH_TYPE_MATCH)).count() / 2;
		negativePairsCount = markedRecords.filter(markedRecords.col(ColName.MATCH_FLAG_COL).equalTo(ColValues.MATCH_TYPE_NOT_A_MATCH)).count() / 2;
		notSurePairsCount = markedRecords.filter(markedRecords.col(ColName.MATCH_FLAG_COL).equalTo(ColValues.MATCH_TYPE_NOT_SURE)).count() / 2;
		totalCount = markedRecords.count() / 2;
	}

	public void processRecordsCli(Dataset<Row> lines) throws ZinggClientException {
		LOG.info("Processing Records for CLI Labelling");
		if (lines == null || lines.count() == 0) {
			LOG.info("It seems there are no unmarked records at this moment. Please run findTrainingData job to build some pairs to be labelled and then run this labeler.");
			return;
		}
		printMarkedRecordsStat();

		lines = lines.cache();
		List<Column> displayCols = DSUtil.getFieldDefColumns(lines, args, false, args.getShowConcise());

		List<Row> clusterIDs = lines.select(ColName.CLUSTER_COLUMN).distinct().collectAsList();
		try {
			double score;
			double prediction;
			Dataset<Row> updatedRecords = null;
			int selected_option = -1;
			String msg1, msg2;
			int totalPairs = clusterIDs.size();
			
			for (int index = 0; index < totalPairs; index++){	
				Dataset<Row> currentPair = lines.filter(lines.col(ColName.CLUSTER_COLUMN).equalTo(
						clusterIDs.get(index).getAs(ColName.CLUSTER_COLUMN))).cache();
				
				score = currentPair.head().getAs(ColName.SCORE_COL);
				prediction = currentPair.head().getAs(ColName.PREDICTION_COL);
	
				msg1 = String.format("\tCurrent labelling round  : %d/%d pairs labelled\n", index, totalPairs);
				String matchType = LabelMatchType.get(prediction).msg;				
				if (prediction == ColValues.IS_NOT_KNOWN_PREDICTION) {
					msg2 = String.format(
							"\tZingg does not do any prediction for the above pairs as Zingg is still collecting training data to build the preliminary models.");
				} else {
					msg2 = String.format("\tZingg predicts the above records %s with a similarity score of %.2f",
							matchType, Math.floor(score * 100) * 0.01);
				}
				//String msgHeader = msg1 + msg2;

				selected_option = displayRecordsAndGetUserInput(DSUtil.select(currentPair, displayCols), msg1, msg2);
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
			throw new ZinggClientException(e.getMessage());
		}
	}

	
	protected int displayRecordsAndGetUserInput(Dataset<Row> records, String preMessage, String postMessage) {
		//System.out.println();
		System.out.println(preMessage);
		records.show(false);
		System.out.println(postMessage);
		System.out.println("\tWhat do you think? Your choices are: ");
		int selection = readCliInput();
		return selection;
	}

	protected Dataset<Row> updateRecords(int matchValue, Dataset<Row> newRecords, Dataset<Row> updatedRecords) {
		newRecords = newRecords.withColumn(ColName.MATCH_FLAG_COL, functions.lit(matchValue));
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

	protected void writeLabelledOutput(Dataset<Row> records) throws ZinggClientException {
		if (records == null) {
			LOG.warn("No records to be labelled.");
			return;
		}		
		PipeUtil.write(records, args, ctx, getOutputPipe());
	}

	protected Pipe getOutputPipe() {
		return PipeUtil.getTrainingDataMarkedPipe(args);
	}
}


