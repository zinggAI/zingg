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

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import zingg.common.client.Arguments;
import zingg.common.client.Client;
import zingg.common.client.ClientOptions;

public class TestSparkClient {

	@Test
	public void testSetColumnOptionThroughBuildAndSetArguments() {
		Arguments arguments = new Arguments();
		String[] args = {ClientOptions.CONF, "configFile", ClientOptions.PHASE, "train", ClientOptions.COLUMN, "columnName", ClientOptions.SHOW_CONCISE, "true", ClientOptions.LICENSE, "licenseFile"};
		ClientOptions options = new ClientOptions(args);
		Client client = new SparkClient();
		client.buildAndSetArguments(arguments, options);

		assertEquals("columnName", client.getArguments().getColumn());
	}

	
}
