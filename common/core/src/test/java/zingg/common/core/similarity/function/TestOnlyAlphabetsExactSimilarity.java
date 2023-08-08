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
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestOnlyAlphabetsExactSimilarity {
	
	
	@Test
	public void testNotSameAlhpabets() {
		OnlyAlphabetsExactSimilarity sim = new OnlyAlphabetsExactSimilarity();
		double score = sim.call("I have 1 number", "I have no number");
		assertEquals(0d, score);
	}

	@Test
	public void testSameAlphabetsDiffNumbers() {
		OnlyAlphabetsExactSimilarity sim = new OnlyAlphabetsExactSimilarity();
		double score = sim.call("I have 1 number", "I have 3 number");
		assertEquals(1d, score);
	}
	
	@Test
	public void testSameNoNum() {
		OnlyAlphabetsExactSimilarity sim = new OnlyAlphabetsExactSimilarity();
		assertEquals(1d, sim.call("I have no number", "I have no number"));
	}

	@Test
	public void testDiffNoNumber() {
		OnlyAlphabetsExactSimilarity sim = new OnlyAlphabetsExactSimilarity();
		assertEquals(0d, sim.call("I have a no number", "I have r number"));
	}	

	@Test
	public void testSameIgnoreCase() {
		OnlyAlphabetsExactSimilarity sim = new OnlyAlphabetsExactSimilarity();
		assertEquals(1d, sim.call("I have 1 number", "I HAVE 2 number"));
	}
}
