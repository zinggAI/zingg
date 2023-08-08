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

package zingg.spark.core.block;

import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.types.DataType;

import zingg.common.client.ZFrame;
import zingg.common.client.util.ListMap;
import zingg.common.core.block.Block;
import zingg.common.core.hash.HashFunction;

public class SparkBlock extends Block<Dataset<Row>, Row, Column, DataType> {

    private static final long serialVersionUID = 1L;


	public SparkBlock(){}
    

    public SparkBlock(ZFrame<Dataset<Row>, Row, Column> training, ZFrame<Dataset<Row>, Row, Column> dupes,
    ListMap<DataType, HashFunction<Dataset<Row>, Row, Column, DataType>> functionsMap, long maxSize) {
		super(training, dupes, functionsMap, maxSize);
	}


    @Override
    public DataType getDataTypeFromString(String t) {
        return DataType.fromDDL(t);
    }

}
