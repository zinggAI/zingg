package zingg.common.core.executor;

import java.util.List;
import java.util.Scanner;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import zingg.common.client.ILabelDataViewHelper;
import zingg.common.client.ITrainingDataModel;
import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;
import zingg.common.client.cols.ZidAndFieldDefSelector;
import zingg.common.client.options.ZinggOptions;
import zingg.common.client.util.ColName;
import zingg.common.client.util.ColValues;
import zingg.common.client.util.DFObjectUtil;
import zingg.common.core.preprocess.IPreprocessors;
import zingg.common.core.util.LabellerUtil;

public abstract class Labeller<S,D,R,C,T> extends ZinggBase<S,D,R,C,T> implements IPreprocessors<S,D,R,C,T> {

	public static final Integer QUIT_LABELING = 9;
	public static final Integer INCREMENT = 1;
	private static final long serialVersionUID = 1L;
	protected static String name = "zingg.common.core.executor.Labeller";
	public static final Log LOG = LogFactory.getLog(Labeller.class);
	protected ITrainingDataModel<S, D, R, C> trainingDataModel;
	protected ILabelDataViewHelper<S, D, R, C> labelDataViewHelper;
	private transient Scanner cliScanner;

	protected enum LabelChoice {
		DO_NOT_MATCH(ColValues.MATCH_TYPE_NOT_A_MATCH, false),
		MATCH(ColValues.MATCH_TYPE_MATCH, false),
		NOT_SURE(ColValues.MATCH_TYPE_NOT_SURE, false),
		QUIT(QUIT_LABELING, true);

		private final int value;
		private final boolean terminal;

		LabelChoice(int value, boolean terminal) {
			this.value = value;
			this.terminal = terminal;
		}

		int value() {
			return value;
		}

		boolean isTerminal() {
			return terminal;
		}

		static LabelChoice fromInput(String input) {
			if (input == null) {
				return null;
			}
			switch (input) {
			case "0":
				return DO_NOT_MATCH;
			case "1":
				return MATCH;
			case "2":
				return NOT_SURE;
			case "9":
				return QUIT;
			default:
				return null;
			}
		}
	}
	
	public Labeller() {
		setZinggOption(ZinggOptions.LABEL);
	}

	public void execute() throws ZinggClientException {
		try {
			LabellerUtil<D, R, C> labellerUtil = new LabellerUtil<D, R, C>();
			LOG.info("Reading inputs for labelling phase ...");
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
		} catch (Exception e) {
			throw new ZinggClientException("Error in labelling phase ", e);
		}
	}

	public ZFrame<D,R,C> getUnmarkedRecords() {
		try {
			ZFrame<D,R,C> unmarkedRecords = readUnmarkedRecords();
			ZFrame<D,R,C> markedRecords = readMarkedRecordsIfAvailable();
			if (markedRecords != null ) {
				getTrainingDataModel().setMarkedRecordsStat(markedRecords);
				return removeMarkedRecords(unmarkedRecords, markedRecords);
			} 
			return unmarkedRecords;
		} catch (ZinggClientException zce) {
			LOG.warn("No unmarked record for labelling", zce);
		} catch (Exception e) {
			LOG.warn("No unmarked record for labelling", e);
		}
		return null;
	}

	protected ZFrame<D,R,C> readUnmarkedRecords() throws ZinggClientException {
		return getPipeUtil().read(false, false, getModelHelper().getTrainingDataUnmarkedPipe(args));
	}

	protected ZFrame<D,R,C> readMarkedRecordsIfAvailable() {
		try {
			return getPipeUtil().read(false, false, getModelHelper().getTrainingDataMarkedPipe(args));
		} catch (ZinggClientException zce) {
			LOG.warn("No record has been marked yet");
		} catch (Exception e) {
			LOG.warn("No record has been marked yet");
		}
		return null;
	}

	protected ZFrame<D,R,C> removeMarkedRecords(ZFrame<D,R,C> unmarkedRecords, ZFrame<D,R,C> markedRecords) {
		return unmarkedRecords.join(markedRecords, ColName.CLUSTER_COLUMN, false, "left_anti");
	}

	public ZFrame<D,R,C> processRecordsCli(ZFrame<D,R,C>  lines) throws ZinggClientException {
		LOG.info("Processing Records for CLI Labelling");
		if (lines != null && lines.count() > 0) {
			printMarkedRecordsStat();

			lines = lines.cache();
			ZidAndFieldDefSelector zidAndFieldDefSelector = getZidAndFieldDefSelector();
			//have to introduce as snowframe can not handle row.getAs with column
			//name and row and lines are out of order for the code to work properly
			//snow getAsString expects row to have same struc as dataframe which is 
			//not happening
			ZFrame<D,R,C> clusterIdZFrame = getLabelDataViewHelper().getClusterIdsFrame(lines);
			List<R>  clusterIDs = getLabelDataViewHelper().getClusterIds(clusterIdZFrame);
			try {
				return labelClusterRecords(lines, clusterIDs, clusterIdZFrame, zidAndFieldDefSelector);
			} catch (Exception e) {
				LOG.warn("Labelling error has occurred " + e.getMessage());
				throw new ZinggClientException("An error has occurred while Labelling.", e);
			}
		} else {
			LOG.info("It seems there are no unmarked records at this moment. Please run findTrainingData job to build some pairs to be labelled and then run this labeler.");
			return null;
		}
	}

