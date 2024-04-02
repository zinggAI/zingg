package zingg.common.core.executor;

import java.util.List;
import java.util.Scanner;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import zingg.common.client.ILabelDataViewHelper;
import zingg.common.client.ITrainingDataModel;
import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;
import zingg.common.client.ZinggOptions;

public abstract class Labeller<S,D,R,C,T> extends ZinggBase<S,D,R,C,T> {

	public static final Integer QUIT_LABELING = 9;
	public static final Integer INCREMENT = 1;
	private static final long serialVersionUID = 1L;
	protected static String name = "zingg.common.core.executor.Labeller";
	public static final Log LOG = LogFactory.getLog(Labeller.class);
	private volatile ITrainingDataModel<S, D, R, C> trainingDataModel;
	private volatile ILabelDataViewHelper<S, D, R, C> labelDataViewHelper;
	
	protected Labeller() {
		this.trainingDataModel = getTrainingDataModel();
		this.labelDataViewHelper = getLabelDataViewHelper();
		setZinggOptions(ZinggOptions.LABEL);
	}

	public void execute() throws ZinggClientException {
		try {
			LOG.info("Reading inputs for labelling phase ...");
			ZFrame<D, R, C> markedRecords =getMarkedRecords();
			if(markedRecords != null) {
				trainingDataModel.setMarkedRecordsStat(markedRecords);
			}
			ZFrame<D,R,C>  unmarkedRecords = super.getUnmarkedRecords();
			ZFrame<D,R,C>  updatedLabelledRecords = processRecordsCli(unmarkedRecords);
			trainingDataModel.writeLabelledOutput(updatedLabelledRecords,args);
			LOG.info("Finished labelling phase");
		} catch (Exception e) {
			LOG.error("An error has occurred during labelling phase: " + e.getMessage(), e);
			throw new ZinggClientException(e.getMessage());
		}
	}

	public ZFrame<D,R,C> processRecordsCli(ZFrame<D,R,C>  lines) throws ZinggClientException {
		LOG.info("Processing Records for CLI Labelling");

		// Check if there are no unmarked records; if so, log a message and return null
		if (lines == null || lines.count() == 0) {
			LOG.info("It seems there are no unmarked records at this moment. Please run findTrainingData job to build some pairs to be labelled and then run this labeler.");
			return null;
		}
		labelDataViewHelper.printMarkedRecordsStat(
				trainingDataModel.getPositivePairsCount(),
				trainingDataModel.getNegativePairsCount(),
				trainingDataModel.getNotSurePairsCount(),
				trainingDataModel.getTotalCount()
				);

		lines = lines.cache();
		List<C> displayCols = labelDataViewHelper.getDisplayColumns(lines, args);
		//have to introduce as snowframe can not handle row.getAs with column
		//name and row and lines are out of order for the code to work properly
		//snow getAsString expects row to have same struc as dataframe which is
		//not happening
		ZFrame<D,R,C> clusterIdZFrame = labelDataViewHelper.getClusterIdsFrame(lines);
		List<R>  clusterIDs = labelDataViewHelper.getClusterIds(clusterIdZFrame);

		try {
			return processPairs(clusterIDs, lines, displayCols, clusterIdZFrame);
		} catch (Exception e) {
			LOG.error("Labelling error has occured: " + e.getMessage(), e);
			throw new ZinggClientException("An error has occured while Labelling.", e);
		}

	}

	private ZFrame<D, R, C> processPairs(List<R> clusterIDs, ZFrame<D, R, C> lines, List<C> displayCols, ZFrame<D, R, C> clusterIdZFrame) {
		try {
			double score;
			double prediction;
			ZFrame<D, R, C> updatedRecords = null;
			int selectedOption = -1;
			String msg1;
			String msg2;
			int totalPairs = clusterIDs.size();

			for (int index = 0; index < totalPairs; index++) {
				ZFrame<D, R, C> currentPair = labelDataViewHelper.getCurrentPair(lines, index, clusterIDs, clusterIdZFrame);

				score = labelDataViewHelper.getScore(currentPair);
				prediction = labelDataViewHelper.getPrediction(currentPair);

				msg1 = labelDataViewHelper.getMsg1(index, totalPairs);
				msg2 = labelDataViewHelper.getMsg2(prediction, score);

				selectedOption = displayRecordsAndGetUserInput(getDSUtil().select(currentPair, displayCols), msg1, msg2);
				updateLabellerStat(selectedOption);
				labelDataViewHelper.printMarkedRecordsStat(
						trainingDataModel.getPositivePairsCount(),
						trainingDataModel.getNegativePairsCount(),
						trainingDataModel.getNotSurePairsCount(),
						trainingDataModel.getTotalCount()
				);

				if (selectedOption == QUIT_LABELING) {
					LOG.info("User has quit in the middle. Updating the records.");
					break;
				}
				updatedRecords = trainingDataModel.updateRecords(selectedOption, currentPair, updatedRecords);
			}
			LOG.info("Processing finished.");
			return updatedRecords;
		} catch (Exception e) {
			LOG.error("Labelling error has occurred in processPairs method: " + e.getMessage(), e);
			throw e;
		}
	}

	
	protected int displayRecordsAndGetUserInput(ZFrame<D,R,C> records, String preMessage, String postMessage) {
		labelDataViewHelper.displayRecords(records, preMessage, postMessage);
		return readCliInput();
	}

	private void updateLabellerStat(int selectedOption) {
		if (selectedOption != QUIT_LABELING) {
			trainingDataModel.updateLabellerStat(selectedOption, INCREMENT);
		}
	}

	protected int readCliInput() {
		try (Scanner scanner = new Scanner(System.in)) {
			while(true) {
				System.out.print("Enter your choice (0, 1, 2, or 9): ");
				String input = scanner.nextLine();
				try {
					int selection = Integer.parseInt(input);
					if(!isValidSelection(selection)) {
						System.out.println("Invalid selection. Please enter one of the allowed options!");
						continue;
					}
					return selection;

				} catch (NumberFormatException e) {
					System.out.println("Invalid input. Please enter a number.");
				}
			}
		}
	}

	private boolean isValidSelection(int selection) {
		return selection == 0 || selection == 1 || selection == 2 || selection == 9;
	}
	
	@Override
	public ITrainingDataModel<S, D, R, C> getTrainingDataModel() {	
		if (trainingDataModel == null) {
			synchronized (this) {
				if (trainingDataModel==null) {
					this.trainingDataModel = new TrainingDataModel<>(getContext(), getZinggOptions(), getClientOptions());
				}
			}
		}
		return trainingDataModel;
    }

	@Override
	public ILabelDataViewHelper<S, D, R, C> getLabelDataViewHelper() {
		if(labelDataViewHelper==null) {
			synchronized (this) {
				if (labelDataViewHelper == null) {
					labelDataViewHelper = new LabelDataViewHelper<>(getContext(), getZinggOptions(), getClientOptions());
				}
			}
		}
    	return labelDataViewHelper;
    }

}




