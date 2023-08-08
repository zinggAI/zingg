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

/**
 * Base class for hash functions related to truncating of floats
 * 
 *
 */
public class TruncateFloat extends BaseHash<Float,Float>{
	private static final long serialVersionUID = 1L;
	private int numDecimalPlaces;
	static final int[] POWERS_OF_10 = {1, 10, 100, 1000, 10000, 100000};
	public TruncateFloat(int numDecimalPlaces) {
	    setName("truncateFloatTo" + numDecimalPlaces + "Places");
		this.numDecimalPlaces = numDecimalPlaces;
	}

	
	public Float call(Float field) {
		Float r = null;
		if (field == null) {
			r = field;
		} else {
			r = (float)(Math.floor(field * POWERS_OF_10[numDecimalPlaces]) / POWERS_OF_10[numDecimalPlaces]);
		}
		return r;
	}


    public int getNumDecimalPlaces() {
        return numDecimalPlaces;
    }

}
