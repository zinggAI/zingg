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

import zingg.common.core.hash.TruncateDouble;

public class TestTruncateDouble {
	
	@Test
	public void testTruncateDoubleTo1Place() {
	    TruncateDouble value =  getInstance(1);
		assertEquals(543534.6d, value.call(543534.67734));
	}

	@Test
	public void testTruncateDoubleTo2Place() {
	    TruncateDouble value =  getInstance(2);
		assertEquals(543534.67d, value.call(543534.67734));
	}

	@Test
	public void testTruncateDoubleTo3Place() {
	    TruncateDouble value =  getInstance(3);
		assertEquals(543534.677d, value.call(543534.67734));
	}

	@Test
	public void testTruncateDoubleTo3PlaceWhenNumberhas2DecimalPlaces() {
	    TruncateDouble value =  getInstance(3);
		assertEquals(543534.670d, value.call(543534.67));
	}

	@Test
	public void testTruncateDoubleTo3PlaceForNumberWithNoDecimalPlaces() {
	    TruncateDouble value =  getInstance(3);
		assertEquals(543534.000d, value.call(543534d));
	}

	@Test
	public void testTruncateDoubleTo3PlaceForNumberNAN() {
	    TruncateDouble value =  getInstance(3);
		assertEquals(Double.NaN, value.call(Double.NaN));
	}

	@Test
	public void testTruncateDoubleTo3PlaceForNumber0() {
	    TruncateDouble value =  getInstance(3);
		assertEquals(0d, value.call(0d));
	}

	@Test
	public void testTruncateDoubleTo3PlaceForNull() {
	    TruncateDouble value =  getInstance(3);
		assertEquals(null, value.call(null));
	}
	
    private TruncateDouble getInstance(int num) {
        return new TruncateDouble(num);
    }

}
