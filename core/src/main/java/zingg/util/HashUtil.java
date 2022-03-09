package zingg.util;

import com.snowflake.snowpark_java.types.DataType;
import com.snowflake.snowpark_java.Session;
import zingg.client.util.ListMap;
import zingg.hash.HashFnFromConf;
import zingg.hash.HashFunction;
import zingg.hash.HashFunctionRegistry;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.snowflake.snowpark_java.udf.JavaUDF1;


public class HashUtil {
    /**
	 * Use only those functions which are defined in the conf
	 * All functions exist in the registry
	 * but we return only those which are specified in the conf
	 * @param fileName
	 * @return
	 * @throws Exception
	 */
	public static ListMap<DataType, HashFunction> getHashFunctionList(String fileName, Session snow)
			throws Exception {
		ListMap<DataType, HashFunction> functions = new ListMap<DataType, HashFunction>();
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
		List<HashFnFromConf> scriptArgs = mapper.readValue(
				zingg.ZinggBase.class.getResourceAsStream("/" + fileName),
				new TypeReference<List<HashFnFromConf>>() {
				});
		for (HashFnFromConf scriptArg : scriptArgs) {
			HashFunction fn = HashFunctionRegistry.getFunction(scriptArg.getName());
			snow.udf().registerPermanent(fn.getName(), (JavaUDF1) fn, fn.getDataType(), fn.getReturnType(), "stageLocation");
			functions.add(fn.getDataType(), fn);
		}
		return functions;
	}
}
