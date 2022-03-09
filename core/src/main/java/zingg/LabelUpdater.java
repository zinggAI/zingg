package zingg;

import java.util.List;
import java.util.Scanner;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.snowflake.snowpark_java.Column;
import com.snowflake.snowpark_java.DataFrame;
import com.snowflake.snowpark_java.SaveMode;
import com.snowflake.snowpark_java.Functions;

import zingg.client.ZinggClientException;
import zingg.client.ZinggOptions;
import zingg.client.pipe.Pipe;
import zingg.client.util.ColName;
import zingg.util.DSUtil;
import zingg.util.LabelMatchType;
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
			DataFrame markedRecords = PipeUtil.read(snow, false, false, PipeUtil.getTrainingDataMarkedPipe(args));
			processRecordsCli(markedRecords);
			LOG.info("Finished updataLabelling phase");
		} catch (Exception e) {
			e.printStackTrace();
			throw new ZinggClientException(e.getMessage());
		}
	}

	public void processRecordsCli(DataFrame lines) throws ZinggClientException {
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
			DataFrame updatedRecords = null;
			DataFrame recordsToUpdate = lines;
			int selectedOption = -1;
			String postMsg;

			Scanner sc = new Scanner(System.in);
			do {
				System.out.print("\n\tPlease enter the cluster id (or 9 to exit): ");
				String cluster_id = sc.next();
				if (cluster_id.equals("9")) {
					LOG.info("User has exit in the middle. Updating the records.");
					break;
				}
				DataFrame currentPair = lines.filter(lines.col(ColName.CLUSTER_COLUMN).in(cluster_id));
				if (currentPair.count() == 0) {
					System.out.println("\tInvalid cluster id. Enter '9' to exit");
					continue;
				}

				matchFlag = currentPair.first().get().getInt(DSUtil.getIndex(currentPair, ColName.MATCH_FLAG_COL));
				String preMsg = String.format("\n\tThe record pairs belonging to the input cluster id %s are:", cluster_id);
				String matchType = LabelMatchType.get(matchFlag).msg;
				postMsg = String.format("\tThe above pair is labeled as %s\n", matchType);
				selectedOption = displayRecordsAndGetUserInput(DSUtil.select(currentPair, displayCols), preMsg, postMsg);
				updateLabellerStat(selectedOption, +1);
				updateLabellerStat(matchFlag, -1);
				printMarkedRecordsStat();
				if (selectedOption == 9) {
					LOG.info("User has quit in the middle. Updating the records.");
					break;
				}
				recordsToUpdate = recordsToUpdate
						.filter(recordsToUpdate.col(ColName.CLUSTER_COLUMN).not_equal(Functions.lit(cluster_id)));
				if (updatedRecords != null) {
					updatedRecords = updatedRecords
							.filter(updatedRecords.col(ColName.CLUSTER_COLUMN).not_equal(Functions.lit(cluster_id)));
				}
				updatedRecords = updateRecords(selectedOption, currentPair, updatedRecords);
			} while (selectedOption != 9);

			if (updatedRecords != null) {
				updatedRecords = updatedRecords.union(recordsToUpdate);
			}
			writeLabelledOutput(updatedRecords);
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

	


	protected Pipe getOutputPipe() {
		Pipe p = PipeUtil.getTrainingDataMarkedPipe(args);
		p.setMode(SaveMode.Overwrite);
		return p;
	}
}