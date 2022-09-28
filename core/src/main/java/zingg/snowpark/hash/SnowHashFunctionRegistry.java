package zingg.snowpark.hash;

import java.util.HashMap;

import com.snowflake.snowpark_java.Column;
import com.snowflake.snowpark_java.DataFrame;
import com.snowflake.snowpark_java.Row;
import com.snowflake.snowpark_java.types.DataType;

import zingg.hash.HashFunction;
import zingg.hash.HashFunctionRegistry;

public class SnowHashFunctionRegistry implements HashFunctionRegistry<DataFrame,Row,Column,DataType>{

	public HashMap<String, HashFunction<DataFrame,Row,Column,DataType>> fns 
		= new HashMap<String,HashFunction<DataFrame,Row,Column,DataType>>();
		
	public HashFunction<DataFrame,Row,Column,DataType> getFunction(String key) {
		return fns.get(key);
	}
	
	public SnowHashFunctionRegistry() {
		
		init(new SnowIdentityString());
		init(new SnowIdentityInteger());
		init(new SnowFirst1Chars());
		init(new SnowFirst2Chars());
		init(new SnowFirst3Chars());
		init(new SnowFirst4Chars());
		init(new SnowLast1Chars());
		init(new SnowLast2Chars());
		init(new SnowLast3Chars());
		init(new SnowRound());
		init(new SnowLastWord());
		init(new SnowFirst2CharsBox());
		init(new SnowFirst3CharsBox());
		init(new SnowIsNullOrEmpty());
	}
	
	public void init(HashFunction<DataFrame,Row,Column,DataType> fn) {
		fns.put(fn.getName(), fn);
	}

	
}
