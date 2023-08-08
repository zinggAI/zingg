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

import zingg.common.core.hash.LessThanZeroDbl;

public class TestLessThanZeroDbl {

	@Test
	public void testLessThanZeroDblForValueZero() {
	    LessThanZeroDbl value = getInstance();
		assertFalse(value.call(0.0));
	}

	@Test
	public void testLessThanZeroDblForValueNull() {
	    LessThanZeroDbl value = getInstance();
		assertFalse(value.call(null));
	}
	
    @Test
    public void testLessThanZeroDblForValueNaN() {
        LessThanZeroDbl value = getInstance();
        assertFalse(value.call(Double.NaN));
    }	

	@Test
	public void testLessThanZeroDblNegativeInteger() {
	    LessThanZeroDbl value = getInstance();
		assertTrue(value.call(-5435.01));
	}

	@Test
	public void testLessThanZeroDblPositiveInteger() {
	    LessThanZeroDbl value = getInstance();
		assertFalse(value.call(5435.01));
	}

    private LessThanZeroDbl getInstance() {
        return new LessThanZeroDbl();
    }

}
