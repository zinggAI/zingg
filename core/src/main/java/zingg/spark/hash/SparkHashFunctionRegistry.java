package zingg.spark.hash;

import java.util.HashMap;

import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.types.DataType;

import zingg.hash.HashFunction;
import zingg.hash.HashFunctionRegistry;

public class SparkHashFunctionRegistry implements HashFunctionRegistry<Dataset<Row>,Row,Column,DataType>{

	public HashMap<String, HashFunction<Dataset<Row>,Row,Column,DataType>> fns 
		= new HashMap<String,HashFunction<Dataset<Row>,Row,Column,DataType>>();
		
	public HashFunction<Dataset<Row>,Row,Column,DataType> getFunction(String key) {
		return fns.get(key);
	}
	
	public SparkHashFunctionRegistry() {
		
		init(new SparkIdentityString());
		init(new SparkIdentityInteger());
		init(new SparkFirst1Chars());
		init(new SparkFirst2Chars());
		init(new SparkFirst3Chars());
		init(new SparkFirst4Chars());
		init(new SparkLast1Chars());
		init(new SparkLast2Chars());
		init(new SparkLast3Chars());
		init(new SparkRound());
		init(new SparkLastWord());
		init(new SparkFirst2CharsBox());
		init(new SparkFirst3CharsBox());
		init(new SparkIsNullOrEmpty());
	}
	
	public void init(HashFunction<Dataset<Row>,Row,Column,DataType> fn) {
		fns.put(fn.getName(), fn);
	}

	
}
