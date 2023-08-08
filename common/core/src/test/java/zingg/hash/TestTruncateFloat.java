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

package zingg.hash;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import zingg.common.core.hash.TruncateFloat;

public class TestTruncateFloat {
    @Test
    public void testTruncateFloatTo1Place() {
        TruncateFloat value = getInstance(1);
        assertEquals(435.3f,value.call(435.391f));
    }
    @Test
    public void testTruncateFloatTo2Place() {
        TruncateFloat value = getInstance(2);
        assertEquals(435.39f, value.call(435.391f));
    }
    @Test
    public void testTruncateFloatTo3PlaceWhenNumHas2DecimalPlaces() {
        TruncateFloat value = getInstance(3);
        assertEquals(35.720f, value.call(35.72f));
    }
    @Test
    public void testTruncateFloatTo3PlaceWhenNumHasNoDecimalPlaces() {
        TruncateFloat value = getInstance(3);
        assertEquals(32.000f, value.call(32f));
    }
    @Test
	public void testTruncateFloatTo3PlaceForNumber0() {
	    TruncateFloat value =  getInstance(3);
		assertEquals(0f, value.call(0f));
	}

	@Test
	public void testTruncateFloatTo3PlaceForNull() {
	    TruncateFloat value =  getInstance(3);
		assertEquals(null, value.call(null));
	}
	

    private TruncateFloat getInstance(int num) {
        return new TruncateFloat(num);
    }
}
