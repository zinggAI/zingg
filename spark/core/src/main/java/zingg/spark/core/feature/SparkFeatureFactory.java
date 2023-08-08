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

package zingg.spark.core.feature;

import java.util.HashMap;

import org.apache.spark.sql.types.DataType;
import org.apache.spark.sql.types.DataTypes;

import zingg.common.core.feature.ArrayDoubleFeature;
import zingg.common.core.feature.DateFeature;
import zingg.common.core.feature.DoubleFeature;
import zingg.common.core.feature.FeatureFactory;
import zingg.common.core.feature.FloatFeature;
import zingg.common.core.feature.IntFeature;
import zingg.common.core.feature.LongFeature;
import zingg.common.core.feature.StringFeature;

public class SparkFeatureFactory extends FeatureFactory<DataType>{

	private static final long serialVersionUID = 1L;
    
    @Override
    public void init() {
            map = new HashMap<DataType, Class>();
            map.put(DataTypes.StringType, StringFeature.class);
            map.put(DataTypes.IntegerType, IntFeature.class);
            map.put(DataTypes.DateType, DateFeature.class);
            map.put(DataTypes.DoubleType, DoubleFeature.class);
            map.put(DataTypes.FloatType, FloatFeature.class);
            map.put(DataTypes.LongType, LongFeature.class);
            map.put(DataTypes.createArrayType(DataTypes.DoubleType), ArrayDoubleFeature.class);
    }

    @Override
    public DataType getDataTypeFromString(String t) {
        return DataType.fromDDL(t);
    }
    
}
