package zingg.common.core.executor;

import java.util.List;
import java.util.Scanner;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import zingg.common.client.ITrainingHelper;
import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;
import zingg.common.client.ZinggOptions;
import zingg.common.client.util.ColName;

public abstract class Labeller<S,D,R,C,T> extends ZinggBase<S,D,R,C,T> {

	private static final long serialVersionUID = 1L;
	protected static String name = "zingg.Labeller";
	public static final Log LOG = LogFactory.getLog(Labeller.class);
	protected ITrainingHelper<S, D, R, C> trainingHelper;

	public Labeller() {
		setZinggOptions(ZinggOptions.LABEL);
		setTrainingHelper(new TrainingHelper<S,D,R,C>());
	}

	public void execute() throws ZinggClientException {
		try {
			LOG.info("Reading inputs for labelling phase ...");
			getTrainingHelper().setMarkedRecordsStat(getMarkedRecords());
			ZFrame<D,R,C>  unmarkedRecords = getUnmarkedRecords();
			ZFrame<D,R,C>  updatedLabelledRecords = processRecordsCli(unmarkedRecords);
			if (updatedLabelledRecords != null) {
				getTrainingHelper().writeLabelledOutput(updatedLabelledRecords,args);
			}
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
				getTrainingHelper().setMarkedRecordsStat(markedRecords);
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
			getTrainingHelper().printMarkedRecordsStat();

			lines = lines.cache();
			List<C> displayCols = getTrainingHelper().getDisplayColumns(lines, args);
			//have to introduce as snowframe can not handle row.getAs with column
			//name and row and lines are out of order for the code to work properly
			//snow getAsString expects row to have same struc as dataframe which is 
			//not happening
			ZFrame<D,R,C> clusterIdZFrame = getTrainingHelper().getClusterIdsFrame(lines);
			List<R>  clusterIDs = getTrainingHelper().getClusterIds(clusterIdZFrame);
			try {
				double score;
				double prediction;
				ZFrame<D,R,C>  updatedRecords = null;
				int selected_option = -1;
				String msg1, msg2;
				int totalPairs = clusterIDs.size();

				for (int index = 0; index < totalPairs; index++) {
					ZFrame<D,R,C>  currentPair = getTrainingHelper().getCurrentPair(lines, index, clusterIDs, clusterIdZFrame);

					score = getTrainingHelper().getScore(currentPair);
					prediction = getTrainingHelper().getPrediction(currentPair);

					msg1 = getTrainingHelper().getMsg1(index, totalPairs);
					msg2 = getTrainingHelper().getMsg2(prediction, score);
					//String msgHeader = msg1 + msg2;

					selected_option = displayRecordsAndGetUserInput(getDSUtil().select(currentPair, displayCols), msg1, msg2);
					getTrainingHelper().updateLabellerStat(selected_option, 1);
					getTrainingHelper().printMarkedRecordsStat();
					if (selected_option == 9) {
						LOG.info("User has quit in the middle. Updating the records.");
						break;
					}
					updatedRecords = getTrainingHelper().updateRecords(selected_option, currentPair, updatedRecords);
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
		getTrainingHelper().displayRecords(records, preMessage, postMessage);
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
    public ITrainingHelper<S, D, R, C> getTrainingHelper() throws UnsupportedOperationException {
		if (((TrainingHelper<S, D, R, C>) trainingHelper).getDSUtil()==null) {
			((TrainingHelper<S, D, R, C>) trainingHelper).setDSUtil(getDSUtil());
		}
		if (((TrainingHelper<S, D, R, C>) trainingHelper).getPipeUtil()==null) {
			((TrainingHelper<S,D,R,C>)trainingHelper).setPipeUtil(getPipeUtil());
		}
    	return trainingHelper;
    }

	public void setTrainingHelper(ITrainingHelper<S, D, R, C> trainingHelper) {
		this.trainingHelper = trainingHelper;
	}    
    
    

}


