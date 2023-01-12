package zingg;

import java.util.List;
import java.util.Scanner;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import zingg.client.ZFrame;
import zingg.client.ZinggClientException;
import zingg.client.ZinggOptions;
import zingg.client.pipe.Pipe;
import zingg.client.util.ColName;
import zingg.util.LabelMatchType;

public abstract class LabelUpdater<S,D,R,C,T> extends Labeller<S,D,R,C,T> {
	protected static String name = "zingg.LabelUpdater";
	public static final Log LOG = LogFactory.getLog(LabelUpdater.class);

	public LabelUpdater() {
		setZinggOptions(ZinggOptions.UPDATE_LABEL);
	}

	public void execute() throws ZinggClientException {
		try {
			LOG.info("Reading inputs for updateLabelling phase ...");
			ZFrame<D,R,C> markedRecords = getPipeUtil().read(false, false, getPipeUtil().getTrainingDataMarkedPipe(args));
			processRecordsCli(markedRecords);
			LOG.info("Finished updataLabelling phase");
		} catch (Exception e) {
			e.printStackTrace();
			throw new ZinggClientException(e.getMessage());
		}
	}

	public void processRecordsCli(ZFrame<D,R,C> lines) throws ZinggClientException {
		LOG.info("Processing Records for CLI updateLabelling");

		if (lines != null && lines.count() > 0) {
			getMarkedRecordsStat(lines);
			printMarkedRecordsStat();

			List<C> displayCols = getDSUtil().getFieldDefColumns(lines, args, false, args.getShowConcise());
			try {
				int matchFlag;
				ZFrame<D,R,C> updatedRecords = null;
				ZFrame<D,R,C> recordsToUpdate = lines;
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
					ZFrame<D,R,C> currentPair = lines.filter(lines.equalTo(ColName.CLUSTER_COLUMN, cluster_id));
					if (currentPair.isEmpty()) {
						System.out.println("\tInvalid cluster id. Enter '9' to exit");
						continue;
					}

					matchFlag = currentPair.getAsInt(currentPair.head(),ColName.MATCH_FLAG_COL);
					String preMsg = String.format("\n\tThe record pairs belonging to the input cluster id %s are:", cluster_id);
					String matchType = LabelMatchType.get(matchFlag).msg;
					postMsg = String.format("\tThe above pair is labeled as %s\n", matchType);
					selectedOption = displayRecordsAndGetUserInput(getDSUtil().select(currentPair, displayCols), preMsg, postMsg);
					updateLabellerStat(selectedOption, +1);
					updateLabellerStat(matchFlag, -1);
					printMarkedRecordsStat();
					if (selectedOption == 9) {
						LOG.info("User has quit in the middle. Updating the records.");
						break;
					}
					recordsToUpdate = recordsToUpdate
							.filter(recordsToUpdate.notEqual(ColName.CLUSTER_COLUMN,cluster_id));
					if (updatedRecords != null) {
						updatedRecords = updatedRecords
								.filter(updatedRecords.notEqual(ColName.CLUSTER_COLUMN,cluster_id));
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
				throw new ZinggClientException("An error while updating label", e);
			}
		} else {
			LOG.info("There is no marked record for updating. Please run findTrainingData/label jobs to generate training data.");
		}
	}

	


	protected Pipe<D,R,C> getOutputPipe() {
		Pipe<D,R,C> p = getPipeUtil().getTrainingDataMarkedPipe(args);
		p = setSaveModeOnPipe(p);
		return p;
	}

	protected abstract Pipe setSaveModeOnPipe(Pipe<D,R,C> p);
}