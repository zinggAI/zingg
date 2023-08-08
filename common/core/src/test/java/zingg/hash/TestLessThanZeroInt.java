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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import zingg.common.core.hash.LessThanZeroInt;

public class TestLessThanZeroInt {

	@Test
	public void testLessThanZeroIntForValueZero() {
	    LessThanZeroInt value = getInstance();
		assertFalse(value.call(0));
	}

	@Test
	public void testLessThanZeroIntForValueNull() {
	    LessThanZeroInt value = getInstance();
		assertFalse(value.call(null));
	}

	@Test
	public void testLessThanZeroIntNegativeInteger() {
	    LessThanZeroInt value = getInstance();
		assertTrue(value.call(-5435));
	}

	@Test
	public void testLessThanZeroIntPositiveInteger() {
	    LessThanZeroInt value = getInstance();
		assertFalse(value.call(5435));
	}
	
    private LessThanZeroInt getInstance() {
        LessThanZeroInt value = new LessThanZeroInt();
        return value;
    }

}
