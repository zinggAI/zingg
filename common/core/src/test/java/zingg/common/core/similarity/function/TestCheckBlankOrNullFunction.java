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

package zingg.common.core.similarity.function;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestCheckBlankOrNullFunction {
	
	
	@Test
	public void testFirstWordNull() {
		CheckBlankOrNullFunction isNull = new CheckBlankOrNullFunction();
		assertEquals(0d, isNull.call(null, "second"));
	}

	@Test
	public void testFirstWordEmpty() {
		CheckBlankOrNullFunction isNull = new CheckBlankOrNullFunction();
		assertEquals(0d, isNull.call("", "second"));
	}

	@Test
	public void testSecondWordNull() {
		CheckBlankOrNullFunction isNull = new CheckBlankOrNullFunction();
		assertEquals(0d, isNull.call("f", null));
	}

	@Test
	public void testSecondWordEmpty() {
		CheckBlankOrNullFunction isNull = new CheckBlankOrNullFunction();
		assertEquals(0d, isNull.call("f", ""));
	}
	@Test
	public void testBothEmpty() {
		CheckBlankOrNullFunction isNull = new CheckBlankOrNullFunction();
		assertEquals(0d, isNull.call("", ""));
	}

	@Test
	public void testBothNull() {
		CheckBlankOrNullFunction isNull = new CheckBlankOrNullFunction();
		assertEquals(0d, isNull.call(null, null));
	}

	@Test
	public void testBothNotNullOrEmpty() {
		CheckBlankOrNullFunction isNull = new CheckBlankOrNullFunction();
		assertEquals(1d, isNull.call("not null", "not null"));
	}

}
