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

import zingg.common.core.hash.RangeInt;

public class TestRangeBetween100And1000Int {

    private RangeInt getInstance() {
        return new RangeInt(100,1000);
    }

	@Test
	public void testRangeForValueZero() {
	    RangeInt value = getInstance();
		assertEquals(0, value.call(0));
	}

	@Test
	public void testRangeForNegativeValue() {
	    RangeInt value = getInstance();
		assertEquals(0, value.call(-100));
	}

	@Test
	public void testRangeForVeryHighValue() {
	    RangeInt value = getInstance();
		assertEquals(0, value.call(999999));
	}

	@Test
	public void testRangeForValue8() {
	    RangeInt value = getInstance();
		assertEquals(0, value.call(8));
	}

	@Test
	public void testRangeForValue65() {
	    RangeInt value = getInstance();
		assertEquals(0, value.call(65));
	}

	@Test
	public void testRangeForValue867() {
	    RangeInt value = getInstance();
		assertEquals(1, value.call(867));
	}
	@Test
	public void testRangeForValue8637() {
	    RangeInt value = getInstance();
		assertEquals(0, value.call(8637));
	}
	@Test
	public void testRangeForNull() {
	    RangeInt value = getInstance();
		assertEquals(0, value.call(null));
	}
	@Test
	public void testRangeForUpperLimit() {
		RangeInt value = getInstance();
		assertEquals(1000, value.getUpperLimit()); 
	}
	@Test
	public void testRangeForLowerLimit() {
		RangeInt value = getInstance();
		assertEquals(100, value.getLowerLimit()); 
	}

}
