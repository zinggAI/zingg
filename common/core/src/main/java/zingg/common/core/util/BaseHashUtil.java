package zingg.common.core.util;

import java.util.List;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import zingg.common.client.util.ListMap;
import zingg.common.core.hash.HashFnFromConf;
import zingg.common.core.hash.HashFunction;


public abstract class BaseHashUtil<S,D,R,C,T> implements HashUtil<S,D,R,C,T>{
    
    public static final String HASH_FUNCTIONS_JSON_FILE = "hashFunctions.json";
    
    /**
	 * Use only those functions which are defined in the conf
	 * All functions exist in the registry
	 * but we return only those which are specified in the conf
	 * @param fileName
	 * @return
	 * @throws Exception
	 */

	private S sessionObj;

	public BaseHashUtil(S sessionObj) {
		this.sessionObj = sessionObj;
	}
	
	public ListMap<T, HashFunction<D,R,C,T>> getHashFunctionList(String fileName)
			throws Exception {
		ListMap<T, HashFunction<D,R,C,T>> functions = new ListMap<T, HashFunction<D,R,C,T>>();
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS,
					true);
		List<HashFnFromConf> scriptArgs = mapper.readValue(
				zingg.common.core.executor.ZinggBaseCommon.class.getResourceAsStream("/" + fileName),
				new TypeReference<List<HashFnFromConf>>() {
				});
		for (HashFnFromConf scriptArg : scriptArgs) {
		    HashFunction<D,R,C,T> fn = registerHashFunction(scriptArg);
		    functions.add(fn.getDataType(), fn);
		}
		return functions;
	}

    public abstract HashFunction<D,R,C,T> registerHashFunction(HashFnFromConf scriptArg);

	@Override
	public ListMap<T, HashFunction<D,R,C,T>> getHashFunctionList() throws Exception {
		return getHashFunctionList(HASH_FUNCTIONS_JSON_FILE);
	}

    public S getSessionObj() {
        return sessionObj;
    }

    public void setSessionObj(S sessionObj) {
        this.sessionObj = sessionObj;
    }
	
}
