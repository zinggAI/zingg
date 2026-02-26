package zingg.common.client;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.rmi.NoSuchObjectException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.Test;
import zingg.common.client.arguments.ArgumentServiceImpl;
import zingg.common.client.arguments.IArgumentService;
import zingg.common.client.arguments.model.Arguments;
import zingg.common.client.arguments.model.IArguments;
import zingg.common.client.arguments.loader.template.EnvironmentVariableSubstitutor;

import static org.junit.jupiter.api.Assertions.*;


public class TestArguments {

	private static final String KEY_HEADER = "header";
	private static final String KEY_FORMAT = "format";
	private static final String KEY_MODEL_ID = "modelId";
	protected final IArgumentService<Arguments> argumentService;
	public static final Log LOG = LogFactory.getLog(TestArguments.class);
	protected final EnvironmentVariableSubstitutor environmentVariableSubstitutor;

	public TestArguments() {
		this.argumentService = new ArgumentServiceImpl<>(Arguments.class);
		this.environmentVariableSubstitutor = new EnvironmentVariableSubstitutor();
	}
	
	@Test
	public void testSubstituteVariablesWithAllEnvVarSet() throws IOException, ZinggClientException {
		Map<String, String> env = new HashMap<>();
		env.put(KEY_HEADER, "true");
		env.put(KEY_FORMAT, "csv");
		env.put(KEY_MODEL_ID, "400");

		byte[] encoded = Files
				.readAllBytes(Paths.get(getClass().getResource("../../../testArguments/testConfigTemplate.json.env").getFile()));
		String template = new String(encoded, StandardCharsets.UTF_8);
		String substitutedJsonString = environmentVariableSubstitutor.substitute(template, env);
		IArguments arguments = argumentService.loadArguments(substitutedJsonString);
		assertEquals(arguments.getData()[0].getProps().get(KEY_HEADER), env.get(KEY_HEADER));
		assertEquals(arguments.getData()[0].getFormat(), env.get(KEY_FORMAT));
		assertEquals(arguments.getModelId(), env.get(KEY_MODEL_ID));
    }

	@Test
	public void testSubstituteVariablesWithMissingEnvVar() throws IOException {

		Map<String, String> env = new HashMap<>();
		env.put(KEY_HEADER, "true");
		env.put(KEY_MODEL_ID, "400");

		byte[] encoded = Files.readAllBytes(
				Paths.get(getClass()
						.getResource("../../../testArguments/testConfigTemplate.json.env")
						.getFile())
		);

		String template = new String(encoded, StandardCharsets.UTF_8);

		assertThrows(ZinggClientException.class, () -> {
			String substitutedJsonString =
					environmentVariableSubstitutor.substitute(template, env);

			argumentService.loadArguments(substitutedJsonString);
		});
	}


	@Test
	public void testSubstituteVariablesWithBlankEnvVar() throws IOException {
		Map<String, String> env = new HashMap<>();
		env.put(KEY_HEADER, "true");
		env.put(KEY_FORMAT, "");
		env.put(KEY_MODEL_ID, "400");

		byte[] encoded = Files.readAllBytes(
				Paths.get(getClass()
						.getResource("../../../testArguments/testConfigTemplate.json.env")
						.getFile()));

		String template = new String(encoded, StandardCharsets.UTF_8);

		assertThrows(ZinggClientException.class, () -> {
			String substitutedJsonString =
					environmentVariableSubstitutor.substitute(template, env);

			argumentService.loadArguments(substitutedJsonString);
		});
	}

	@Test
	public void testInvalidEnvVarBooleanType() throws IOException {
//		try {

		Map<String, String> env = new HashMap<>();
		env.put(KEY_HEADER, "someValue");
		env.put(KEY_FORMAT, "csv");
		env.put(KEY_MODEL_ID, "400");

		byte[] encoded = Files.readAllBytes(
				Paths.get(getClass()
						.getResource("../../../testArguments/testConfigTemplate.json.env")
						.getFile()));

		String template = new String(encoded, StandardCharsets.UTF_8);
		assertThrows(ZinggClientException.class, () -> {
			String substitutedJsonString =
					environmentVariableSubstitutor.substitute(template, env);

			argumentService.loadArguments(substitutedJsonString);
		});
	}

	@Test
	public void testBooleanType() throws IOException, ZinggClientException {
		Map<String, String> env = new HashMap<>();
		env.put(KEY_HEADER, "true");
		env.put(KEY_FORMAT, "csv");
		env.put(KEY_MODEL_ID, "400");

		byte[] encoded = Files
				.readAllBytes(Paths.get(getClass().getResource("../../../testArguments/testConfigTemplate.json.env").getFile()));

		String template = new String(encoded, StandardCharsets.UTF_8);
		String substitutedJsonString = environmentVariableSubstitutor.substitute(template, env);
		IArguments args = argumentService.loadArguments(substitutedJsonString);

		assertEquals(args.getOutput()[0].getProps().get(KEY_HEADER), env.get(KEY_HEADER));
	}

