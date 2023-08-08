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

import zingg.common.core.hash.First3CharsBox;

public class TestFirst3CharsBox {
	
	@Test
	public void testFirst3CharsBox1() {
	    First3CharsBox value = getInstance();
		assertEquals(1, value.call("elephant"));
	}

    @Test
    public void testFirst3CharsBox2() {
        First3CharsBox value = getInstance();
        assertEquals(2, value.call("father"));
    }
	
    @Test
    public void testFirst3CharsBox3() {
        First3CharsBox value = getInstance();
        assertEquals(2, value.call("India"));
    }
    
    @Test
    public void testFirst3CharsBox4() {
        First3CharsBox value = getInstance();
        assertEquals(3, value.call("Izzze"));
    }

    @Test
    public void testFirst3CharsBox5() {
        First3CharsBox value = getInstance();
        assertEquals(4, value.call("Noddy"));
    }
    
    @Test
    public void testFirst3CharsBox6() {
        First3CharsBox value = getInstance();
        assertEquals(5, value.call("Sunday"));
    }

    @Test
    public void testFirst3CharsBox7() {
        First3CharsBox value = getInstance();
        assertEquals(6, value.call("Uzzzz"));
    }

    @Test
    public void testFirst3CharsBox8() {
        First3CharsBox value = getInstance();
        assertEquals(6, value.call("xyzxyz"));
    }

    @Test
    public void testFirst3CharsBoxForShortWord() {
        First3CharsBox value = getInstance();
        assertEquals(0, value.call("ab"));
    }

    @Test
    public void testFirst3CharsBoxForNull() {
        First3CharsBox value = getInstance();
        assertEquals(0, value.call(null));
    }

    private First3CharsBox getInstance() {
        return new First3CharsBox();
    }


}
