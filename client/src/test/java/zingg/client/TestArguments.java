package zingg.client;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.junit.Test;

public class TestArguments {

	@Test
	public void testCreateArgsCorrect() {
		try {
			String testFileBase = System.getProperty("dataDir")
					+ "/multiField/";
			String json = testFileBase + "config.json";
			Arguments args = Arguments.createArgumentsFromJSON(json);
			assertNotNull(args);
		} catch (ZinggClientException e) {
			fail("Unexpected exception");
		}
	}

	@Test
	public void testCreateArgsMissingPosFile() {
		try {
			Arguments args = Arguments.createArgumentsFromJSON(getClass()
					.getResource("/missingFieldsPos.json").getFile());
			fail("Exception was expected for missing pos file");
		} catch (ZinggClientException e) {
			System.out.println("Expected exception received " + e.getMessage());
		}
	}

	@Test
	public void testCreateArgsMissingNegFile() {
		try {
			Arguments args = Arguments.createArgumentsFromJSON(getClass()
					.getResource("/missingFieldsNeg.json").getFile());
			fail("Exception was expected for missing neg file");
		} catch (ZinggClientException e) {
			System.out.println("Expected exception received " + e.getMessage());
		}
	}

	@Test
	public void testCreateArgsMissingMatchFile() {
		try {
			Arguments args = Arguments.createArgumentsFromJSON(getClass()
					.getResource("/missingFieldsMatch.json").getFile());
			fail("Exception was expected for missing match file");
		} catch (ZinggClientException e) {
			System.out.println("Expected exception received " + e.getMessage());
		}
	}

	@Test
	public void testCreateArgsMissingDelimiterFile() {
		try {
			Arguments args = Arguments.createArgumentsFromJSON(getClass()
					.getResource("/missingDel.json").getFile());
			fail("Exception was expected for missing delimiter");
		} catch (ZinggClientException e) {
			System.out.println("Expected exception received " + e.getMessage());
		}
	}

	@Test
	public void testCreateArgsMissingFieldDef() {
		try {
			Arguments args = Arguments.createArgumentsFromJSON(getClass()
					.getResource("/missingFieldDef.json").getFile());
			fail("Exception was expected for missing field definition ");
		} catch (ZinggClientException e) {
			System.out.println("Expected exception received " + e.getMessage());
		}
	}

	@Test
	public void testCreateArgsMissingZinggDir() {
		try {
			Arguments args = Arguments.createArgumentsFromJSON(getClass()
					.getResource("/missingZinggDir.json").getFile());
		} catch (Throwable e) {
			System.out.println("UNexpected exception received "
					+ e.getMessage());
			fail("Wrong exception, should have default");
		}

	}

	@Test
	public void testCreateArgsMissingOutDir(){
		try {
			Arguments args = Arguments.createArgumentsFromJSON(getClass()
					.getResource("/missingOutDir.json").getFile());
		} catch (Throwable e) {
			System.out.println("UNexpected exception received "
					+ e.getMessage());
			fail("Wrong exception, should have default");
		}
	}

	@Test
	public void testCreateArgsWrongFormatMissingComma() {
		try {
			Arguments args = Arguments.createArgumentsFromJSON(getClass()
					.getResource("/wrongFormatMissingComma.json").getFile());
			fail("Exception was expected for wrong format");
		} catch (ZinggClientException e) {
			System.out.println("Expected exception received " + e.getMessage());
		}
	}
	/*
	
	@Test(expected=ZinggClientException.class)
	public void testIsValidNull() throws ZinggClientException{
		Arguments.checkNullBlankEmpty(null, "null");
	}
	
	@Test(expected=ZinggClientException.class)
	public void testIsValidBlank() throws ZinggClientException{
		Arguments.checkNullBlankEmpty("", "blank");
	}
	
	@Test(expected=ZinggClientException.class)
	public void testIsValidTab() throws ZinggClientException{
		Arguments.checkNullBlankEmpty("\t", "tab");
	}
	
	@Test(expected=ZinggClientException.class)
	public void testIsValidSpace() throws ZinggClientException{
		Arguments.checkNullBlankEmpty(" ", "space");
	}
	*/

}
