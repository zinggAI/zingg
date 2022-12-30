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
		
	    init(new SparkFirst1Chars());
	    init(new SparkFirst2Chars());
	    init(new SparkFirst2CharsBox());
	    init(new SparkFirst3Chars());
	    init(new SparkFirst3CharsBox());
	    init(new SparkFirst4Chars());
	    init(new SparkIdentityInteger());
	    init(new SparkIdentityString());
	    init(new SparkIsNullOrEmpty());
	    init(new SparkLast1Chars());
	    init(new SparkLast2Chars());
	    init(new SparkLast3Chars());
	    init(new SparkLastWord());
	    init(new SparkLessThanZeroDbl());
	    init(new SparkLessThanZeroInt());
	    init(new SparkRangeBetween0And10Dbl());
	    init(new SparkRangeBetween0And10Int());
	    init(new SparkRangeBetween1000And10000Dbl());
	    init(new SparkRangeBetween1000And10000Int());
	    init(new SparkRangeBetween100And1000Dbl());
	    init(new SparkRangeBetween100And1000Int());
	    init(new SparkRangeBetween10And100Dbl());
	    init(new SparkRangeBetween10And100Int());
	    init(new SparkRound());
	    init(new SparkTrimLast1DigitDbl());
	    init(new SparkTrimLast1DigitInt());
	    init(new SparkTrimLast2DigitsDbl());
	    init(new SparkTrimLast2DigitsInt());
	    init(new SparkTrimLast3DigitsDbl());
	    init(new SparkTrimLast3DigitsInt());
	    init(new SparkTruncateDoubleTo1Place());
	    init(new SparkTruncateDoubleTo2Places());
	    init(new SparkTruncateDoubleTo3Places());		
		
	}
	
	public void init(HashFunction<Dataset<Row>,Row,Column,DataType> fn) {
		fns.put(fn.getName(), fn);
	}

	
}
