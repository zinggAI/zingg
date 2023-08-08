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

import zingg.common.core.hash.FirstChars;

public class TestFirstChars {
	
	@Test
	public void testFirstChars1() {
	    FirstChars value = getInstance(2);
		assertEquals("un", value.call("unhappy"));
	}

    @Test
    public void testFirstChars2() {
        FirstChars value = getInstance(12);
        assertEquals("unhappy", value.call("unhappy"));
    }
	
    private FirstChars getInstance(int endIndex) {
        return new FirstChars(endIndex);
    }


}
