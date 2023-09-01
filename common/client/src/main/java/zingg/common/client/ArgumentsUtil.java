package zingg.common.client;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.json.JsonWriteFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.module.scala.DefaultScalaModule;

public class ArgumentsUtil {
	
	protected Class<? extends Arguments> argsClass;
	private static final String ENV_VAR_MARKER_START = "$";
	private static final String ENV_VAR_MARKER_END = "$";
	private static final String ESC = "\\";
	private static final String PATTERN_ENV_VAR = ESC + ENV_VAR_MARKER_START + "(.+?)" + ESC + ENV_VAR_MARKER_END;
	public static final Log LOG = LogFactory.getLog(ArgumentsUtil.class);	
	
	
	public ArgumentsUtil() {
		this(Arguments.class);
	}

	public ArgumentsUtil( Class<? extends Arguments> argsClass) {
		this.argsClass = argsClass;
	}

	/**
	 * Create arguments from a json file
	 * 
	 * @param filePath
	 *            json file containing arguments
	 * @return Arguments object populated through JSON
	 * @throws ZinggClientException
	 *             in case of invalid/wrong json/file not found
	 */
	public Arguments createArgumentsFromJSON(String filePath)
			throws ZinggClientException {
		return createArgumentsFromJSON(filePath, "match");
	}

