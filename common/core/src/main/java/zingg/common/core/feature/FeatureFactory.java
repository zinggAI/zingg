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

package zingg.common.core.feature;

import java.io.Serializable;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class FeatureFactory<T> implements Serializable {

	public static final Log LOG = LogFactory.getLog(FeatureFactory.class);

	protected Map<T, Class> map;

	public abstract void init(); 

	public abstract T getDataTypeFromString(String t) ;

	public Object get(String dataType) throws Exception {
		if (map == null) {
			init();
		} 
		return map.get(getDataTypeFromString(dataType)).newInstance();
	}

}
