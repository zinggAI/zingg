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

package zingg.spark.client;
import org.apache.spark.sql.SparkSession;

import zingg.common.client.ZSession;
import zingg.common.client.license.IZinggLicense;

public class ZSparkSession implements ZSession<SparkSession> {
	
	private SparkSession session;
	
	private IZinggLicense license;

	public ZSparkSession(SparkSession session, IZinggLicense license) {
		super();
		this.session = session;
		this.license = license;
	}	
	
	@Override
	public SparkSession getSession() {
		return session;
	}

	@Override
	public void setSession(SparkSession session) {
		this.session = session;
	}

	@Override
	public IZinggLicense getLicense() {
		return license;
	}

	@Override
	public void setLicense(IZinggLicense license) {
		this.license = license;
	}

}
