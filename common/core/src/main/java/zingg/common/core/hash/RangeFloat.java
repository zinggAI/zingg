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

package zingg.common.core.hash;

public class RangeFloat extends BaseHash<Float,Integer>{
	private static final long serialVersionUID = 1L;
	private int lowerLimit;
	private int upperLimit;

	public RangeFloat(int lower, int upper) {
	    setName("rangeBetween" + lower + "And" + upper + "Float");
		this.lowerLimit = lower;
		this.upperLimit = upper;
	}

	
	public Integer call(Float field) {
		int withinRange = 0;
		if (field != null && field >= lowerLimit && field < upperLimit) {
			withinRange = 1;
		}
		return withinRange;
	}


    public int getLowerLimit() {
        return lowerLimit;
    }


    public int getUpperLimit() {
        return upperLimit;
    }	

}
