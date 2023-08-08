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
package zingg.common.client;

import java.util.List;

public interface ILabelDataViewHelper<S, D, R, C> {

	ZFrame<D, R, C> getClusterIdsFrame(ZFrame<D, R, C> lines);

	List<R> getClusterIds(ZFrame<D, R, C> lines);

	List<C> getDisplayColumns(ZFrame<D, R, C> lines, Arguments args);

	ZFrame<D, R, C> getCurrentPair(ZFrame<D, R, C> lines, int index, List<R> clusterIds, ZFrame<D, R, C> clusterLines);

	double getScore(ZFrame<D, R, C> currentPair);

	double getPrediction(ZFrame<D, R, C> currentPair);

	String getMsg1(int index, int totalPairs);

	String getMsg2(double prediction, double score);

	void displayRecords(ZFrame<D, R, C> records, String preMessage, String postMessage);

	void printMarkedRecordsStat(long positivePairsCount, long negativePairsCount, long notSurePairsCount,
			long totalCount);

}