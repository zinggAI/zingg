package zingg.common.core.executor;

import java.util.Scanner;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;
import zingg.common.client.cols.ZidAndFieldDefSelector;
import zingg.common.client.options.ZinggOptions;
import zingg.common.client.pipe.Pipe;
import zingg.common.client.util.ColName;
import zingg.common.core.util.LabelMatchType;

public abstract class LabelUpdater<S,D,R,C,T> extends Labeller<S,D,R,C,T> {
	private static final long serialVersionUID = 1L;
	protected static String name = "zingg.common.core.executor.LabelUpdater";
	public static final Log LOG = LogFactory.getLog(LabelUpdater.class);

	public LabelUpdater() {
		setZinggOption(ZinggOptions.UPDATE_LABEL);
	}

	public void execute() throws ZinggClientException {
		try {
			LOG.info("Reading inputs for updateLabelling phase ...");
			ZFrame<D,R,C> markedRecords = getPipeUtil().read(false, false, getModelHelper().getTrainingDataMarkedPipe(args));
			processRecordsCli(markedRecords);
			LOG.info("Finished updataLabelling phase");
		} catch (Exception e) {
			e.printStackTrace();
			throw new ZinggClientException(e.getMessage());
		}
	}

	public ZFrame<D,R,C> processRecordsCli(ZFrame<D,R,C> lines) throws ZinggClientException {
		LOG.info("Processing Records for CLI updateLabelling");

		if (lines != null && lines.count() > 0) {
			getTrainingDataModel().setMarkedRecordsStat(lines);
			getLabelDataViewHelper().printMarkedRecordsStat(
					getTrainingDataModel().getPositivePairsCount(),
					getTrainingDataModel().getNegativePairsCount(),
					getTrainingDataModel().getNotSurePairsCount(),
					getTrainingDataModel().getTotalCount()
					);
			
			try {
				return update(lines);
			} catch (Exception e) {
				if (LOG.isDebugEnabled()) {
					e.printStackTrace();
				}
				LOG.warn("An error has occured while Updating Label. " + e.getMessage());
				throw new ZinggClientException("An error while updating label", e);
			}
		} else {
			LOG.info("There is no marked record for updating. Please run findTrainingData/label jobs to generate training data.");
			return null;
		}
	}

	protected ZFrame<D, R, C> update(ZFrame<D, R, C> lines) throws ZinggClientException {
		ZFrame<D,R,C> updatedRecords = null;
		ZFrame<D,R,C> recordsToUpdate = lines;
		int selectedOption = -1;

		Scanner sc = new Scanner(System.in);
		do {
			System.out.print("\n\tPlease enter the cluster id (or 9 to exit): ");
			String cluster_id = sc.next();
			if (cluster_id.equals(QUIT_LABELING.toString())) {
				LOG.info("User has exit in the middle. Updating the records.");
				break;
			}
			ZFrame<D,R,C> currentPair = lines.filter(lines.equalTo(ColName.CLUSTER_COLUMN, cluster_id));
			if (currentPair.isEmpty()) {
				System.out.println("\tInvalid cluster id. Enter '9' to exit");
				continue;
			}
			
			selectedOption = getUserInput(lines, currentPair,cluster_id);
			
			if (selectedOption == QUIT_LABELING) {
				LOG.info("User has quit in the middle. Updating the records.");
				break;
			}
			// as feedback has been taken for this cluster, remove from DF of remaining clusters
			recordsToUpdate = removeCluster(recordsToUpdate, cluster_id);
			updatedRecords = getUpdatedRecords(updatedRecords, selectedOption, cluster_id, currentPair);
		} while (selectedOption != QUIT_LABELING);

		sc.close();
		
		return writeUpdatedRecords(updatedRecords, recordsToUpdate);
	}

	protected ZFrame<D, R, C> removeCluster(ZFrame<D, R, C> recordsToUpdate, String cluster_id) {
		return recordsToUpdate
				.filter(recordsToUpdate.notEqual(ColName.CLUSTER_COLUMN,cluster_id));
	}

	protected ZFrame<D, R, C> writeUpdatedRecords(ZFrame<D, R, C> updatedRecords, ZFrame<D, R, C> recordsToUpdate)
			throws ZinggClientException {
		// combine those which need to be updated with those which were not touched
		if (updatedRecords != null) {
			updatedRecords = updatedRecords.union(recordsToUpdate);
		}
		getTrainingDataModel().writeLabelledOutput(updatedRecords,args,getOutputPipe());
		
		LOG.info("Processing finished.");
		return updatedRecords;
	}

	protected ZFrame<D, R, C> getUpdatedRecords(ZFrame<D, R, C> updatedRecords, int selectedOption, String cluster_id,
			ZFrame<D, R, C> currentPair) {
		// remove if this cluster already present in updated records DF
		if (updatedRecords != null) {
			updatedRecords = removeCluster(updatedRecords, cluster_id);
		}
		// update the match flag and add to DF of records which need to be updated
		updatedRecords = getTrainingDataModel().updateRecords(selectedOption, currentPair, updatedRecords);
		return updatedRecords;
	}

	protected int getUserInput(ZFrame<D,R,C> lines,ZFrame<D,R,C> currentPair,String cluster_id) {
//		List<C> displayCols = getDSUtil().getFieldDefColumns(lines, args, false, args.getShowConcise());
		ZidAndFieldDefSelector zidAndFieldDefSelector = new ZidAndFieldDefSelector(args.getFieldDefinition(), false, args.getShowConcise());
		int matchFlag = currentPair.getAsInt(currentPair.head(),ColName.MATCH_FLAG_COL);
		String preMsg = String.format("\n\tThe record pairs belonging to the input cluster id %s are:", cluster_id);
		String matchType = LabelMatchType.get(matchFlag).msg;
		String postMsg = String.format("\tThe above pair is labeled as %s\n", matchType);
//		int selectedOption = displayRecordsAndGetUserInput(getDSUtil().select(currentPair, displayCols), preMsg, postMsg);
		int selectedOption = displayRecordsAndGetUserInput(currentPair.select(zidAndFieldDefSelector.getCols()), preMsg, postMsg);
		getTrainingDataModel().updateLabellerStat(selectedOption, INCREMENT);
		getTrainingDataModel().updateLabellerStat(matchFlag, -1*INCREMENT);
		getLabelDataViewHelper().printMarkedRecordsStat(
				getTrainingDataModel().getPositivePairsCount(),
				getTrainingDataModel().getNegativePairsCount(),
				getTrainingDataModel().getNotSurePairsCount(),
				getTrainingDataModel().getTotalCount()
				);
		return selectedOption;
	}
	



	protected Pipe<D,R,C> getOutputPipe() {
		Pipe<D,R,C> p = getModelHelper().getTrainingDataMarkedPipe(args);
		p = setSaveModeOnPipe(p);
		return p;
	}

	protected abstract Pipe setSaveModeOnPipe(Pipe<D,R,C> p);
}