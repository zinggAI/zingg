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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.apache.spark.sql.types.DataType;
import org.junit.jupiter.api.Test;

import zingg.common.client.util.*;



public class TestUtil {

	@Test
	public void testParseCsvSimple() {
		String csv = "a,b,c";
		List<String> values = new ArrayList<String>();
		values.add("a");
		values.add("b");
		values.add("c");

		List<String> ret = Util.parse(csv, ",");
		for (int i = 0; i < values.size(); ++i) {
			assertEquals(values.get(i), ret.get(i));
		}
	}

	@Test
	public void testParseCsvMissingFieldEnd() {
		String csv = "a,b,c,";
		List<String> values = new ArrayList<String>();
		values.add("a");
		values.add("b");
		values.add("c");
		values.add("");

		List<String> ret = Util.parse(csv, ",");
		for (int i = 0; i < values.size(); ++i) {
			assertEquals(values.get(i), ret.get(i));
		}
	}

	@Test
	public void testParseCsvMissingFieldMiddleEnd() {
		String csv = "a,b,,d,";
		List<String> values = new ArrayList<String>();
		values.add("a");
		values.add("b");
		values.add("");
		values.add("d");
		values.add("");

		List<String> ret = Util.parse(csv, ",");
		for (int i = 0; i < values.size(); ++i) {
			assertEquals(values.get(i), ret.get(i));
		}
	}

	@Test
	public void testParsePipeMissingFieldMiddleEnd() {
		String csv = "a|b||d|";
		List<String> values = new ArrayList<String>();
		values.add("a");
		values.add("b");
		values.add("");
		values.add("d");
		values.add("");

		List<String> ret = Util.parse(csv, "|");
		for (int i = 0; i < values.size(); ++i) {
			assertEquals(values.get(i), ret.get(i));
		}
	}

	@Test
	public void testParseTabMissingFieldMiddleEnd() {
		String csv = "a\tb\t\td\t";
		List<String> values = new ArrayList<String>();
		values.add("a");
		values.add("b");
		values.add("");
		values.add("d");
		values.add("");

		List<String> ret = Util.parse(csv, "\t");
		for (int i = 0; i < values.size(); ++i) {
			assertEquals(values.get(i), ret.get(i));
		}
	}

	@Test
	public void testParseQuotedTabMissingFieldMiddleEnd() {
		String csv = "a\t\"b\t\td\t";
		List<String> values = new ArrayList<String>();
		values.add("a");
		values.add("\"b");
		values.add("");
		values.add("d");
		values.add("");

		List<String> ret = Util.parse(csv, "\t");
		for (int i = 0; i < values.size(); ++i) {
			assertEquals(values.get(i), ret.get(i));
		}
	}
	
	/*

	@Test
	public void testGetFunctions() throws Exception {
		ListMap<DataType, RFunction> functions = Util
				.getFunctionList(Zingg.functionFile);
		assertNotNull(functions);
		assertTrue(functions.size() > 0);
		System.out.println(functions);
	}
	
	@Test
	public void testGetHashFunctions() throws Exception {
		ListMap<DataType, RFunction> functions = Util
				.getHashFunctionList(Zingg.functionFile);
		assertNotNull(functions);
		assertTrue(functions.size() > 0);
		System.out.println(functions);
	}
	*/

}
