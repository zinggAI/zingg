package zingg.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.apache.spark.sql.types.DataType;
import org.junit.Test;

import zingg.client.util.*;



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
