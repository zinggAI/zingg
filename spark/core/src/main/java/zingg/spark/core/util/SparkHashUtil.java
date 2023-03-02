package zingg.spark.core.util;

import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.api.java.UDF1;
import org.apache.spark.sql.types.DataType;

import zingg.common.core.hash.HashFnFromConf;
import zingg.common.core.hash.HashFunction;
import zingg.spark.core.hash.SparkHashFunctionRegistry;
import zingg.common.core.util.BaseHashUtil;


public class SparkHashUtil extends BaseHashUtil<SparkSession,Dataset<Row>, Row, Column,DataType>{

	public SparkHashUtil(SparkSession spark) {
		super(spark);
	}
	
    public HashFunction<Dataset<Row>, Row, Column,DataType> registerHashFunction(HashFnFromConf scriptArg) {
        HashFunction<Dataset<Row>, Row, Column,DataType> fn = new SparkHashFunctionRegistry().getFunction(scriptArg.getName());
        getSessionObj().udf().register(fn.getName(), (UDF1) fn, fn.getReturnType());
        return fn;
    }
    
}
