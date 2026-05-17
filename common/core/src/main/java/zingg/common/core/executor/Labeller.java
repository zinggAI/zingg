package zingg.common.core.executor;

import java.util.List;
import java.util.Scanner;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import zingg.common.client.ILabelDataViewHelper;
import zingg.common.client.ITrainingDataModel;
import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;
import zingg.common.client.cols.ZidAndFieldDefSelector;
import zingg.common.client.options.ZinggOptions;
import zingg.common.client.util.ColName;
import zingg.common.client.util.DFObjectUtil;
import zingg.common.core.preprocess.IPreprocessors;
import zingg.common.core.util.LabellerUtil;

public abstract class Labeller<S,D,R,C,T> extends ZinggBase<S,D,R,C,T> implements IPreprocessors<S,D,R,C,T> {

	public enum LabelAction {
		MATCH(1),
		NO_MATCH(0),
		NOT_SURE(2),
		QUIT(9);

		private final int code;

		LabelAction(int code) {
			this.code = code;
		}
    	public int getCode() {
        	return code;
    	}
	}
	private static final int STAT_INCREMENT = 1; 
	private static final long serialVersionUID = 1L;
	// protected static final String NAME = "zingg.common.core.executor.Labeller";
	private static final String LEFT_ANTI_JOIN = "left_anti";
	public static final Log LOG = LogFactory.getLog(Labeller.class);
	private final Scanner scanner = new Scanner(System.in);
	private final ITrainingDataModel<S, D, R, C> trainingDataModel;
	private final ILabelDataViewHelper<S, D, R, C> labelDataViewHelper;
	public Labeller(ITrainingDataModel<S,D,R,C> trainingDataModel,
    ILabelDataViewHelper<S,D,R,C> labelDataViewHelper) {
		this.trainingDataModel = trainingDataModel;
		this.labelDataViewHelper = labelDataViewHelper;
	}

	public void execute() throws ZinggClientException {
		setZinggOption(ZinggOptions.LABEL);

		try {
			LabellerUtil<D, R, C> labellerUtil = new LabellerUtil<>();
			LOG.info("Reading inputs for labelling phase ...");
			if(getMarkedRecords() == null) {
				LOG.info("No marked records found. Initializing the marked records stat.");
				return;
			}
			getTrainingDataModel().setMarkedRecordsStat(getMarkedRecords());
			ZFrame<D,R,C>  unmarkedRecords = getUnmarkedRecords();
			ZFrame<D, R, C> preprocessedUnmarkedRecords = preprocess(unmarkedRecords);
			ZFrame<D,R,C>  updatedLabelledRecords = processRecordsCli(preprocessedUnmarkedRecords);
			//only post processing if there are labelled records
			if(updatedLabelledRecords != null){
				ZFrame<D, R, C> postProcessedLabelledRecords = labellerUtil.postProcessLabel(updatedLabelledRecords, unmarkedRecords);
				getTrainingDataModel().writeLabelledOutput(postProcessedLabelledRecords,args);
			}
			LOG.info("Finished labelling phase");
		}catch(ZinggClientException e) {
			LOG.error("Error while labelling records", e);
			throw e;

		}catch (RuntimeException e) {
			LOG.error("Unexpected error has occurred while labelling records", e);
			throw new ZinggClientException("Unexpected error occurred while labelling records", e);
		}finally {
			scanner.close();
		}


	}


	public ZFrame<D,R,C> getUnmarkedRecords() {
		ZFrame<D,R,C> unmarkedRecords = null;
		ZFrame<D,R,C> markedRecords = null;
		try {
			unmarkedRecords = getPipeUtil().read(false, false, getModelHelper().getTrainingDataUnmarkedPipe(args));
			try {
				markedRecords = getPipeUtil().read(false, false, getModelHelper().getTrainingDataMarkedPipe(args));
			}catch (ZinggClientException zce) {
					LOG.warn("No record has been marked yet", zce);
			}			
			if (markedRecords != null ) {
				unmarkedRecords = unmarkedRecords.join(markedRecords,ColName.CLUSTER_COLUMN, false,LEFT_ANTI_JOIN);
				getTrainingDataModel().setMarkedRecordsStat(markedRecords);
			} 
		} catch (ZinggClientException e) {
			// No marked records available, continuing with unmarked records only
			LOG.error("Error while reading unmarked records for labelling", e);
		}
		return unmarkedRecords;
	}


