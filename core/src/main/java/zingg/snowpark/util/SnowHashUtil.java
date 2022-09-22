package zingg.snowpark.util;

import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import com.snowflake.snowpark_java.Column;
import com.snowflake.snowpark_java.DataFrame;
import com.snowflake.snowpark_java.types.DataType;
import com.snowflake.snowpark_java.Row;
import com.snowflake.snowpark_java.Session;

import zingg.client.util.ListMap;
import zingg.hash.HashFnFromConf;
import zingg.hash.HashFunction;
import zingg.spark.hash.SparkHashFunctionRegistry;
import zingg.util.HashUtil;

import java.util.List;
import org.apache.spark.sql.api.java.UDF1;


public class SnowHashUtil implements HashUtil<DataFrame, Row, Column,DataType>{
    /**
	 * Use only those functions which are defined in the conf
	 * All functions exist in the registry
	 * but we return only those which are specified in the conf
	 * @param fileName
	 * @return
	 * @throws Exception
	 */

	
	public ListMap<DataType, HashFunction<DataFrame, Row, Column,DataType>> getHashFunctionList(String fileName, Object spark)
			throws Exception {
		ListMap<DataType, HashFunction<DataFrame, Row, Column,DataType>> functions = new ListMap<DataType, 
			HashFunction<DataFrame, Row, Column,DataType>>();
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
		List<HashFnFromConf> scriptArgs = mapper.readValue(
				zingg.ZinggBase.class.getResourceAsStream("/" + fileName),
				new TypeReference<List<HashFnFromConf>>() {
				});
		for (HashFnFromConf scriptArg : scriptArgs) {
			HashFunction<DataFrame, Row, Column,DataType> fn = new SnowHashFunctionRegistry().getFunction(scriptArg.getName());
			((Session)snow).udf().register(fn.getName(), (UDF1) fn, fn.getReturnType());
			functions.add(fn.getDataType(), fn);
		}
		return functions;
	}
}