	/**
	 * Create arguments from a json file
	 * 
	 * @param filePath
	 *            json file containing arguments
	 * @return Arguments object populated through JSON
	 * @throws ZinggClientException
	 *             in case of invlaid/wrong json/file not found
	 */
	public Arguments createArgumentsFromJSON(String filePath, String phase)
			throws ZinggClientException {
		try {
			ObjectMapper mapper = new ObjectMapper();
			mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS,
					true);
			LOG.warn("Config Argument is " + filePath);
			/*SimpleModule module = new SimpleModule();
			module.addDeserializer(List<MatchType>.class, new FieldDefinition.MatchTypeDeserializer());
			mapper.registerModule(module);
			*/
			Arguments args = mapper.readValue(new File(filePath), argsClass);
			LOG.warn("phase is " + phase);
			checkValid(args, phase);
			return args;			
		} catch (Exception e) { 
			//e.printStackTrace();
			throw new ZinggClientException("Unable to parse the configuration at " + filePath + 
					". The error is " + e.getMessage(), e);
		}
	}
	
	/**
	 * Write arguments to a json file
	 * 
	 * @param filePath
	 *            json file where arguments shall be written to
	 * @return Arguments object
	 * @throws ZinggClientException
	 *             in case there is an error in writing to file
	 */
	public void writeArgumentsToJSON(String filePath, Arguments args)
			throws ZinggClientException {
		try {
			ObjectMapper mapper = new ObjectMapper();
			mapper.enable(SerializationFeature.INDENT_OUTPUT);
			mapper.getFactory().configure(JsonWriteFeature.QUOTE_FIELD_NAMES.mappedFeature(),true);
			LOG.warn("Arguments are written to file: " + filePath);		
			mapper.writeValue(new File(filePath), args);
		} catch (Exception e) { 
			throw new ZinggClientException("Unable to write the configuration to " + filePath + 
					". The error is " + e.getMessage(), e);
		}
	}

	public void checkValid(Arguments args, String phase) throws ZinggClientException {
		if (phase.equals("train") || phase.equals("match") || phase.equals("trainMatch") || phase.equals("link")) {
			checkIsValid(args);
		}
		else if(phase.equals("seed") || phase.equals("seedDB")){
			checkIsValidForLabelling(args);
		}
		else if (!phase.equalsIgnoreCase("WEB")){
			checkIsValidForOthers(args);
		}
	}
	
	public Arguments createArgumentsFromJSONString(String data, String phase)
			throws ZinggClientException {
		try {
			ObjectMapper mapper = new ObjectMapper();
			mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS,
					true);
			Arguments args = mapper.readValue(data, argsClass);
			LOG.warn("phase is " + phase);
			checkValid(args, phase);
			return args;			
		} catch (Exception e) {
			//e.printStackTrace();
			throw new ZinggClientException("Unable to parse the configuration at " + data + 
					". The error is " + e.getMessage());
		}
	}

	public Arguments createArgumentsFromJSONTemplate(String filePath, String phase)
			throws ZinggClientException {
		try {
			LOG.warn("Config Argument is " + filePath);
			byte[] encoded = Files.readAllBytes(Paths.get(filePath));
			String template = new String(encoded, StandardCharsets.UTF_8);
			Map<String, String> env = System.getenv();
			String updatedJson = substituteVariables(template, env);
			Arguments args = createArgumentsFromJSONString(updatedJson, phase);
			return args;
		} catch (Exception e) {
			//e.printStackTrace();
			throw new ZinggClientException("Unable to parse the configuration at " + filePath +
					". The error is " + e.getMessage());
		}
	}

	public String substituteVariables(String template, Map<String, String> variables) throws ZinggClientException {
		Pattern pattern = Pattern.compile(PATTERN_ENV_VAR);
		Matcher matcher = pattern.matcher(template);
		// StringBuilder cannot be used here because Matcher expects StringBuffer
		StringBuffer buffer = new StringBuffer();
		while (matcher.find()) {
			if (variables.containsKey(matcher.group(1))) {
				String replacement = variables.get(matcher.group(1));
				if (replacement == null || replacement.equals("")) {
					throw new ZinggClientException("The environment variable for " + ENV_VAR_MARKER_START
							+ matcher.group(1) + ENV_VAR_MARKER_END + " is not set or is empty string");
				}
				// quote to work properly with $ and {,} signs
				matcher.appendReplacement(buffer, replacement != null ? Matcher.quoteReplacement(replacement) : "null");
				LOG.warn("The variable " + ENV_VAR_MARKER_START + matcher.group(1) + ENV_VAR_MARKER_END
						+ " has been substituted");
			} else {
				throw new ZinggClientException("The environment variable for " + ENV_VAR_MARKER_START + matcher.group(1)
						+ ENV_VAR_MARKER_END + " is not set");
			}
		}
		matcher.appendTail(buffer);
		return buffer.toString();
	}

	public void writeArgumentstoJSON(String filePath, Arguments args) throws ZinggClientException {
		try{
			ObjectMapper mapper = new ObjectMapper();
			mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS,
					true);
			mapper.registerModule(new DefaultScalaModule());
			mapper.writerWithDefaultPrettyPrinter().writeValue(new File(filePath), args);
		}
		catch(Exception e) {
			e.printStackTrace();
			throw new ZinggClientException("Unable to create arguments for the job");
		}
	}

	public String writeArgumentstoJSONString(Arguments args) throws ZinggClientException {
		try{
			ObjectMapper mapper = new ObjectMapper();
			mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS,
					true);
			mapper.registerModule(new DefaultScalaModule());
			return mapper.writeValueAsString(args);
		}
		catch(Exception e) {
			e.printStackTrace();
			throw new ZinggClientException("Unable to create arguments for the job");
		}
	}
	
	/**
	 * Checks if the given arguments are correct or not
	 * @param args
	 * @throws ZinggClientException
	 */
	public void checkIsValid(Arguments args) throws ZinggClientException {
		Arguments arg = new Arguments();
		arg.setTrainingSamples(args.getTrainingSamples());
		arg.setData(args.getData());
		arg.setNumPartitions(args.getNumPartitions());
		arg.setFieldDefinition(args.getFieldDefinition());
	}
	
	public void checkIsValidForOthers(Arguments args) throws ZinggClientException {
		Arguments arg = new Arguments();
		arg.setData(args.getData());
		arg.setNumPartitions(args.getNumPartitions());
	}
	
	
	public void checkIsValidForLabelling(Arguments args) throws ZinggClientException {
		Arguments arg = new Arguments();
		//arg.setPositiveTrainingSamples(args.getPositiveTrainingSamples());
		//arg.setNegativeTrainingSamples(args.getNegativeTrainingSamples());
		arg.setData(args.getData());
		arg.setNumPartitions(args.getNumPartitions());
		arg.setFieldDefinition(args.getFieldDefinition());
	}
	

}
