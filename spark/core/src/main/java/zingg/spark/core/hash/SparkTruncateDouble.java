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

package zingg.spark.core.hash;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.functions;
import org.apache.spark.sql.api.java.UDF1;
import org.apache.spark.sql.types.DataType;
import org.apache.spark.sql.types.DataTypes;

import zingg.common.client.ZFrame;
import zingg.common.core.hash.TruncateDouble;

/**
 * Spark specific trunc function for double
 * 
 * 
 *
 */
public class SparkTruncateDouble extends SparkHashFunction<Double, Double>{
	
	public static final Log LOG = LogFactory.getLog(SparkTruncateDouble.class);
	
	public SparkTruncateDouble(int count){
	    setBaseHash(new TruncateDouble(count));
	    setDataType(DataTypes.DoubleType);
	    setReturnType(DataTypes.DoubleType);
	}
	
}
