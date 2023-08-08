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

package zingg.common.core.util;

import zingg.common.client.util.ColValues;

public enum LabelMatchType {
	NOT_SURE(ColValues.IS_NOT_SURE_PREDICTION, "NOT SURE"), 
	UNDEFINED(ColValues.IS_NOT_KNOWN_PREDICTION, "ARE NOT KNOWN IF MATCH"), 
	DO_NOT_MATCH(ColValues.IS_NOT_A_MATCH_PREDICTION, "DO NOT MATCH"), 
	MATCH(ColValues.IS_MATCH_PREDICTION, "MATCH");

	private Double value;  
	public String msg;

	private LabelMatchType(Double value, String msg){  
		this.value=value; 
		this.msg = msg;
	}

	public static LabelMatchType get(double value) {
		for (LabelMatchType t: LabelMatchType.values()) {
			if (t.value.equals(value)) return t;
		}
		return null;
	}

}