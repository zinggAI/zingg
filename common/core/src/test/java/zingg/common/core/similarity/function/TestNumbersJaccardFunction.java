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

public class TestNumbersJaccardFunction {
	
	
	@Test
	public void testFirstHas1NumSecondHas0Num() {
		NumbersJaccardFunction sim = new NumbersJaccardFunction();
		assertEquals(0d, sim.call("I have 1 number", "I have no number"));
	}

	@Test
	public void testFirstHas0NumSecondHas1Num() {
		NumbersJaccardFunction sim = new NumbersJaccardFunction();
		assertEquals(0d, sim.call("I have no number", "I have 1 number"));
	}
	
	@Test
	public void testFirstHas0NumSecondHas0Num() {
		NumbersJaccardFunction sim = new NumbersJaccardFunction();
		assertEquals(0d, sim.call("I have no number", "I have no number"));
	}

	@Test
	public void testFirstHas1NumSecondHas1NumMatch() {
		NumbersJaccardFunction sim = new NumbersJaccardFunction();
		assertEquals(1d, sim.call("I have 1 number", "I have 1 number"));
	}

	@Test
	public void testFirstHas1NumSecondHas1NumNoMatch() {
		NumbersJaccardFunction sim = new NumbersJaccardFunction();
		assertEquals(0d, sim.call("I have 1 number", "I have 2 number"));
	}

	@Test
	public void testFirstHas2NumSecondHas2NumNoMatch() {
		NumbersJaccardFunction sim = new NumbersJaccardFunction();
		assertEquals(0d, sim.call("I have 1 number 2 ", "I have 3 4 number"));
	}

	@Test
	public void testFirstHas2NumSecondHas2Num1Match() {
		NumbersJaccardFunction sim = new NumbersJaccardFunction();
		assertEquals(1.0d/3, sim.call("I have 1 number 2 ", "I have 3 1 number"));
	}
	
	@Test
	public void testFirstHas2NumSecondHas2Num2Match() {
		NumbersJaccardFunction sim = new NumbersJaccardFunction();
		assertEquals(1.0d, sim.call("I have 1 number 2 ", "I have 2 number 1"));
	}

	@Test
	public void testFirstHas2NumSecondHas2Num2ButNotClean() {
		NumbersJaccardFunction sim = new NumbersJaccardFunction();
		assertEquals(1d, sim.call("I have1 number2 ", "I have 2number 1unclean"));
	}
}
