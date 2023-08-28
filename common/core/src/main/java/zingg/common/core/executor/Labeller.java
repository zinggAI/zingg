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
import zingg.common.client.util.ColName;

public abstract class Labeller<S,D,R,C,T> extends ZinggBase<S,D,R,C,T> {

	public static final Integer QUIT_LABELING = 9;
	public static final Integer INCREMENT = 1;
	private static final long serialVersionUID = 1L;
	protected static String name = "zingg.common.core.executor.Labeller";
	public static final Log LOG = LogFactory.getLog(Labeller.class);
	protected ITrainingDataModel<S, D, R, C> trainingDataModel;
	protected ILabelDataViewHelper<S, D, R, C> labelDataViewHelper;
	
	public Labeller() {
		setZinggOptions(ZinggOptions.LABEL);
	}

	public void execute() throws ZinggClientException {
		try {
			LOG.info("Reading inputs for labelling phase ...");
			getTrainingDataModel().setMarkedRecordsStat(getMarkedRecords());
			ZFrame<D,R,C>  unmarkedRecords = getUnmarkedRecords();
			ZFrame<D,R,C>  updatedLabelledRecords = processRecordsCli(unmarkedRecords);
			getTrainingDataModel().writeLabelledOutput(updatedLabelledRecords,args);
			LOG.info("Finished labelling phase");
		} catch (Exception e) {
			e.printStackTrace();
			throw new ZinggClientException(e.getMessage());
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
			} catch (ZinggClientException zce) {
					LOG.warn("No record has been marked yet");
			}			
			if (markedRecords != null ) {
				unmarkedRecords = unmarkedRecords.join(markedRecords,ColName.CLUSTER_COLUMN, false,
						"left_anti");
				getTrainingDataModel().setMarkedRecordsStat(markedRecords);
			} 
		} catch (Exception e) {
			LOG.warn("No unmarked record for labelling");
		} catch (ZinggClientException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return unmarkedRecords;
	}

	public ZFrame<D,R,C> processRecordsCli(ZFrame<D,R,C>  lines) throws ZinggClientException {
		LOG.info("Processing Records for CLI Labelling");
		if (lines != null && lines.count() > 0) {
			getLabelDataViewHelper().printMarkedRecordsStat(
					getTrainingDataModel().getPositivePairsCount(),
					getTrainingDataModel().getNegativePairsCount(),
					getTrainingDataModel().getNotSurePairsCount(),
					getTrainingDataModel().getTotalCount()
					);

			lines = lines.cache();
			List<C> displayCols = getLabelDataViewHelper().getDisplayColumns(lines, args);
			//have to introduce as snowframe can not handle row.getAs with column
			//name and row and lines are out of order for the code to work properly
			//snow getAsString expects row to have same struc as dataframe which is 
			//not happening
			ZFrame<D,R,C> clusterIdZFrame = getLabelDataViewHelper().getClusterIdsFrame(lines);
			List<R>  clusterIDs = getLabelDataViewHelper().getClusterIds(clusterIdZFrame);
			try {
				double score;
				double prediction;
				ZFrame<D,R,C>  updatedRecords = null;
				int selectedOption = -1;
				String msg1, msg2;
				int totalPairs = clusterIDs.size();

				for (int index = 0; index < totalPairs; index++) {
					ZFrame<D,R,C>  currentPair = getLabelDataViewHelper().getCurrentPair(lines, index, clusterIDs, clusterIdZFrame);

					score = getLabelDataViewHelper().getScore(currentPair);
					prediction = getLabelDataViewHelper().getPrediction(currentPair);

					msg1 = getLabelDataViewHelper().getMsg1(index, totalPairs);
					msg2 = getLabelDataViewHelper().getMsg2(prediction, score);
					//String msgHeader = msg1 + msg2;

					selectedOption = displayRecordsAndGetUserInput(getDSUtil().select(currentPair, displayCols), msg1, msg2);
					getTrainingDataModel().updateLabellerStat(selectedOption, INCREMENT);
					getLabelDataViewHelper().printMarkedRecordsStat(
							getTrainingDataModel().getPositivePairsCount(),
							getTrainingDataModel().getNegativePairsCount(),
							getTrainingDataModel().getNotSurePairsCount(),
							getTrainingDataModel().getTotalCount()
							);
					if (selectedOption == QUIT_LABELING) {
						LOG.info("User has quit in the middle. Updating the records.");
						break;
					}
					updatedRecords = getTrainingDataModel().updateRecords(selectedOption, currentPair, updatedRecords);
				}
				LOG.warn("Processing finished.");
				return updatedRecords;
			} catch (Exception e) {
				if (LOG.isDebugEnabled()) {
					e.printStackTrace();
				}
				LOG.warn("Labelling error has occured " + e.getMessage());
				throw new ZinggClientException("An error has occured while Labelling.", e);
			}
		} else {
			LOG.info("It seems there are no unmarked records at this moment. Please run findTrainingData job to build some pairs to be labelled and then run this labeler.");
			return null;
		}
	}

	
	protected int displayRecordsAndGetUserInput(ZFrame<D,R,C> records, String preMessage, String postMessage) {
		getLabelDataViewHelper().displayRecords(records, preMessage, postMessage);
		int selection = readCliInput();
		return selection;
	}


	int readCliInput() {
		Scanner sc = new Scanner(System.in);

		while (!sc.hasNext("[0129]")) {
			sc.next();
			System.out.println("Nope, please enter one of the allowed options!");
		}
		String word = sc.next();
		int selection = Integer.parseInt(word);
		// sc.close();

		return selection;
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

	@Override
	public ILabelDataViewHelper<S, D, R, C> getLabelDataViewHelper() {
		if(labelDataViewHelper==null) {
			labelDataViewHelper = new LabelDataViewHelper<S,D,R,C,T>(getContext(), getClientOptions());
		}
    	return labelDataViewHelper;
    }

	public void setLabelDataViewHelper(ILabelDataViewHelper<S, D, R, C> labelDataViewHelper) {
		this.labelDataViewHelper = labelDataViewHelper;
	}

}


