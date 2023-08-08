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

public class TestPinCodeMatchTypeFunction {
	
	
	@Test
	public void testFirstPinNull() {
		PinCodeMatchTypeFunction pincodeMatchFn = new PinCodeMatchTypeFunction();
		assertEquals(1d, pincodeMatchFn.call(null, "34567"));
	}

	@Test
	public void testFirstPinEmpty() {
		PinCodeMatchTypeFunction pincodeMatchFn = new PinCodeMatchTypeFunction();
		assertEquals(1d, pincodeMatchFn.call("", "28390-6890"));
	}

	@Test
	public void testSecondPinNull() {
		PinCodeMatchTypeFunction pincodeMatchFn = new PinCodeMatchTypeFunction();
		assertEquals(1d, pincodeMatchFn.call("93949-1894", null));
	}

	@Test
	public void testSecondPinEmpty() {
		PinCodeMatchTypeFunction pincodeMatchFn = new PinCodeMatchTypeFunction();
		assertEquals(1d, pincodeMatchFn.call("31249", ""));
	}
	@Test
	public void testBothEmpty() {
		PinCodeMatchTypeFunction pincodeMatchFn = new PinCodeMatchTypeFunction();
		assertEquals(1d, pincodeMatchFn.call("", ""));
	}

	@Test
	public void testBothNull() {
		PinCodeMatchTypeFunction pincodeMatchFn = new PinCodeMatchTypeFunction();
		assertEquals(1d, pincodeMatchFn.call(null, null));
	}

	@Test
	public void testBothNotEmptyExact() {
		PinCodeMatchTypeFunction pincodeMatchFn = new PinCodeMatchTypeFunction();
		assertEquals(1d, pincodeMatchFn.call("45678", "45678"));
	}

	@Test
	public void testBothNotEmptyDifferent() {
		PinCodeMatchTypeFunction pincodeMatchFn = new PinCodeMatchTypeFunction();
		assertEquals(0d, pincodeMatchFn.call("34234", "34334"));
	}

	@Test
	public void testFirstPartMatch() {
		PinCodeMatchTypeFunction pincodeMatchFn = new PinCodeMatchTypeFunction();
		assertEquals(1d, pincodeMatchFn.call("23412-9838", "23412-6934"));
	}

	@Test
	public void testFirstPartDifferentSecondPartMatch() {
		PinCodeMatchTypeFunction pincodeMatchFn = new PinCodeMatchTypeFunction();
		assertEquals(0d, pincodeMatchFn.call("34625-2153", "34325-2153"));
	}

	@Test
	public void testOnlyFirstPartMatch() {
		PinCodeMatchTypeFunction pincodeMatchFn = new PinCodeMatchTypeFunction();
		assertEquals(1d, pincodeMatchFn.call("79492", "79492-3943"));
	}

	@Test
	public void testFirstPartDifferent() {
		PinCodeMatchTypeFunction pincodeMatchFn = new PinCodeMatchTypeFunction();
		assertEquals(0d, pincodeMatchFn.call("87248", "87238-9024"));
	}

	@Test
	public void testFirstPartmatch() {
		PinCodeMatchTypeFunction pincodeMatchFn = new PinCodeMatchTypeFunction();
		assertEquals(1d, pincodeMatchFn.call("89827-9703", "89827"));
	}
}
