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

import zingg.common.client.license.IZinggLicense;

public interface IZingg<S,D,R,C> {

	public void init(Arguments args, IZinggLicense license)
			throws ZinggClientException;

	public void execute() throws ZinggClientException;

	public void cleanup() throws ZinggClientException;

	public ZinggOptions getZinggOptions();	

	public String getName();

	public void postMetrics();

	//** placing these methods for the assessModel phase */

	public ZFrame<D,R,C>  getMarkedRecords();

	public ZFrame<D,R,C>  getUnmarkedRecords();

	public Long getMarkedRecordsStat(ZFrame<D,R,C>  markedRecords, long value);

    public Long getMatchedMarkedRecordsStat(ZFrame<D,R,C>  markedRecords);

    public Long getUnmatchedMarkedRecordsStat(ZFrame<D,R,C>  markedRecords);

    public Long getUnsureMarkedRecordsStat(ZFrame<D,R,C>  markedRecords);

    //public void setSession(S session); // method name will have to be changed in Client too

	
	public void setClientOptions(ClientOptions clientOptions);

	public ClientOptions getClientOptions(); 

	public void setSession(S session);
	
	public ITrainingDataModel<S, D, R, C> getTrainingDataModel() throws UnsupportedOperationException;
	
	public ILabelDataViewHelper<S, D, R, C> getLabelDataViewHelper() throws UnsupportedOperationException;
	
}
