package zingg.spark.core.util;

import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.api.java.UDF1;
import org.apache.spark.sql.types.DataType;

import zingg.common.core.hash.HashFnFromConf;
import zingg.common.core.hash.HashFunction;
import zingg.common.core.util.BaseHashUtil;
import org.apache.spark.sql.SparkSession;
import zingg.spark.core.hash.SparkHashFunctionRegistry;


public class SparkHashUtil extends BaseHashUtil<SparkSession,Dataset<Row>, Row, Column,DataType>{

	public SparkHashUtil(SparkSession spark) {
		super(spark);
	}
	
    public HashFunction<Dataset<Row>, Row, Column,DataType> registerHashFunction(HashFnFromConf scriptArg) {
        HashFunction<Dataset<Row>, Row, Column,DataType> fn = new SparkHashFunctionRegistry().getFunction(scriptArg.getName());

        //register udf only if it is not registered already
        if (!getSessionObj().catalog().functionExists(fn.getName())) {
            getSessionObj().udf().register(fn.getName(), (UDF1) fn, fn.getReturnType());
        }
        return fn;
    }
    
}
