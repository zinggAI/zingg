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

package zingg;


import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.spark.sql.types.DataType;
import org.apache.spark.sql.types.DataTypes;
import org.junit.jupiter.api.Test;

import zingg.spark.core.executor.ZinggSparkTester;

public class TestDataType extends ZinggSparkTester{
	
	@Test
	public void testDataType() {
		assertEquals("ArrayType(DoubleType,true)",String.valueOf(DataTypes.createArrayType(DataTypes.DoubleType)));
		assertEquals("ArrayType(StringType,true)",String.valueOf(DataTypes.createArrayType(DataTypes.StringType)));
		assertEquals("ArrayType(DoubleType,true)",String.valueOf(DataType.fromJson("{\"type\":\"array\",\"elementType\":\"double\",\"containsNull\":true}")));
		assertEquals("ArrayType(StringType,true)",String.valueOf(DataType.fromJson("{\"type\":\"array\",\"elementType\":\"string\",\"containsNull\":true}")));
		assertEquals("ArrayType(DoubleType,true)",String.valueOf(DataType.fromDDL("ARRAY<DOUBLE>")));
		assertEquals("ArrayType(StringType,true)",String.valueOf(DataType.fromDDL("ARRAY<STRING>")));
		assertEquals("ArrayType(DoubleType,true)",String.valueOf(DataType.fromDDL("array<double>")));
		
		assertEquals("StringType",String.valueOf(DataType.fromDDL("STRING")));
		assertEquals("StringType",String.valueOf(DataType.fromDDL("String")));
		assertEquals("StringType",String.valueOf(DataType.fromDDL("string")));
		assertEquals("DateType",String.valueOf(DataType.fromDDL("date")));
		
	}
	
}
