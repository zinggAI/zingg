package zingg.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Properties;
import java.util.stream.Collectors;

import org.junit.Ignore;
import org.junit.Test;

public class TestArguments {

	private static final String KEY_HEADER = "header";
	private static final String KEY_FORMAT = "format";
	private static final String KEY_MODEL_ID = "modelId";

	@Ignore
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
	public void testSubstituteVariablesWithAllEnvVarSet() {
		try {
			FileReader reader = new FileReader(getClass().getResource("../../testConfigEnv.sh").getFile());
			Properties p = new Properties();
			p.load(reader);
			HashMap<String, String> env = p.entrySet().stream().collect(
					Collectors.toMap(
							e -> String.valueOf(e.getKey()),
							e -> String.valueOf(e.getValue()),
							(prev, next) -> next, HashMap::new));

			byte[] encoded = Files
					.readAllBytes(Paths.get(getClass().getResource("../../testConfigTemplate.json").getFile()));

			String template = new String(encoded, StandardCharsets.UTF_8);
			String json = Arguments.substituteVariables(template, env);
			Arguments args = Arguments.createArgumentsFromJSONString(json, "");
			assertNotNull(args);
		} catch (IOException | ZinggClientException e) {
			fail("Unexpected exception " + e.getMessage());
		}
	}

	private Properties createProperties() {
		Properties p = new Properties();
		p.setProperty(KEY_HEADER, "true");
		p.setProperty(KEY_FORMAT, "csv");
		p.setProperty(KEY_MODEL_ID, "400");
		return p;
	}

	@Test
	public void testSubstituteVariablesWithMissingEnvVar() {
		try {
			Properties p = createProperties();
			p.remove(KEY_FORMAT);

			HashMap<String, String> env = p.entrySet().stream().collect(
					Collectors.toMap(
							e -> String.valueOf(e.getKey()),
							e -> String.valueOf(e.getValue()),
							(prev, next) -> next, HashMap::new));

			byte[] encoded = Files
					.readAllBytes(Paths.get(getClass().getResource("../../testConfigTemplate.json").getFile()));

			String template = new String(encoded, StandardCharsets.UTF_8);
			String json = Arguments.substituteVariables(template, env);
			Arguments args = Arguments.createArgumentsFromJSONString(json, "");
			assertNotNull(args);
		} catch (IOException | ZinggClientException e) {
			System.out.println("Expected exception received Unable to parse the configuration");
		}
	}

	@Test
	public void testSubstituteVariablesWithBlankEnvVar() {
		try {

			Properties p = createProperties();
			p.remove(KEY_FORMAT);
			p.setProperty(KEY_FORMAT, "");

			HashMap<String, String> env = p.entrySet().stream().collect(
					Collectors.toMap(
							e -> String.valueOf(e.getKey()),
							e -> String.valueOf(e.getValue()),
							(prev, next) -> next, HashMap::new));

			byte[] encoded = Files
					.readAllBytes(Paths.get(getClass().getResource("../../testConfigTemplate.json").getFile()));

			String template = new String(encoded, StandardCharsets.UTF_8);
			String json = Arguments.substituteVariables(template, env);
			Arguments args = Arguments.createArgumentsFromJSONString(json, "");

			assertNull(args.getOutput()[0].getFormat());
		} catch (IOException | ZinggClientException e) {
			fail("Unexpected exception" + e.getMessage());
		}
	}

	@Test
	public void testInvalidEnvVarBooleanType() {
		try {

			Properties p = createProperties();
			p.remove(KEY_HEADER);
			p.setProperty(KEY_HEADER, "someValue");
  
			HashMap<String, String> env = p.entrySet().stream().collect(
					Collectors.toMap(
							e -> String.valueOf(e.getKey()),
							e -> String.valueOf(e.getValue()),
							(prev, next) -> next, HashMap::new));

			byte[] encoded = Files
					.readAllBytes(Paths.get(getClass().getResource("../../testConfigTemplate.json").getFile()));

			String template = new String(encoded, StandardCharsets.UTF_8);
			String json = Arguments.substituteVariables(template, env);
			Arguments.createArgumentsFromJSONString(json, "");
 
			fail("Exception was expected for invalid value for a Boolean variable");
 		} catch (IOException | ZinggClientException e) {
			System.out.println("Expected exception received due to invalid value for a Boolean variable");
 		}
	}

