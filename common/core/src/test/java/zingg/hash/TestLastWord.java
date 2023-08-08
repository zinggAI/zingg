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

import zingg.common.core.hash.LastWord;

public class TestLastWord {
	
	@Test
	public void testLastWord1() {
	    LastWord value = getInstance();
		assertEquals("gupta", value.call("vikas gupta"));
	}

    @Test
    public void testLastWord2() {
        LastWord value = getInstance();
        assertEquals("gupta", value.call("gupta"));
    }
	
    @Test
    public void testLastWord3() {
        LastWord value = getInstance();
        assertEquals(null, value.call(null));
    }
	
    private LastWord getInstance() {
        return new LastWord();
    }


}