	protected ZidAndFieldDefSelector getZidAndFieldDefSelector() {
		return new ZidAndFieldDefSelector(args.getFieldDefinition(), false, args.getShowConcise());
	}

	protected ZFrame<D,R,C> labelClusterRecords(ZFrame<D,R,C> lines, List<R> clusterIDs, ZFrame<D,R,C> clusterIdZFrame,
			ZidAndFieldDefSelector zidAndFieldDefSelector) throws ZinggClientException {
		ZFrame<D,R,C> updatedRecords = null;
		int totalPairs = clusterIDs.size();

		for (int index = 0; index < totalPairs; index++) {
			ZFrame<D,R,C> currentPair = getLabelDataViewHelper().getCurrentPair(lines, index, clusterIDs, clusterIdZFrame);
			LabelChoice selectedOption = getUserLabelChoice(currentPair, zidAndFieldDefSelector, index, totalPairs);
			if (selectedOption.isTerminal()) {
				LOG.info("User has quit in the middle. Updating the records.");
				break;
			}
			recordLabelChoice(selectedOption);
			updatedRecords = getTrainingDataModel().updateRecords(selectedOption.value(), currentPair, updatedRecords);
		}
		LOG.info("Processing finished.");
		return updatedRecords;
	}

	protected LabelChoice getUserLabelChoice(ZFrame<D,R,C> currentPair,
			ZidAndFieldDefSelector zidAndFieldDefSelector, int index, int totalPairs) throws ZinggClientException {
		double score = getLabelDataViewHelper().getScore(currentPair);
		double prediction = getLabelDataViewHelper().getPrediction(currentPair);
		String msg1 = getLabelDataViewHelper().getMsg1(index, totalPairs);
		String msg2 = getLabelDataViewHelper().getMsg2(prediction, score);

		return displayRecordsAndGetUserChoice(currentPair.select(zidAndFieldDefSelector.getCols()), msg1, msg2);
	}

	protected void recordLabelChoice(LabelChoice selectedOption) {
		getTrainingDataModel().updateLabellerStat(selectedOption.value(), INCREMENT);
		printMarkedRecordsStat();
	}

	protected void printMarkedRecordsStat() {
		getLabelDataViewHelper().printMarkedRecordsStat(
				getTrainingDataModel().getPositivePairsCount(),
				getTrainingDataModel().getNegativePairsCount(),
				getTrainingDataModel().getNotSurePairsCount(),
				getTrainingDataModel().getTotalCount()
				);
	}

	
	protected int displayRecordsAndGetUserInput(ZFrame<D,R,C> records, String preMessage, String postMessage) throws ZinggClientException {
		return displayRecordsAndGetUserChoice(records, preMessage, postMessage).value();
	}

	protected LabelChoice displayRecordsAndGetUserChoice(ZFrame<D,R,C> records, String preMessage, String postMessage) throws ZinggClientException {
		getLabelDataViewHelper().displayRecords(records, preMessage, postMessage);
		return readCliChoice();
	}


	int readCliInput() {
		return readCliChoice().value();
	}

	LabelChoice readCliChoice() {
		Scanner sc = getCliScanner();
		LabelChoice selection = null;
		while (selection == null) {
			String word = sc.next();
			selection = LabelChoice.fromInput(word);
			if (selection == null) {
				System.out.println("Nope, please enter one of the allowed options!");
			}
		}
		return selection;
	}

	protected Scanner getCliScanner() {
		if (cliScanner == null) {
			cliScanner = new Scanner(System.in);
		}
		return cliScanner;
	}

	@Override
	public ITrainingDataModel<S, D, R, C> getTrainingDataModel() {	
		if (trainingDataModel==null) {
			this.trainingDataModel = new TrainingDataModel<S, D, R, C, T>(getContext(), getClientOptions());
		}
		return trainingDataModel;
    }

	public void setTrainingDataModel(ITrainingDataModel<S, D, R, C> trainingDataModel) {
		this.trainingDataModel = trainingDataModel;
	}

	
	public ILabelDataViewHelper<S, D, R, C> getLabelDataViewHelper() {
		if(labelDataViewHelper==null) {
			labelDataViewHelper = new LabelDataViewHelper<S,D,R,C,T>(getContext(), getClientOptions());
			labelDataViewHelper.initVerticalDisplayUtility(getDfObjectUtil());
		}
    	return labelDataViewHelper;
    }

	public void setLabelDataViewHelper(ILabelDataViewHelper<S, D, R, C> labelDataViewHelper) {
		this.labelDataViewHelper = labelDataViewHelper;
	}

	protected abstract DFObjectUtil<S, D, R, C> getDfObjectUtil();
}
