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

import zingg.common.core.hash.Round;

public class TestRound {
	
	@Test
	public void testRound1() {
	    Round value = getInstance();
		assertEquals(543535, value.call(543534.677));
	}

    @Test
    public void testRound2() {
        Round value = getInstance();
        assertEquals(543534, value.call(543534.377));
    }

    @Test
    public void testRound3() {
        Round value = getInstance();
        assertEquals(null, value.call(null));
    }
	
	
    private Round getInstance() {
        return new Round();
    }


}
