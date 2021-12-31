package zingg;

import java.util.List;
import java.util.Scanner;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SaveMode;

import zingg.client.ZinggClientException;
import zingg.client.ZinggOptions;
import zingg.client.pipe.Pipe;
import zingg.client.util.ColName;
import zingg.client.util.ColValues;
import zingg.util.DSUtil;
import zingg.util.PipeUtil;

public class LabelUpdater extends Labeller {
	protected static String name = "zingg.LabelUpdater";
	public static final Log LOG = LogFactory.getLog(LabelUpdater.class);

	public LabelUpdater() {
		setZinggOptions(ZinggOptions.UPDATE_LABEL);
	}

	public void execute() throws ZinggClientException {
		try {
			LOG.info("Reading inputs for updateLabelling phase ...");
			Dataset<Row> markedRecords = PipeUtil.read(spark, false, false, PipeUtil.getTrainingDataMarkedPipe(args));
			processRecordsCli(markedRecords);
			LOG.info("Finished updataLabelling phase");
		} catch (Exception e) {
			e.printStackTrace();
			throw new ZinggClientException(e.getMessage());
		}
	}

	public void processRecordsCli(Dataset<Row> lines) throws ZinggClientException {
		LOG.info("Processing Records for CLI updateLabelling");
		getMarkedRecordsStat(lines);
		printMarkedRecordsStat();
		if (lines == null || lines.count() == 0) {
			LOG.info("There is no marked record for updating. Please run findTrainingData/label jobs to generate training data.");
			return;
		}

		List<Column> displayCols = DSUtil.getFieldDefColumns(lines, args, false);
		try {
			int matchFlag;
			Dataset<Row> updatedRecords = null;
			Dataset<Row> recordsToUpdate = lines;
			int selectedOption = -1;
			String postMsg;

			Scanner sc = new Scanner(System.in);
			do {
				System.out.print("\tPlease enter the cluster id (or '9' to exit): ");
				String cluster_id = sc.next();
				if (cluster_id.equals("9")) {
					LOG.info("User has exit in the middle. Updating the records.");
					break;
				}
				Dataset<Row> currentPair = lines.filter(lines.col(ColName.CLUSTER_COLUMN).equalTo(cluster_id));
				if (currentPair.isEmpty()) {
					System.out.println("\tInvalid cluster id. Enter '9' to exit");
					continue;
				}

				matchFlag = currentPair.head().getAs(ColName.MATCH_FLAG_COL);
				postMsg = String.format("\tCurrent Match type for the above pair is %d\n", matchFlag);
				selectedOption = displayRecordsAndGetUserInput(DSUtil.select(currentPair, displayCols), "", postMsg);
				updateLabellerStat(selectedOption, matchFlag);
				printMarkedRecordsStat();
				if (selectedOption == 9) {
					LOG.info("User has quit in the middle. Updating the records.");
					break;
				}
				recordsToUpdate = recordsToUpdate
						.filter(recordsToUpdate.col(ColName.CLUSTER_COLUMN).notEqual(cluster_id));
				if (updatedRecords != null) {
					updatedRecords = updatedRecords
							.filter(updatedRecords.col(ColName.CLUSTER_COLUMN).notEqual(cluster_id));
				}
				updatedRecords = updateRecords(selectedOption, currentPair, updatedRecords);
			} while (selectedOption != 9);

			if (updatedRecords != null) {
				updatedRecords = updatedRecords.union(recordsToUpdate);
			}
			writeLabelledOutput(updatedRecords, SaveMode.Overwrite);
			sc.close();
			LOG.info("Processing finished.");
		} catch (Exception e) {
			if (LOG.isDebugEnabled()) {
				e.printStackTrace();
			}
			LOG.warn("An error has occured while Updating Label. " + e.getMessage());
			throw new ZinggClientException(e.getMessage());
		}
		return;
	}

	private void updateLabellerStat(int selectedOption, int existingType) {
		--totalCount;
		if (existingType == ColValues.MATCH_TYPE_MATCH) {
			--positivePairsCount;
		}
		else if (existingType == ColValues.MATCH_TYPE_NOT_A_MATCH) {
			--negativePairsCount;
		}
		else if (existingType == ColValues.MATCH_TYPE_NOT_SURE) {
			--notSurePairsCount;
		}
		updateLabellerStat(selectedOption);
	}

	void writeLabelledOutput(Dataset<Row> records, SaveMode mode) {
		if (records == null) {
			LOG.warn("No marked record has been updated.");
			return;
		}
		Pipe p = PipeUtil.getTrainingDataMarkedPipe(args);
		p.setMode(mode);
		PipeUtil.write(records, args, ctx, p);
	}
}