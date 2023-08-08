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
import org.apache.spark.sql.types.DataTypes;

import zingg.common.core.hash.LessThanZeroFloat;

public class SparkLessThanZeroFloat extends SparkHashFunction<Float, Boolean>{
	
    private static final long serialVersionUID = 1L;
	public static final Log LOG = LogFactory.getLog(SparkLessThanZeroFloat.class);
    
    public SparkLessThanZeroFloat() {
        setBaseHash(new LessThanZeroFloat());
        setDataType(DataTypes.FloatType);
        setReturnType(DataTypes.BooleanType);
    }

}
