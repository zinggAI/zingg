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
import zingg.common.client.util.DFObjectUtil;
import zingg.common.core.preprocess.IPreprocessors;
import zingg.common.core.util.LabellerUtil;

public abstract class Labeller<S, D, R, C, T> extends ZinggBase<S, D, R, C, T> implements IPreprocessors<S, D, R, C, T> {

	public static final Integer QUIT_LABELING = 9;
	public static final Integer INCREMENT = 1;
	private static final long serialVersionUID = 1L;
	protected static String name = "zingg.common.core.executor.Labeller";
	public static final Log LOG = LogFactory.getLog(Labeller.class);
	protected ITrainingDataModel<S, D, R, C> trainingDataModel;
	protected ILabelDataViewHelper<S, D, R, C> labelDataViewHelper;

	private transient Scanner inputScanner; // It doesn't implement Serializable hence putting transient
	private static final int MAX_INVALID_ATTEMPTS = 50;

	public Labeller() {
		setZinggOption(ZinggOptions.LABEL);
	}

	public void execute() throws ZinggClientException {
		try {
			LOG.info("Reading inputs for labelling phase ...");
			ZFrame<D, R, C> markedRecords = getMarkedRecords();
			ZFrame<D, R, C> unmarkedRecords = getUnmarkedRecords(); // Still not correct, internally it's still calling MarkedRecords
			if (markedRecords != null) {
				getTrainingDataModel().setMarkedRecordsStat(markedRecords);
			}
			ZFrame<D, R, C> preprocessedUnmarkedRecords = preprocess(unmarkedRecords);
			ZFrame<D, R, C> updatedLabelledRecords = processRecordsCli(preprocessedUnmarkedRecords);

			if (updatedLabelledRecords != null) {
				ZFrame<D, R, C> postProcessedLabelledRecords = new LabellerUtil<D, R, C>().postProcessLabel(updatedLabelledRecords, unmarkedRecords);
				getTrainingDataModel().writeLabelledOutput(postProcessedLabelledRecords, args);
			}
			LOG.info("Finished labelling phase");
		} catch (Exception e) {
			throw new ZinggClientException("Error in labelling phase ", e);
		}
	}

	public ZFrame<D, R, C> processRecordsCli(ZFrame<D, R, C> lines) throws ZinggClientException {
		LOG.info("Processing Records for CLI Labelling");
		if (lines == null || lines.count() == 0) {
			LOG.info("It seems there are no unmarked records at this moment. Please run findTrainingData job to build some pairs to be labelled and then run this labeler.");
			return null;
		}
		printCurrentStats();

		ZFrame<D, R, C> cachedLines = lines.cache();
		ZidAndFieldDefSelector zidAndFieldDefSelector = new ZidAndFieldDefSelector(args.getFieldDefinition(), false, args.getShowConcise());
		ZFrame<D, R, C> clusterIdZFrame = getLabelDataViewHelper().getClusterIdsFrame(cachedLines);
		List<R> clusterIDs = getLabelDataViewHelper().getClusterIds(clusterIdZFrame);

		try {
			ZFrame<D, R, C> updatedRecords = null;
			int totalPairs = clusterIDs.size();

			for (int index = 0; index < totalPairs; index++) {
				ZFrame<D, R, C> currentPair = getLabelDataViewHelper().getCurrentPair(cachedLines, index, clusterIDs, clusterIdZFrame);
				validatePairRowCount(currentPair, clusterIDs.get(index));
				int selectedOption = labelSinglePair(currentPair, index, clusterIDs.size(), zidAndFieldDefSelector);
				if (selectedOption == QUIT_LABELING) {
					LOG.info("User has quit in the middle. Updating the records.");
					break;
				}
				getTrainingDataModel().updateLabellerStat(selectedOption, INCREMENT);
				updatedRecords = getTrainingDataModel().updateRecords(selectedOption, currentPair, updatedRecords);
				printCurrentStats();
			}
			LOG.warn("Processing finished.");
			return updatedRecords;
		} catch (Exception e) {
			LOG.warn("Labelling error has occurred " + e.getMessage());
			throw new ZinggClientException("An error has occurred while Labelling.", e);
		}
	}

	private int labelSinglePair(ZFrame<D, R, C> currentPair, int index, int totalPairs, ZidAndFieldDefSelector selector) throws ZinggClientException {

		double score = getLabelDataViewHelper().getScore(currentPair);
		double prediction = getLabelDataViewHelper().getPrediction(currentPair);

		String msg1 = getLabelDataViewHelper().getMsg1(index, totalPairs);
		String msg2 = getLabelDataViewHelper().getMsg2(prediction, score);

		return displayRecordsAndGetUserInput(currentPair.select(selector.getCols()), msg1, msg2);
	}

	private void validatePairRowCount(ZFrame<D, R, C> currentPair, R clusterId) {
		if (currentPair == null) {
			LOG.warn("Current pair is null for clusterId: " + clusterId);
			return;
		}
		long rowCount = currentPair.count();
		if (rowCount != 2) {
			LOG.warn("Expected 2 records for clusterId " + clusterId + ", found " + rowCount);
		}
	}

	protected int displayRecordsAndGetUserInput(ZFrame<D, R, C> records, String preMessage, String postMessage) throws ZinggClientException {
		getLabelDataViewHelper().displayRecords(records, preMessage, postMessage);
		return readCliInput();
	}

	// Can be used at other places as well
	protected void printCurrentStats() {
		getLabelDataViewHelper().printMarkedRecordsStat(
				getTrainingDataModel().getPositivePairsCount(),
				getTrainingDataModel().getNegativePairsCount(),
				getTrainingDataModel().getNotSurePairsCount(),
				getTrainingDataModel().getTotalCount()
		);
	}

	protected Scanner getInputScanner() {
		if (inputScanner == null) {
			inputScanner = new Scanner(System.in);
		}
		return inputScanner;
	}

	int readCliInput() throws ZinggClientException {
		Scanner sc = getInputScanner();
		int invalidAttempts = 0;

		while (sc.hasNext()) {
			if (sc.hasNext("[0129]")) {
				return Integer.parseInt(sc.next());
			}
			sc.next();
			invalidAttempts++;
			if (invalidAttempts >= MAX_INVALID_ATTEMPTS) {
				throw new ZinggClientException("Exceeded maximum invalid CLI input attempts (" + MAX_INVALID_ATTEMPTS + ")."); // Or return QUIT_LABELING
			}
			System.out.println("Nope, please enter one of the allowed options!");
		}

		throw new ZinggClientException("Input stream closed before a valid labelling option was provided."); // Or return QUIT_LABELING
	}

	@Override
	public ITrainingDataModel<S, D, R, C> getTrainingDataModel() {
		if (trainingDataModel == null) {
			this.trainingDataModel = new TrainingDataModel<S, D, R, C, T>(getContext(), getClientOptions());
		}
		return trainingDataModel;
	}

	public ILabelDataViewHelper<S, D, R, C> getLabelDataViewHelper() {
		if (labelDataViewHelper == null) {
			labelDataViewHelper = new LabelDataViewHelper<S, D, R, C, T>(getContext(), getClientOptions());
			labelDataViewHelper.initVerticalDisplayUtility(getDfObjectUtil());
		}
		return labelDataViewHelper;
	}

	protected abstract DFObjectUtil<S, D, R, C> getDfObjectUtil();
}
