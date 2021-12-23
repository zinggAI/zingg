package zingg.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

public class TestArguments {

	private static final String KEY_HEADER = "header";
	private static final String KEY_FORMAT = "format";
	private static final String KEY_MODEL_ID = "modelId";

	public static final Log LOG = LogFactory.getLog(TestArguments.class);

	@Test
	public void testSubstituteVariablesWithAllEnvVarSet() {
		try {
			Map<String, String> env = new HashMap<String, String>();
			env.put(KEY_HEADER, "true");
			env.put(KEY_FORMAT, "csv");
			env.put(KEY_MODEL_ID, "400");
	
			byte[] encoded = Files
					.readAllBytes(Paths.get(getClass().getResource("../../testConfigTemplate.json.env").getFile()));
			String template = new String(encoded, StandardCharsets.UTF_8);
			String json = Arguments.substituteVariables(template, env);
			Arguments args = Arguments.createArgumentsFromJSONString(json, "");

			assertEquals(args.getData()[0].getProps().get(KEY_HEADER), env.get(KEY_HEADER));
			assertEquals(args.getData()[0].getFormat().type(), env.get(KEY_FORMAT));
			assertEquals(args.getModelId(), env.get(KEY_MODEL_ID));
		} catch (IOException | ZinggClientException e) {
			fail("Unexpected exception " + e.getMessage());
		}
	}

	@Test
	public void testSubstituteVariablesWithMissingEnvVar() {
		try {
			Map<String, String> env = new HashMap<String, String>();
			env.put(KEY_HEADER, "true");
			env.put(KEY_MODEL_ID, "400");

			byte[] encoded = Files
					.readAllBytes(Paths.get(getClass().getResource("../../testConfigTemplate.json.env").getFile()));

			String template = new String(encoded, StandardCharsets.UTF_8);
			String json = Arguments.substituteVariables(template, env);
			Arguments args = Arguments.createArgumentsFromJSONString(json, "");
			fail("Exception was expected due to missing environment variable");
 		} catch (IOException | ZinggClientException e) {
			LOG.warn("Expected exception received due to missing environment variable");
 		}
	}

	@Test
	public void testSubstituteVariablesWithBlankEnvVar() {
		try {
			Map<String, String> env = new HashMap<String, String>();
			env.put(KEY_HEADER, "true");
			env.put(KEY_FORMAT, "");
			env.put(KEY_MODEL_ID, "400");

			byte[] encoded = Files
					.readAllBytes(Paths.get(getClass().getResource("../../testConfigTemplate.json.env").getFile()));

			String template = new String(encoded, StandardCharsets.UTF_8);
			String json = Arguments.substituteVariables(template, env);
			Arguments args = Arguments.createArgumentsFromJSONString(json, "");

			fail("Exception was expected for blank value for an environment variable");
 		} catch (IOException | ZinggClientException e) {
 			LOG.warn("Expected exception received due to blank value for an environment variable");
		}
	}

	@Test
	public void testInvalidEnvVarBooleanType() {
		try {

			Map<String, String> env = new HashMap<String, String>();
			env.put(KEY_HEADER, "someValue");
			env.put(KEY_FORMAT, "csv");
			env.put(KEY_MODEL_ID, "400");

			byte[] encoded = Files
					.readAllBytes(Paths.get(getClass().getResource("../../testConfigTemplate.json.env").getFile()));

			String template = new String(encoded, StandardCharsets.UTF_8);
			String json = Arguments.substituteVariables(template, env);
			Arguments.createArgumentsFromJSONString(json, "");
 
			fail("Exception was expected for invalid value for a Boolean variable");
 		} catch (IOException | ZinggClientException e) {
			LOG.warn("Expected exception received due to invalid value for a Boolean variable");
 		}
	}

	@Test
	public void testBooleanType() {
		try {
			Map<String, String> env = new HashMap<String, String>();
			env.put(KEY_HEADER, "true");
			env.put(KEY_FORMAT, "csv");
			env.put(KEY_MODEL_ID, "400");

			byte[] encoded = Files
					.readAllBytes(Paths.get(getClass().getResource("../../testConfigTemplate.json.env").getFile()));

			String template = new String(encoded, StandardCharsets.UTF_8);
			String json = Arguments.substituteVariables(template, env);
			Arguments args = Arguments.createArgumentsFromJSONString(json, "");
 
			assertEquals(args.getOutput()[0].getProps().get(KEY_HEADER), env.get(KEY_HEADER));
		} catch (IOException | ZinggClientException e) {
			fail("Exception was not expected for valid value for a Boolean variable within quotes");

		}
	}

	@Test
	public void testInvalidEnvVarNumericType() {
		try {
			Map<String, String> env = new HashMap<String, String>();
			env.put(KEY_HEADER, "true");
			env.put(KEY_FORMAT, "csv");
			env.put(KEY_MODEL_ID, "ONEHUNDRED");

			byte[] encoded = Files
					.readAllBytes(Paths.get(getClass().getResource("../../testConfigTemplate.json.env").getFile()));

			String template = new String(encoded, StandardCharsets.UTF_8);
			String json = Arguments.substituteVariables(template, env);
			Arguments.createArgumentsFromJSONString(json, "");

			fail("Exception was expected for invalid value for a Numeric variable");
		} catch (IOException | ZinggClientException e) {
			LOG.warn("Expected exception received due to invalid value for a Numeric variable");
		}
	}

	@Test
	public void testNumericWithinQuotes() {
		try {
			
			Map<String, String> env = new HashMap<String, String>();
			env.put(KEY_HEADER, "true");
			env.put(KEY_FORMAT, "csv");
			env.put(KEY_MODEL_ID, "500");

			byte[] encoded = Files.readAllBytes(
					Paths.get(getClass().getResource("../../testNumericWithinQuotesTemplate.json.env").getFile()));

			String template = new String(encoded, StandardCharsets.UTF_8);
			String json = Arguments.substituteVariables(template, env);
			Arguments args = Arguments.createArgumentsFromJSONString(json, "");

			//Numeric within quotes are allowed
			assertEquals(args.getModelId(), env.get(KEY_MODEL_ID));
		} catch (IOException | ZinggClientException e) {
			fail("Unexpected exception in testNumericWithinQuotes()" + e.getMessage());
		}
	}

	@Test
	public void testMalformedVariable() {
		try {
			
			Map<String, String> env = new HashMap<String, String>();
			env.put(KEY_HEADER, "true");
			env.put(KEY_FORMAT, "csv");
			env.put(KEY_MODEL_ID, "500");

			byte[] encoded = Files.readAllBytes(
					Paths.get(getClass().getResource("../../testMalformedConfigTemplate.json.env").getFile()));

			String template = new String(encoded, StandardCharsets.UTF_8);
			String json = Arguments.substituteVariables(template, env);
			Arguments args = Arguments.createArgumentsFromJSONString(json, "");

			fail("Exception was expected for malformed variable in json");
		} catch (IOException | ZinggClientException e) {
			LOG.warn("Expected exception received due to malformed variable in json");
		}
	}

	@Test
	public void testInvalidFilePath() {
		String filePath = "../dummyFilename";
		try {
			Arguments.createArgumentsFromJSONTemplate(filePath, "");
			fail("Exception was expected for invalid filepath or name");
		} catch (ZinggClientException e) {
			LOG.warn("Expected exception received: NoSuchFileException");
		}
	}
}