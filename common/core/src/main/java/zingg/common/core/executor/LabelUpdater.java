/*
 * Zingg
 * Copyright (C) 2021-Present  Zingg Labs,inc
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package zingg.common.core.executor;

import java.util.List;
import java.util.Scanner;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;
import zingg.common.client.ZinggOptions;
import zingg.common.client.pipe.Pipe;
import zingg.common.client.util.ColName;
import zingg.common.core.util.LabelMatchType;

public abstract class LabelUpdater<S,D,R,C,T> extends Labeller<S,D,R,C,T> {
	private static final long serialVersionUID = 1L;
	protected static String name = "zingg.common.core.executor.LabelUpdater";
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
					if (cluster_id.equals(QUIT_LABELING.toString())) {
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
					getTrainingDataModel().updateLabellerStat(selectedOption, INCREMENT);
					getTrainingDataModel().updateLabellerStat(matchFlag, -1*INCREMENT);
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
					recordsToUpdate = recordsToUpdate
							.filter(recordsToUpdate.notEqual(ColName.CLUSTER_COLUMN,cluster_id));
					if (updatedRecords != null) {
						updatedRecords = updatedRecords
								.filter(updatedRecords.notEqual(ColName.CLUSTER_COLUMN,cluster_id));
					}
					updatedRecords = getTrainingDataModel().updateRecords(selectedOption, currentPair, updatedRecords);
				} while (selectedOption != QUIT_LABELING);

				if (updatedRecords != null) {
					updatedRecords = updatedRecords.union(recordsToUpdate);
				}
				getTrainingDataModel().writeLabelledOutput(updatedRecords,args,getOutputPipe());
				sc.close();
				LOG.info("Processing finished.");
				return updatedRecords;
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

	


	protected Pipe<D,R,C> getOutputPipe() {
		Pipe<D,R,C> p = getPipeUtil().getTrainingDataMarkedPipe(args);
		p = setSaveModeOnPipe(p);
		return p;
	}

	protected abstract Pipe setSaveModeOnPipe(Pipe<D,R,C> p);
}