package zingg.util;

import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import org.apache.spark.sql.types.DataType;
import org.apache.spark.sql.SparkSession;
import zingg.client.util.ListMap;
import zingg.hash.HashFnFromConf;
import zingg.hash.HashFunction;
import zingg.hash.HashFunctionRegistry;

import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.sql.api.java.UDF1;


public class HashUtil {
    /**
	 * Use only those functions which are defined in the conf
	 * All functions exist in the registry
	 * but we return only those which are specified in the conf
	 * @param fileName
	 * @return
	 * @throws Exception
	 */
	public static ListMap<DataType, HashFunction> getHashFunctionList(String fileName, SparkSession spark)
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
			spark.udf().register(fn.getName(), (UDF1) fn, fn.getReturnType());
			functions.add(fn.getDataType(), fn);
		}
		return functions;
	}
}
