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

import java.io.File;

import org.junit.jupiter.api.Test;



public class TestToCSV {

	@Test
	public void dummy() {
		//placeholder
	}
	
	/*
	
	@Test
	public void testFormulaException() throws ZinggWebException, ZinggWebServerException{
		String filePath = getClass().getResource("/formula.xlsx").getFile();
		ToCSV csvConverter = new ToCSV();
		String val = csvConverter.convertExcelToCSVUnix(filePath, "/tmp", "formula.csv", "\t");
		new File("/tmp/formula.csv").delete();
		System.out.println(val);
	}
	
	@Test
	public void testEmptyRows() throws ZinggWebException, ZinggWebServerException{
		String filePath = getClass().getResource("/treehouse.xlsx").getFile();
		ToCSV csvConverter = new ToCSV();
		String val = csvConverter.convertExcelToCSVUnix(filePath, "/tmp", "formula.csv", "\t");
		new File("/tmp/formula.csv").delete();
		System.out.println(val);
	}
	
	@Test
	public void testEmptyFirstCol() throws ZinggWebException, ZinggWebServerException{
		String filePath = getClass().getResource("/emptyFirstCol.xlsx").getFile();
		ToCSV csvConverter = new ToCSV();
		String val = csvConverter.convertExcelToCSVUnix(filePath, "/tmp", "emptyFirstCol.csv", "\t");
		//new File("/tmp/formula.csv").delete();
		System.out.println(val);
	}
	
	@Test
	public void testLargeFile() throws ZinggWebException, ZinggWebServerException{
		String filePath = getClass().getResource("/large.xlsx").getFile();
		ToCSV csvConverter = new ToCSV();
		String val = csvConverter.convertExcelToCSVUnix(filePath, "/tmp", "large.csv", "\t");
		//new File("/tmp/formula.csv").delete();
		System.out.println(val);
	}
	*/

}