	@Test
	public void testBooleanType() {
		try {

			Properties p = createProperties();
			p.remove(KEY_HEADER);
			p.setProperty(KEY_HEADER, "true");

			HashMap<String, String> env = p.entrySet().stream().collect(
					Collectors.toMap(
							e -> String.valueOf(e.getKey()),
							e -> String.valueOf(e.getValue()),
							(prev, next) -> next, HashMap::new));

			byte[] encoded = Files
					.readAllBytes(Paths.get(getClass().getResource("../../testConfigTemplate.json").getFile()));

			String template = new String(encoded, StandardCharsets.UTF_8);
			String json = Arguments.substituteVariables(template, env);
			Arguments args = Arguments.createArgumentsFromJSONString(json, "");
 
			assertEquals(args.getOutput()[0].getProps().get(KEY_HEADER), p.getProperty(KEY_HEADER));
		} catch (IOException | ZinggClientException e) {
			fail("Exception was not expected for valid value for a Boolean variable within quotes");

		}
	}

	@Test
	public void testInvalidEnvVarNumericType() {
		try {

			Properties p = createProperties();
			p.remove(KEY_MODEL_ID);
			p.setProperty(KEY_MODEL_ID, "ONEHUNDRED");

			HashMap<String, String> env = p.entrySet().stream().collect(
					Collectors.toMap(
							e -> String.valueOf(e.getKey()),
							e -> String.valueOf(e.getValue()),
							(prev, next) -> next, HashMap::new));

			byte[] encoded = Files
					.readAllBytes(Paths.get(getClass().getResource("../../testConfigTemplate.json").getFile()));

			String template = new String(encoded, StandardCharsets.UTF_8);
			String json = Arguments.substituteVariables(template, env);
			Arguments.createArgumentsFromJSONString(json, "");

			fail("Exception was expected for invalid value for a Numeric variable");
		} catch (IOException | ZinggClientException e) {
			System.out.println("Expected exception received due to invalid value for a Numeric variable");
		}
	}

	@Test
	public void testNumericWithinQuotesTemplate() {
		try {
			Properties p = createProperties();

			HashMap<String, String> env = p.entrySet().stream().collect(
					Collectors.toMap(
							e -> String.valueOf(e.getKey()),
							e -> String.valueOf(e.getValue()),
							(prev, next) -> next, HashMap::new));

			byte[] encoded = Files.readAllBytes(
					Paths.get(getClass().getResource("../../testNumericWithinQuotesTemplate.json").getFile()));

			String template = new String(encoded, StandardCharsets.UTF_8);
			String json = Arguments.substituteVariables(template, env);
			Arguments args = Arguments.createArgumentsFromJSONString(json, "");

			//Numeric within quotes are allowed
			assertEquals(args.getModelId(), p.getProperty(KEY_MODEL_ID));
		} catch (IOException | ZinggClientException e) {
			fail("Unexpected exception in testMalformedTestConfigTemplate()" + e.getMessage());
		}
	}

	@Test
	public void testInvalidFilePath() {
		String filePath = "../dummyFilename";
		try {
			Arguments.createArgumentsFromJSONTemplate(filePath, "");
			fail("Exception was expected for invalid filepath or name");
		} catch (ZinggClientException e) {
			System.out.println("Expected exception received: NoSuchFileException");
		}
	}

	@Test
	@Ignore
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
	@Ignore
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
	@Ignore
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
	@Ignore
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
	@Ignore
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
	@Ignore
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
	@Ignore
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
	@Ignore
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
