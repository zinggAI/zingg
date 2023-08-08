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

import zingg.common.core.hash.TrimLastDigitsInt;

public class TestTrimLastDigitsInt {
	
	@Test
	public void testTrimLast1Digit() {
	    TrimLastDigitsInt value = getInstance(1);
		assertEquals(54353, value.call(543534));
	}

	@Test
	public void testTrimLast2DigitsInt() {
	    TrimLastDigitsInt value = getInstance(2);
		assertEquals(5435, value.call(543534));
	}

	@Test
	public void testTrimLast3DigitsInt() {
	    TrimLastDigitsInt value = getInstance(3);
		assertEquals(543, value.call(543534));
	}

	@Test
	public void testTrimLast3DigitsIntNullValue() {
	    TrimLastDigitsInt value = getInstance(3);
		assertEquals(null, value.call(null));
	}

    private TrimLastDigitsInt getInstance(int num) {
        return new TrimLastDigitsInt(num);
    }
	
}
