package zingg.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.Test;

import zingg.client.pipe.Format;
import zingg.client.pipe.Pipe;

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
					.readAllBytes(Paths.get(getClass().getResource("../../testArguments/testConfigTemplate.json.env").getFile()));
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
					.readAllBytes(Paths.get(getClass().getResource("../../testArguments/testConfigTemplate.json.env").getFile()));

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
					.readAllBytes(Paths.get(getClass().getResource("../../testArguments/testConfigTemplate.json.env").getFile()));

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
					.readAllBytes(Paths.get(getClass().getResource("../../testArguments/testConfigTemplate.json.env").getFile()));

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
					.readAllBytes(Paths.get(getClass().getResource("../../testArguments/testConfigTemplate.json.env").getFile()));

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
					.readAllBytes(Paths.get(getClass().getResource("../../testArguments/testConfigTemplate.json.env").getFile()));

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
					Paths.get(getClass().getResource("../../testArguments/testNumericWithinQuotesTemplate.json.env").getFile()));

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
					Paths.get(getClass().getResource("../../testArguments/testMalformedConfigTemplate.json.env").getFile()));

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

	@Test
	public void testMatchTypeMultiple() {
			Arguments args;
            try {
                args = Arguments.createArgumentsFromJSON(getClass().getResource("../../testArguments/configWithMultipleMatchTypes.json").getFile(), "test");
				List<MatchType> fNameMatchType = args.getFieldDefinition().get(0).getMatchType();
				assertEquals(2, fNameMatchType.size());
				assertEquals(MatchType.FUZZY, fNameMatchType.get(0));
				assertEquals(MatchType.NULL_OR_BLANK, fNameMatchType.get(1));

				
            } catch (Exception | ZinggClientException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
				fail("Could not read config");
            }
		
	}

	@Test
	public void testMatchTypeWrong() {
			Arguments args;
            try {
                args = Arguments.createArgumentsFromJSON(getClass().getResource("../../testArguments/configWithMultipleMatchTypesUnsupported.json").getFile(), "test");
				//List<MatchType> fNameMatchType = args.getFieldDefinition().get(0).getMatchType();
				fail("config had error, should have flagged");
				
            } catch (Exception | ZinggClientException e) {
                // TODO Auto-generated catch block
               // e.printStackTrace();				
            }
			
			
		
	}

	@Test
	public void testWriteArgumentObjectToJSONFile() {
			Arguments args = new Arguments();
			try {
				FieldDefinition fname = new FieldDefinition();
				fname.setFieldName("fname");
				fname.setDataType("\"string\"");
				fname.setMatchType(Arrays.asList(MatchType.EXACT, MatchType.FUZZY, MatchType.PINCODE));
				//fname.setMatchType(Arrays.asList(MatchType.EXACT));
				fname.setFields("fname");
				FieldDefinition lname = new FieldDefinition();
				lname.setFieldName("lname");
				lname.setDataType("\"string\"");
				lname.setMatchType(Arrays.asList(MatchType.FUZZY));
				lname.setFields("lname");
				args.setFieldDefinition(Arrays.asList(fname, lname));

				Pipe inputPipe = new Pipe();
				inputPipe.setName("test");
				inputPipe.setFormat(Format.CSV);
				inputPipe.setProp("location", "examples/febrl/test.csv");
				args.setData(new Pipe[] {inputPipe});

				Pipe outputPipe = new Pipe();
				outputPipe.setName("output");
				outputPipe.setFormat(Format.CSV);
				outputPipe.setProp("location", "examples/febrl/output.csv");
				args.setOutput(new Pipe[] {outputPipe});

				args.setBlockSize(400L);
				args.setCollectMetrics(true);
				args.setModelId("500");
                Arguments.writeArgumentsToJSON("/tmp/configFromArgObject.json", args);

				//reload the same config file to check if deserialization is successful
				Arguments newArgs = Arguments.createArgumentsFromJSON("/tmp/configFromArgObject.json", "test");
				assertEquals(newArgs.getModelId(), "500", "Model id is different");
				assertEquals(newArgs.getBlockSize(), 400L, "Block size is different");
				assertEquals(newArgs.getFieldDefinition().get(0).getFieldName(), "fname", "Field Definition[0]'s name is different");
				String expectedMatchType =  "[EXACT, FUZZY, PINCODE]";
				assertEquals(newArgs.getFieldDefinition().get(0).getMatchType().toString(), expectedMatchType);
			} catch (Exception | ZinggClientException e) {
				e.printStackTrace();
			}
		}
	}
