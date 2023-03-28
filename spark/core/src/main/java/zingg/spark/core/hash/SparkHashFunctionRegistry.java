package zingg.spark.core.hash;

import java.util.HashMap;

import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.types.DataType;

import zingg.common.core.hash.HashFunction;
import zingg.common.core.hash.HashFunctionRegistry;

public class SparkHashFunctionRegistry implements HashFunctionRegistry<Dataset<Row>,Row,Column,DataType>{

	public HashMap<String, HashFunction<Dataset<Row>,Row,Column,DataType>> fns 
		= new HashMap<String,HashFunction<Dataset<Row>,Row,Column,DataType>>();
		
	public HashFunction<Dataset<Row>,Row,Column,DataType> getFunction(String key) {
		return fns.get(key);
	}
	
	public SparkHashFunctionRegistry() {
		
	    init(new SparkFirstChars(1));
	    init(new SparkFirstChars(2));
	    init(new SparkFirstChars(3));
	    init(new SparkFirstChars(4));
	    
        init(new SparkLastChars(1));
        init(new SparkLastChars(2));
        init(new SparkLastChars(3));
        
        init(new SparkLastWord());
        
        init(new SparkIsNullOrEmpty());
        
	    init(new SparkIdentityString());
	    
        init(new SparkFirst2CharsBox());
        init(new SparkFirst3CharsBox());
        
        init(new SparkIdentityInteger());
        
        init(new SparkIdentityLong());
	
        init(new SparkTruncateDouble(1));
        init(new SparkTruncateDouble(2));
        init(new SparkTruncateDouble(3));  
        
        init(new SparkTruncateFloat(1));
        init(new SparkTruncateFloat(2));
        init(new SparkTruncateFloat(3));       
        
	    init(new SparkLessThanZeroDbl());
	    
	    init(new SparkLessThanZeroFloat());
	    
	    init(new SparkLessThanZeroInt());
	    
	    init(new SparkLessThanZeroLong());
	    
        init(new SparkTrimLastDigitsDbl(1));
        init(new SparkTrimLastDigitsDbl(2));
        init(new SparkTrimLastDigitsDbl(3));

        init(new SparkTrimLastDigitsFloat(1));
        init(new SparkTrimLastDigitsFloat(2));
        init(new SparkTrimLastDigitsFloat(3));
        
        init(new SparkTrimLastDigitsInt(1));
        init(new SparkTrimLastDigitsInt(2));        
        init(new SparkTrimLastDigitsInt(3));

        init(new SparkTrimLastDigitsLong(1));
        init(new SparkTrimLastDigitsLong(2));        
        init(new SparkTrimLastDigitsLong(3));
        
	    init(new SparkRangeDbl(0,10));
	    init(new SparkRangeDbl(10,100));
	    init(new SparkRangeDbl(100,1000));
	    init(new SparkRangeDbl(1000,10000));    

	    init(new SparkRangeFloat(0,10));
	    init(new SparkRangeFloat(10,100));
	    init(new SparkRangeFloat(100,1000));
	    init(new SparkRangeFloat(1000,10000));    
	    
	    init(new SparkRangeInt(0,10));
	    init(new SparkRangeInt(10,100));
	    init(new SparkRangeInt(100,1000));
	    init(new SparkRangeInt(1000,10000));

	    init(new SparkRangeLong(0,10));
	    init(new SparkRangeLong(10,100));
	    init(new SparkRangeLong(100,1000));
	    init(new SparkRangeLong(1000,10000));
	    
	    init(new SparkRound());
		
	}
	
	public void init(HashFunction<Dataset<Row>,Row,Column,DataType> fn) {
		fns.put(fn.getName(), fn);
	}

	
}