	@Test
	public void testInvalidEnvVarNumericType() throws IOException {
		Map<String, String> env = new HashMap<>();
		env.put(KEY_HEADER, "true");
		env.put(KEY_FORMAT, "csv");
		env.put(KEY_MODEL_ID, "ONEHUNDRED");

		byte[] encoded = Files.readAllBytes(
				Paths.get(getClass()
						.getResource("../../../testArguments/testConfigTemplate.json.env")
						.getFile()));

		String template = new String(encoded, StandardCharsets.UTF_8);
		assertThrows(ZinggClientException.class, () -> {
			String substitutedJsonString =
					environmentVariableSubstitutor.substitute(template, env);

			argumentService.loadArguments(substitutedJsonString);
		});
	}

	@Test
	public void testNumericWithinQuotes() throws IOException, ZinggClientException {
		Map<String, String> env = new HashMap<>();
		env.put(KEY_HEADER, "true");
		env.put(KEY_FORMAT, "csv");
		env.put(KEY_MODEL_ID, "500");

		byte[] encoded = Files.readAllBytes(
				Paths.get(getClass().getResource("../../../testArguments/testNumericWithinQuotesTemplate.json.env").getFile()));

		String template = new String(encoded, StandardCharsets.UTF_8);
		String substitutedJsonString = environmentVariableSubstitutor.substitute(template, env);
		IArguments args = argumentService.loadArguments(substitutedJsonString);
		//Numeric within quotes are allowed
		assertEquals(args.getModelId(), env.get(KEY_MODEL_ID));
	}

	@Test
	public void testMalformedVariable() throws IOException {
		Map<String, String> env = new HashMap<>();
		env.put(KEY_HEADER, "true");
		env.put(KEY_FORMAT, "csv");
		env.put(KEY_MODEL_ID, "500");

		byte[] encoded = Files.readAllBytes(
				Paths.get(getClass().getResource("../../../testArguments/testMalformedConfigTemplate.json.env").getFile()));

		String template = new String(encoded, StandardCharsets.UTF_8);
		assertThrows(ZinggClientException.class, () -> {
			String substitutedJsonString =
					environmentVariableSubstitutor.substitute(template, env);

			argumentService.loadArguments(substitutedJsonString);
		});
	}

	@Test
	public void testInvalidFilePath() {
		String filePath = "../dummyFilename";

		assertThrows(ZinggClientException.class, () -> {
			argumentService.loadArguments(filePath);
		});
	}


	@Test
	public void testMatchTypeMultiple() throws ZinggClientException, NoSuchObjectException {
		IArguments args = argumentService.loadArguments(getClass().getResource("../../../testArguments/configWithMultipleMatchTypes.json").getFile());
		List<? extends IMatchType> fNameMatchType = args.getFieldDefinition().get(0).getMatchType();
		assertEquals(2, fNameMatchType.size());
		assertEquals(MatchTypes.FUZZY, fNameMatchType.get(0));
		assertEquals(MatchTypes.NULL_OR_BLANK, fNameMatchType.get(1));
	}

	@Test
	public void testMatchTypeWrong() throws NoSuchObjectException, ZinggClientException {
			IArguments args = argumentService.loadArguments(getClass().getResource("../../../testArguments/configWithMultipleMatchTypesUnsupported.json").getFile());
	}

	@Test
	public void testJsonStringify() throws NoSuchObjectException, ZinggClientException {
		IArguments argsFromJsonFile = argumentService.loadArguments(getClass().getResource("../../../testArguments/configWithMultipleMatchTypes.json").getFile());
		String strFromJsonFile = argsFromJsonFile.toString();

		IArguments argsFullCycle = argumentService.loadArguments(strFromJsonFile);

		assertEquals(argsFullCycle.getFieldDefinition().get(0).getName(), argsFromJsonFile.getFieldDefinition().get(0).getName());
		assertEquals(argsFullCycle.getFieldDefinition().get(2).getName(), argsFromJsonFile.getFieldDefinition().get(2).getName());
		assertEquals(argsFullCycle.getModelId(), argsFromJsonFile.getModelId());
		assertEquals(argsFullCycle.getNumPartitions(), argsFromJsonFile.getNumPartitions());
		assertEquals(argsFullCycle.getLabelDataSampleSize() ,argsFromJsonFile.getLabelDataSampleSize());
		assertEquals(argsFullCycle.getZinggDir(),argsFromJsonFile.getZinggDir());
		assertEquals(argsFullCycle.getJobId(),argsFromJsonFile.getJobId());
	}		
	
}
