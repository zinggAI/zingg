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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import zingg.common.client.Arguments;
import zingg.common.client.ZinggClientException;
import zingg.common.client.ZinggOptions;
import zingg.common.client.license.IZinggLicense;

public abstract class FindAndLabeller<S, D, R, C, T> extends Labeller<S, D, R, C, T> {
	private static final long serialVersionUID = 1L;
	protected static String name = "zingg.FindAndLabeller";
	public static final Log LOG = LogFactory.getLog(FindAndLabeller.class);

	protected TrainingDataFinder<S, D, R, C, T> finder;

	public FindAndLabeller() {
		setZinggOptions(ZinggOptions.FIND_AND_LABEL);
	}

	@Override
	public void init(Arguments args, IZinggLicense license) throws ZinggClientException {
		finder.init(args, license);
		super.init(args, license);
	}

	@Override
	public void execute() throws ZinggClientException {
		finder.execute();
		super.execute();
	}

}
