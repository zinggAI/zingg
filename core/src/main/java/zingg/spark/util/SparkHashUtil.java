package zingg.spark.util;

import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import org.apache.spark.sql.types.DataType;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.SparkSession;
import zingg.client.util.ListMap;
import zingg.hash.HashFnFromConf;
import zingg.hash.HashFunction;
import zingg.spark.hash.SparkHashFunctionRegistry;
import zingg.util.HashUtil;

import java.util.List;
import org.apache.spark.sql.api.java.UDF1;


public class SparkHashUtil implements HashUtil<Dataset<Row>, Row, Column,DataType>{
    /**
	 * Use only those functions which are defined in the conf
	 * All functions exist in the registry
	 * but we return only those which are specified in the conf
	 * @param fileName
	 * @return
	 * @throws Exception
	 */

	
	public ListMap<DataType, HashFunction<Dataset<Row>, Row, Column,DataType>> getHashFunctionList(String fileName, Object spark)
			throws Exception {
		ListMap<DataType, HashFunction<Dataset<Row>, Row, Column,DataType>> functions = new ListMap<DataType, 
			HashFunction<Dataset<Row>, Row, Column,DataType>>();
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
		List<HashFnFromConf> scriptArgs = mapper.readValue(
				zingg.ZinggBase.class.getResourceAsStream("/" + fileName),
				new TypeReference<List<HashFnFromConf>>() {
				});
		for (HashFnFromConf scriptArg : scriptArgs) {
			HashFunction<Dataset<Row>, Row, Column,DataType> fn = new SparkHashFunctionRegistry().getFunction(scriptArg.getName());
			((SparkSession)spark).udf().register(fn.getName(), (UDF1) fn, fn.getReturnType());
			functions.add(fn.getDataType(), fn);
		}
		return functions;
	}

	
}