	public ZFrame<D,R,C> processRecordsCli(ZFrame<D,R,C>  unmarkedRecords) throws ZinggClientException {
		LOG.info("Processing Records for CLI Labelling");
		if (unmarkedRecords != null && !unmarkedRecords.isEmpty()) {
			printStatistics();

		unmarkedRecords = unmarkedRecords.cache();
		ZidAndFieldDefSelector zidAndFieldDefSelector = new ZidAndFieldDefSelector(args.getFieldDefinition(), false, args.getShowConcise());
		//have to introduce as snowframe can not handle row.getAs with column
		//name and row and lines are out of order for the code to work properly
		//snow getAsString expects row to have same struc as dataframe which is 
		//not happening
		ZFrame<D,R,C> clusterIdZFrame = getLabelDataViewHelper().getClusterIdsFrame(unmarkedRecords);
		List<R>  clusterIDs = getLabelDataViewHelper().getClusterIds(clusterIdZFrame);
		Objects.requireNonNull(clusterIDs, "Cluster IDs cannot be null");
			try {
				double score;
				double prediction;
				ZFrame<D,R,C>  updatedRecords = null;
				int selectedOption = -1;
				String progressMessage, predictionMessage;
				int totalPairs = clusterIDs.size();

				for (int index = 0; index < totalPairs; index++) {
					ZFrame<D,R,C>  currentPair = getLabelDataViewHelper().getCurrentPair(unmarkedRecords, index, clusterIDs, clusterIdZFrame);
					Objects.requireNonNull(currentPair, "Current pair to label cannot be null");

					score = getLabelDataViewHelper().getScore(currentPair);
					prediction = getLabelDataViewHelper().getPrediction(currentPair);

					progressMessage = getLabelDataViewHelper().getMsg1(index, totalPairs);
					predictionMessage = getLabelDataViewHelper().getMsg2(prediction, score);
					selectedOption = displayRecordsAndGetUserInput(currentPair.select(zidAndFieldDefSelector.getCols()), progressMessage, predictionMessage);
					getTrainingDataModel().updateLabellerStat(selectedOption, STAT_INCREMENT);
					printStatistics();
					if (selectedOption == LabelAction.QUIT.getCode()) {
						LOG.info("User has quit in the middle. Updating the records.");
						break;
					}
					updatedRecords = getTrainingDataModel().updateRecords(selectedOption, currentPair, updatedRecords);
				}
				LOG.info("Processing finished.");
				return updatedRecords;
			} catch (ZinggClientException e) {
				LOG.error("Error while processing records for labelling", e);
				throw e;
			}
			catch (RuntimeException e) {
				LOG.error("Unexpected error has occurred while processing records for labelling", e);
				throw new ZinggClientException("Unexpected error occurred while processing records for labelling", e);
			}
		} else {
			LOG.info("It seems there are no unmarked records at this moment. Please run findTrainingData job to build some pairs to be labelled and then run this labeler.");
			return null;
		}
	}
	
	protected int displayRecordsAndGetUserInput(ZFrame<D,R,C> records, String preMessage, String postMessage) throws ZinggClientException {
		getLabelDataViewHelper().displayRecords(records, preMessage, postMessage);
		int selection = readCliInput();
		return selection;
	}
	private boolean isValidOption(String input){
		try {
			int code = Integer.parseInt(input);
			return java.util.Arrays.stream(LabelAction.values())
					.anyMatch(action -> action.getCode() == code);
		} catch (NumberFormatException e) {
			return false;
		}
	}
	private int readCliInput ()throws ZinggClientException{

		while (true) {
			if(!scanner.hasNext()) {
				throw new ZinggClientException("No input received from user");
			}
			String userInput = scanner.next().trim();
			if (isValidOption(userInput)) {
				return Integer.parseInt(userInput);
			}
			System.out.println("Invalid input. Allowed values: 0, 1, 2, 9");
		}
	}

	@Override
	public ITrainingDataModel<S, D, R, C> getTrainingDataModel() {	
		return trainingDataModel;
    }
	public ILabelDataViewHelper<S, D, R, C> getLabelDataViewHelper() {
    	return labelDataViewHelper;
    }
	private void printStatistics() {
		getLabelDataViewHelper().printMarkedRecordsStat(
        getTrainingDataModel().getPositivePairsCount(),
        getTrainingDataModel().getNegativePairsCount(),
        getTrainingDataModel().getNotSurePairsCount(),
        getTrainingDataModel().getTotalCount());
	}

}
