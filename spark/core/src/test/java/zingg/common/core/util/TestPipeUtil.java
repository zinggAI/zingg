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

package zingg.common.core.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.sql.SaveMode;
import org.junit.jupiter.api.Test;

import zingg.common.client.Arguments;
import zingg.common.client.pipe.FilePipe;
import zingg.common.client.pipe.Pipe;
import zingg.spark.core.executor.ZinggSparkTester;

public class TestPipeUtil extends ZinggSparkTester{
	public static final Log LOG = LogFactory.getLog(TestPipeUtil.class);

	@Test
	public void testStopWordsPipe() {
		Arguments args = new Arguments();
		String fileName = args.getStopWordsDir() + "file";
		Pipe p = zsCTX.getPipeUtil().getStopWordsPipe(args, fileName);

		assertEquals(Pipe.FORMAT_CSV, p.getFormat(), "Format is not CSV");
		assertEquals("true", p.get(FilePipe.HEADER).toLowerCase(), "Property 'header' is set to 'false'");
		assertEquals(SaveMode.Overwrite.toString(), p.getMode(), "SaveMode is not 'Overwrite'");
		assertEquals(fileName, p.get(FilePipe.LOCATION), "Absolute location of file differs");
	}
}
