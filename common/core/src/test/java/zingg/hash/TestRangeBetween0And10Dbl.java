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

import zingg.common.core.hash.RangeDbl;

public class TestRangeBetween0And10Dbl {

    private RangeDbl getInstance() {
        return new RangeDbl(0,10);
    }

	@Test
	public void testRangeForValueZero() {
	    RangeDbl value = getInstance();
		assertEquals(1, value.call(0d));
	}

	@Test
	public void testRangeForNegativeValue() {
		Double input = -100d;
		RangeDbl value = getInstance();
		assertEquals(0, value.call(input));
	}

	@Test
	public void testRangeForVeryHighValue() {
		Double input = 999999d;
		RangeDbl value = getInstance();
		assertEquals(0, value.call(input));
	}

	@Test
	public void testRangeForValue8() {
	    RangeDbl value = getInstance();
		assertEquals(1, value.call(8d));
	}

	@Test
	public void testRangeForValue65() {
	    RangeDbl value = getInstance();
		assertEquals(0, value.call(65d));
	}

	@Test
	public void testRangeForValue867() {
	    RangeDbl value = getInstance();
		assertEquals(0, value.call(867d));
	}
	@Test
	public void testRangeForValue8637() {
	    RangeDbl value = getInstance();
		assertEquals(0, value.call(8637d));
	}
	@Test
	public void testRangeForNull() {
        RangeDbl value = getInstance();
		assertEquals(0, value.call(null));
	}
	@Test
	public void testRangeForUpperLimit() {
		RangeDbl value = getInstance();
		assertEquals(10, value.getUpperLimit()); 
	}
	@Test
	public void testRangeForLowerLimit() {
		RangeDbl value = getInstance();
		assertEquals(0, value.getLowerLimit()); 
	}
}
