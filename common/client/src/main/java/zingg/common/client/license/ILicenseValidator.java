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

package zingg.common.client.license;

import java.util.Properties;

public interface ILicenseValidator {

	public boolean validate();

	public Properties getLicenseProps();

	public void setLicenseProps(Properties licenseProps);

	public String getKey();

	public void setKey(String key);

	public String getValToCheck();

	public void setValToCheck(String valToCheck);

	public String getName();

	public void setName(String name);
	
}
