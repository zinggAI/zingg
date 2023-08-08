/*
 * Zingg
 * Copyright (C) 2021-Present  Zingg Labs,inc
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package zingg.spark.core.util;

import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.api.java.UDF1;
import org.apache.spark.sql.types.DataType;

import zingg.common.core.hash.HashFnFromConf;
import zingg.common.core.hash.HashFunction;
import zingg.common.core.util.BaseHashUtil;
import zingg.spark.client.ZSparkSession;
import zingg.spark.core.hash.SparkHashFunctionRegistry;


public class SparkHashUtil extends BaseHashUtil<ZSparkSession,Dataset<Row>, Row, Column,DataType>{

	public SparkHashUtil(ZSparkSession spark) {
		super(spark);
	}
	
    public HashFunction<Dataset<Row>, Row, Column,DataType> registerHashFunction(HashFnFromConf scriptArg) {
        HashFunction<Dataset<Row>, Row, Column,DataType> fn = new SparkHashFunctionRegistry().getFunction(scriptArg.getName());
        getSessionObj().getSession().udf().register(fn.getName(), (UDF1) fn, fn.getReturnType());
        return fn;
    }
    
}
